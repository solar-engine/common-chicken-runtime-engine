package ccre.drivers.chrobotics;

import java.io.IOException;
import java.util.Arrays;

import ccre.channel.EventOutput;
import ccre.channel.SerialIO;
import ccre.channel.SerialInput;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.drivers.ByteFiddling;
import ccre.drivers.NMEA;
import ccre.log.Logger;
import ccre.util.CArrayUtils;
import ccre.util.Utils;

/**
 * The low-level interface to the UM7-LT orientation sensor from CH Robotics,
 * via RS232.
 * 
 * @author skeggsc
 * @see ccre.drivers.chrobotics.UM7LT
 */
public class InternalUM7LT { // default rate: 115200 baud.
    private final SerialIO rs232;
    private final Object rs232lock = new Object();
    public static final int DREG_BASE = 0x55;
    public static final int DREG_LAST = 0x88;
    public static final int DREG_HEALTH = 0x55;
    public static final int DREG_EULER_PHI_THETA = 0x70;
    public static final int DREG_EULER_PSI = 0x71;
    public static final int DREG_EULER_PHI_THETA_DOT = 0x72;
    public static final int DREG_EULER_PSI_DOT = 0x73;
    public static final int DREG_EULER_TIME = 0x74;
    public static final float EULER_CONVERSION_DIVISOR = 91.02222f;
    public static final float EULER_RATE_CONVERSION_DIVISOR = 16.0f;
    public int[] dregs = new int[DREG_LAST - DREG_BASE];
    public int[] dregsUpdateAt = new int[DREG_LAST - DREG_BASE];
    public int lastUpdateId = 0;
    public final Object notify = new Object();
    public long lastUpdateTime = System.currentTimeMillis();
    private final EventOutput onUpdate;

    public InternalUM7LT(SerialIO rs232, EventOutput onUpdate) {
        this.rs232 = rs232;
        this.onUpdate = onUpdate;
    }

    public void writeSettings(int quaternion_rate, int euler_rate, int position_rate, int velocity_rate, int health_rate_step) throws IOException {
        int com_settings = (5 << 28); // just set the baud rate to default.
        int com_rates1 = 0;
        int com_rates2 = 0;
        int com_rates3 = 0;
        int com_rates4 = 0;
        int com_rates5 = ((quaternion_rate & 0xFF) << 24) | ((euler_rate & 0xFF) << 16) | ((position_rate & 0xFF) << 8) | (velocity_rate & 0xFF);
        int com_rates6 = (health_rate_step & 0xF) << 16;
        int com_rates7 = 0;
        doBatchWriteOperation((byte) 0x00, new int[] { com_settings, com_rates1, com_rates2, com_rates3, com_rates4, com_rates5, com_rates6, com_rates7 });
    }

    public void handleRS232Input(int count) throws IOException {
        byte[] activeBuffer = new byte[4096];
        int from = 0, to = 0;
        while (count-- > 0) {
            int consumed = handlePacket(activeBuffer, from, to);
            if (consumed == 0) { // need more data
                if (from != 0 && to >= activeBuffer.length - 64) { // nearing the end, or at the end - shift earlier.
                    System.arraycopy(activeBuffer, from, activeBuffer, 0, to - from);
                    to -= from;
                    from = 0;
                }
                if (activeBuffer.length == to) {
                    // still no matched packet...?
                    Logger.warning("RS232 input buffer overflow, somehow? Resetting buffer.");
                    from = to = 0;
                }
                byte[] gotten = rs232.readBlocking(activeBuffer.length - to);
                System.arraycopy(gotten, 0, activeBuffer, to, gotten.length);
                to += gotten.length;
            } else {
                from += consumed;
                if (from == to) {
                    from = to = 0;
                }
            }
        }
    }

    // Returns the number of consumed bytes, or zero if packet needs more data to be valid.
    public int handlePacket(byte[] bytes, int from, int to) {
        // TODO: Check bounds on To.
        if (to - from < 6) { // no way for any valid packets to be ready
            return 0;
        }
        try {
            if (bytes[from] == '$') {
                int end = NMEA.getPacketEnd(bytes, from, to);
                if (end != -1) {
                    handleNMEA(bytes, from, end);
                    return end - from;
                } else {
                    return 0;
                }
            } else if (bytes[from] == 's' && bytes[from + 1] == 'n' && bytes[from + 2] == 'p') {
                return handleBinary(bytes, from, to);
            } else {
                throw new IOException("Invalid packet that starts with bytes " + ByteFiddling.toHex(bytes, from, Math.min(to, from + 8)));
            }
        } catch (IOException ex) {
            Logger.warning("UM7 message handling failed - attempting to reset state", ex);
        }
        int possibleStartNMEA = ByteFiddling.indexOf(bytes, from + 1, to, (byte) '$');
        int possibleStartBinary = ByteFiddling.indexOf(bytes, from + 1, to, (byte) 's');
        if (possibleStartBinary != -1 && (possibleStartNMEA == -1 || possibleStartBinary < possibleStartNMEA)) {
            Logger.fine("Skipping " + (possibleStartBinary - from) + " bytes to Binary.");
            return possibleStartBinary - from; // skip until the start
        } else if (possibleStartNMEA != -1) {
            Logger.fine("Skipping " + (possibleStartNMEA - from) + " bytes to NMEA.");
            return possibleStartNMEA - from; // skip until the start
        } else {
            Logger.fine("Skipping " + (to - from) + " bytes to end.");
            return to - from; // everything's bad. skip it all.
        }
    }

    // Returns the number of consumed bytes, or zero if packet needs more data to be valid.
    private int handleBinary(byte[] bin, int from, int to) throws IOException {
        if (to - from < 7) {
            return 0;
        }
        from += 3; // 'snp' has already been checked
        byte packet_type = bin[from];
        byte address = bin[from + 1];
        boolean has_data = (packet_type & 0x80) != 0;
        boolean is_batch = (packet_type & 0x40) != 0;
        int batch_length = is_batch ? (packet_type & 0x3C) >> 2 : -1;
        boolean is_hidden = (packet_type & 0x2) != 0;
        boolean is_command_failed = (packet_type & 0x1) != 0;
        int data_count = has_data ? (is_batch ? batch_length : 1) : 0;
        if (to - from < 7 + data_count * 4) {
            return 0;
        }
        checkChecksum(bin, from - 3, from + 4 + data_count * 4); // -3 for snp
        int[] data = new int[data_count];
        for (int i = 0; i < data_count; i++) {
            data[i] = ((bin[from + 2 + 4 * i + 0] & 0xFF) << 24) |
                    ((bin[from + 2 + 4 * i + 1] & 0xFF) << 16) |
                    ((bin[from + 2 + 4 * i + 2] & 0xFF) << 8) |
                    ((bin[from + 2 + 4 * i + 3] & 0xFF));
        }
        if (address + data_count - 1 >= DREG_BASE && address < DREG_LAST) {
            synchronized (notify) {
                int nextUpdateId = lastUpdateId + 1;
                for (int i = 0; i < data_count; i++) {
                    int regid = address + i - DREG_BASE;
                    if (regid >= 0 && regid < DREG_LAST - DREG_BASE) {
                        dregs[regid] = data[i];
                        dregsUpdateAt[regid] = nextUpdateId;
                    }
                }
                lastUpdateId = nextUpdateId;
                lastUpdateTime = System.currentTimeMillis();
                notify.notifyAll();
            }
            onUpdate.event();
        } else if (address == (byte) 0xAA) {
            Logger.info("UM7LT firmware revision: " + new String(new char[] { (char) ((data[0] >> 24) & 0xFF), (char) ((data[0] >> 16) & 0xFF), (char) ((data[0] >> 8) & 0xFF), (char) (data[0] & 0xFF) }));
        } else if (address == (byte) 0xAD) {
            Logger.config("UM7LT gyro calibrating...");
        } else if (address == (byte) 0xC) {
            Logger.config("UM7LT gyro calibration updated.");
        } else if (address != 0) {
            Logger.finest("UNHANDLED BINARY MESSAGE " + Integer.toHexString(address & 0xFF) + " " + CArrayUtils.asList(data) + " [" + is_hidden + ":" + is_command_failed + "]");
        }
        return 2 + 4 * data_count + 2 + 3; // two for checksum, three for the 'snp' that was stripped out. 
    }
    
    public void zeroGyros(boolean aggressive) throws IOException {
        doReadOperation((byte) 0xAD);
        if (aggressive) {
            doBatchWriteOperation((byte) 0x70, new int[] {0, 0, 0, 0, Float.floatToIntBits(10f)});
        }
    }

    public void doReadOperation(byte address) throws IOException {
        sendWithChecksum(new byte[] { 's', 'n', 'p', 0x00, address, 0, 0 });
    }

    public void doBatchReadOperation(byte address, int count) throws IOException {
        if (count < 1 || count > 15) {
            // don't allow zero - why would we?
            throw new IllegalArgumentException("Bad count in encodeBatchReadOperation: must be in [1, 15]");
        }
        sendWithChecksum(new byte[] { 's', 'n', 'p', (byte) (0x40 | (count << 2)), address, 0, 0 });
    }

    public void doWriteOperation(byte address, int value) throws IOException {
        sendWithChecksum(new byte[] { 's', 'n', 'p', (byte) 0x80, address, (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value, 0, 0 });
    }

    public void doBatchWriteOperation(byte address, int[] values) throws IOException {
        if (values.length < 1 || values.length > 15) {
            // don't allow zero - why would we?
            throw new IllegalArgumentException("Bad length in encodeBatchWriteOperation: must be in [1, 15]");
        }
        byte[] out = new byte[7 + 4 * values.length];
        out[0] = 's';
        out[1] = 'n';
        out[2] = 'p';
        out[3] = (byte) (0xC0 | (values.length << 2));
        out[4] = address;
        int ptr = 5;
        for (int value : values) {
            out[ptr++] = (byte) (value >> 24);
            out[ptr++] = (byte) (value >> 16);
            out[ptr++] = (byte) (value >> 8);
            out[ptr++] = (byte) value;
        }
        sendWithChecksum(out); // the last two unset bytes are checksum bytes.
    }

    private void sendWithChecksum(byte[] data) throws IOException {
        synchronized (rs232lock) {
            rs232.writeFully(addChecksum(data), 0, data.length);
        }
    }

    private int checksumTotal(byte[] data, int from, int to) {
        int total = 0;
        for (int i = from; i < to; i++) {
            total += data[i] & 0xFF;
        }
        return total;
    }

    private byte[] addChecksum(byte[] data) {
        int total = checksumTotal(data, 0, data.length - 2);
        data[data.length - 2] = (byte) (total >> 8);
        data[data.length - 1] = (byte) total;
        return data;
    }

    private void checkChecksum(byte[] data, int from, int to) throws IOException {
        int total = checksumTotal(data, from, to - 2);
        if (data[to - 2] != (byte) (total >> 8) || data[to - 1] != (byte) total) {
            throw new IOException("Binary checksum mismatch: got " + ByteFiddling.toHex(data, from, to));
        }
    }

    private void handleNMEA(byte[] nmea, int from, int to) throws IOException {
        Logger.finest("UM7LT NMEA received: " + ByteFiddling.parseASCII(nmea, from, to));
    }

    private boolean parseBoolean(byte[] bs, int field) throws IOException {
        Integer bl = ByteFiddling.parseInt(bs);
        if (bl == null || (bl & ~1) != 0) {
            throw new IOException("Malformed field # " + field + " of PCHRH.");
        }
        return bl != 0;
    }

    public void dumpSerialData() throws IOException {
        while (this.rs232.readNonblocking(1024).length > 0) {
            Logger.finest("dumping...");
        }
    }
}

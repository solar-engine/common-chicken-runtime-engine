package ccre.drivers.chrobotics;

import java.io.IOException;
import java.util.Arrays;

import ccre.channel.SerialIO;
import ccre.channel.SerialInput;
import ccre.drivers.ByteFiddling;
import ccre.drivers.NMEA;
import ccre.log.Logger;
import ccre.util.Utils;

/**
 * The low-level interface to the UM7-LT orientation sensor from CH Robotics,
 * via RS232.
 * 
 * @author skeggsc
 * @see ccre.drivers.chrobotics.UM7LT
 */
public class InternalUM7LT { // default rate: 115200 baud.
    public long um_time_ms = -1;
    public Integer um_sats_used, um_sats_in_view, um_hdop; // hdop: horizontal dilution of position, from GPS
    public boolean um_quaternions, um_fault_overflow, um_fault_accl,
            um_fault_gyro, um_fault_mag, um_fault_gps;
    public Double gps_pn, gps_pe, gps_alt_rel, gps_heading;
    public Double gps_vn, gps_ve;
    public Double gps_latitude, gps_longitude, gps_altitude;
    public Double dir_roll, dir_pitch, dir_yaw;
    public Double dir_roll_rate, dir_pitch_rate, dir_yaw_rate;
    public Double quat_a, quat_b, quat_c, quat_d;
    public Double
            raw_gyro_x, raw_gyro_y, raw_gyro_z,
            raw_accl_x, raw_accl_y, raw_accl_z,
            raw_magn_x, raw_magn_y, raw_magn_z;
    private final SerialIO rs232;
    private final Object rs232lock = new Object();

    public InternalUM7LT(SerialIO rs232) {
        this.rs232 = rs232;
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
        doBatchWriteOperation((byte) 0x00, new int[] {com_settings, com_rates1, com_rates2, com_rates3, com_rates4, com_rates5, com_rates6, com_rates7});
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
        int possibleStartNMEA = ByteFiddling.indexOf(bytes, from, to, (byte) '$');
        int possibleStartBinary = ByteFiddling.indexOf(bytes, from, to, (byte) 's');
        if (possibleStartBinary != -1 && (possibleStartNMEA == -1 || possibleStartBinary < possibleStartNMEA)) {
            return possibleStartBinary - from; // skip until the start
        } else if (possibleStartNMEA != -1) {
            return possibleStartNMEA - from; // skip until the start
        } else {
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
        Logger.finest("BINARY " + Integer.toHexString(address & 0xFF) + " " + Arrays.toString(data) + " [" + is_hidden + ":" + is_command_failed + "]");
        return 2 + 4 * data_count + 2 + 3; // two for checksum, three for the 'snp' that was stripped out. 
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
            throw new IOException("Binary checksum mismatch: got " + Arrays.toString(Arrays.copyOfRange(data, from, to)));
        }
    }

    private void handleNMEA(byte[] nmea, int from, int to) throws IOException {
        byte[][] fields = NMEA.parse(nmea, from, to);
        byte[] title = fields[0];
        Logger.finest("NMEA received: " + title);
        // $PCHRH,time,sats_used,sats_in_view,HDOP,mode,COM,accel,gyro,mag,GPS,res,res,res,*checksum<CR><LN>
        if (ByteFiddling.streq(title, "PCHRH") && fields.length == 15) {
            Double dbl = ByteFiddling.parseDouble(fields[1]);
            if (dbl == null) {
                throw new IOException("Malformed field #1 of PCHRH.");
            }
            um_time_ms = Math.round(dbl * 1000);
            um_sats_used = ByteFiddling.parseInt(fields[2]);
            um_sats_in_view = ByteFiddling.parseInt(fields[3]);
            um_hdop = ByteFiddling.parseInt(fields[4]);
            um_quaternions = parseBoolean(fields[5], 5);
            um_fault_overflow = parseBoolean(fields[6], 6);
            um_fault_accl = parseBoolean(fields[7], 7);
            um_fault_gyro = parseBoolean(fields[8], 8);
            um_fault_mag = parseBoolean(fields[9], 9);
            um_fault_gps = parseBoolean(fields[10], 10);
            // reserved: fields[11], fields[12], fields[13]
            // field 14 is checksum, already checked by NEMA.parse.
        } else if (ByteFiddling.streq(title, "PCHRP") && fields.length == 10) {
            Double dbl = ByteFiddling.parseDouble(fields[1]);
            if (dbl == null) {
                throw new IOException("Malformed field #1 of PCHRP.");
            }
            um_time_ms = Math.round(dbl * 1000);
            gps_pn = ByteFiddling.parseDouble(fields[2]);
            gps_pe = ByteFiddling.parseDouble(fields[3]);
            gps_alt_rel = ByteFiddling.parseDouble(fields[4]);
            dir_roll = ByteFiddling.parseDouble(fields[5]);
            dir_pitch = ByteFiddling.parseDouble(fields[6]);
            dir_yaw = ByteFiddling.parseDouble(fields[7]);
            gps_heading = ByteFiddling.parseDouble(fields[8]);
            // field 9 is checksum, already checked by NEMA.parse.
        } else if (ByteFiddling.streq(title, "PCHRA") && fields.length == 7) {
            Double dbl = ByteFiddling.parseDouble(fields[1]);
            if (dbl == null) {
                throw new IOException("Malformed field #1 of PCHRA.");
            }
            um_time_ms = Math.round(dbl * 1000);
            dir_roll = ByteFiddling.parseDouble(fields[2]);
            dir_pitch = ByteFiddling.parseDouble(fields[3]);
            dir_yaw = ByteFiddling.parseDouble(fields[4]);
            gps_heading = ByteFiddling.parseDouble(fields[5]);
            // field 6 is checksum, already checked by NEMA.parse.
        } else if (ByteFiddling.streq(title, "PCHRS") && fields.length == 7) {
            Integer sensorID = ByteFiddling.parseInt(fields[1]);
            if (sensorID == null) {
                throw new IOException("Malformed field #1 of PCHRS.");
            }
            Double dbl = ByteFiddling.parseDouble(fields[2]);
            if (dbl == null) {
                throw new IOException("Malformed field #2 of PCHRS.");
            }
            um_time_ms = Math.round(dbl * 1000);
            switch (sensorID) {
            case 0:
                raw_gyro_x = ByteFiddling.parseDouble(fields[3]);
                raw_gyro_y = ByteFiddling.parseDouble(fields[4]);
                raw_gyro_z = ByteFiddling.parseDouble(fields[5]);
                break;
            case 1:
                raw_accl_x = ByteFiddling.parseDouble(fields[3]);
                raw_accl_y = ByteFiddling.parseDouble(fields[4]);
                raw_accl_z = ByteFiddling.parseDouble(fields[5]);
                break;
            case 2:
                raw_magn_x = ByteFiddling.parseDouble(fields[3]);
                raw_magn_y = ByteFiddling.parseDouble(fields[4]);
                raw_magn_z = ByteFiddling.parseDouble(fields[5]);
                break;
            default:
                throw new IOException("Invalid sensorID in field #1 of PCHRS.");
            }
            // field 6 is checksum, already checked by NEMA.parse.
        } else if (ByteFiddling.streq(title, "PCHRR") && fields.length == 10) {
            Double dbl = ByteFiddling.parseDouble(fields[1]);
            if (dbl == null) {
                throw new IOException("Malformed field #1 of PCHRR.");
            }
            um_time_ms = Math.round(dbl * 1000);
            gps_vn = ByteFiddling.parseDouble(fields[2]);
            gps_ve = ByteFiddling.parseDouble(fields[3]);
            // not vu because it's always zero on the UM7.
            dir_roll_rate = ByteFiddling.parseDouble(fields[5]);
            dir_pitch_rate = ByteFiddling.parseDouble(fields[6]);
            dir_yaw_rate = ByteFiddling.parseDouble(fields[7]);
            gps_heading = ByteFiddling.parseDouble(fields[8]);
            // field 9 is checksum, already checked by NEMA.parse.
        } else if (ByteFiddling.streq(title, "PCHRG") && fields.length == 10) {
            Double dbl = ByteFiddling.parseDouble(fields[1]);
            if (dbl == null) {
                throw new IOException("Malformed field #1 of PCHRG.");
            }
            um_time_ms = Math.round(dbl * 1000);
            gps_latitude = ByteFiddling.parseDouble(fields[2]);
            gps_longitude = ByteFiddling.parseDouble(fields[3]);
            gps_altitude = ByteFiddling.parseDouble(fields[4]);
            dir_roll = ByteFiddling.parseDouble(fields[5]);
            dir_pitch = ByteFiddling.parseDouble(fields[6]);
            dir_yaw = ByteFiddling.parseDouble(fields[7]);
            gps_heading = ByteFiddling.parseDouble(fields[8]);
            // field 9 is checksum, already checked by NEMA.parse.
        } else if (ByteFiddling.streq(title, "PCHRQ") && fields.length == 7) {
            Double dbl = ByteFiddling.parseDouble(fields[1]);
            if (dbl == null) {
                throw new IOException("Malformed field #1 of PCHRG.");
            }
            um_time_ms = Math.round(dbl * 1000);
            quat_a = ByteFiddling.parseDouble(fields[2]);
            quat_b = ByteFiddling.parseDouble(fields[3]);
            quat_c = ByteFiddling.parseDouble(fields[4]);
            quat_d = ByteFiddling.parseDouble(fields[5]);
            // field 6 is checksum, already checked by NEMA.parse.
        } else {
            throw new IOException("Unrecognized NMEA packet: " + ByteFiddling.parseASCII(title) + " with field count " + fields.length);
        }
    }

    private boolean parseBoolean(byte[] bs, int field) throws IOException {
        Integer bl = ByteFiddling.parseInt(bs);
        if (bl == null || (bl & ~1) != 0) {
            throw new IOException("Malformed field # " + field + " of PCHRH.");
        }
        return bl != 0;
    }
}

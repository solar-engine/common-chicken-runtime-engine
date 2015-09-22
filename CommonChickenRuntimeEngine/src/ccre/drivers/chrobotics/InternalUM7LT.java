/*
 * Copyright 2015 Colby Skeggs
 *
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 *
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package ccre.drivers.chrobotics;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventOutput;
import ccre.channel.SerialIO;
import ccre.drivers.ByteFiddling;
import ccre.drivers.NMEA;
import ccre.time.Time;

/**
 * The low-level interface to the UM7-LT orientation sensor from CH Robotics,
 * via RS232.
 *
 * This is not complete. If you need more functionality, you may need to modify
 * this class.
 *
 * @author skeggsc
 * @see ccre.drivers.chrobotics.UM7LT
 */
public class InternalUM7LT { // default rate: 115200 baud.
    private static Logger logger = LoggerFactory.getLogger(InternalUM7LT.class);
    
    private final SerialIO rs232;
    private final Object rs232lock = new Object();
    /**
     * The first register that is tracked by the dregs cache.
     *
     * @see #dregs
     */
    public static final int DREG_BASE = 0x55;
    /**
     * The last register that is tracked by the dregs cache.
     *
     * @see #dregs
     */
    public static final int DREG_LAST = 0x88;
    /**
     * The ID of the Health register.
     */
    public static final int DREG_HEALTH = 0x55;
    /**
     * The ID of the Euler Phi Theta register.
     */
    public static final int DREG_EULER_PHI_THETA = 0x70;
    /**
     * The ID of the Euler Psi register.
     */
    public static final int DREG_EULER_PSI = 0x71;
    /**
     * The ID of the Euler Phi Theta Dot register.
     */
    public static final int DREG_EULER_PHI_THETA_DOT = 0x72;
    /**
     * The ID of the Euler Psi Dot register.
     */
    public static final int DREG_EULER_PSI_DOT = 0x73;
    /**
     * The ID of the Euler Time register.
     */
    public static final int DREG_EULER_TIME = 0x74;
    /**
     * The conversion divisor to convert from the signed shorts in the Euler
     * rotation registers to angles in degrees.
     */
    public static final float EULER_CONVERSION_DIVISOR = 91.02222f;
    /**
     * The conversion divisor to convert from the signed shorts in the Euler
     * rotation rate registers to angular velocities in degrees per second.
     */
    public static final float EULER_RATE_CONVERSION_DIVISOR = 16.0f;
    /**
     * The register cache for data coming from the UM7LT.
     *
     * @see #dregsUpdateAt
     */
    public int[] dregs = new int[DREG_LAST - DREG_BASE];
    /**
     * The last time that the corresponding entry in the register cache was
     * updated. This is an update ID.
     *
     * @see #dregs
     * @see #lastUpdateId
     */
    public int[] dregsUpdateAt = new int[DREG_LAST - DREG_BASE];
    /**
     * The last update ID - the pseudo-clock used for tracking register cache
     * updates.
     *
     * @see #dregs
     * @see #dregsUpdateAt
     * @see #lastUpdateTime
     */
    public int lastUpdateId = 0;
    /**
     * The last time that any cached registers were updated. Updated exactly
     * when lastUpdateId is updated.
     *
     * @see #lastUpdateId
     */
    public long lastUpdateTime = Time.currentTimeMillis();
    private final EventOutput onUpdate;
    private int correctBinaryPackets, correctNMEAPackets, incorrectPackets;
    private static boolean treatNMEAAsErroneous = true;

    /**
     * Create a new internal handler for the UM7LT that runs on a rs232 port and
     * fires onUpdate whenever the cached registers update.
     *
     * @param rs232 the RS232 port connected to the UM7LT.
     * @param onUpdate the output to update when the cached registers update.
     */
    public InternalUM7LT(SerialIO rs232, EventOutput onUpdate) {
        if (onUpdate == null || rs232 == null) {
            throw new NullPointerException();
        }
        this.rs232 = rs232;
        this.onUpdate = onUpdate;
    }

    /**
     * Write settings for the update rates of some of the currently-supported
     * rates. Not everything is currently supported! This may need extension if
     * you want to do more with the UM7LT.
     *
     * @param quaternion_rate how often to update the quaternion registers.
     * (Specified in 0-255 Hz)
     * @param euler_rate how often to update the Euler angle registers.
     * (Specified in 0-255 Hz)
     * @param position_rate how often to update the position registers.
     * (Specified in 0-255 Hz)
     * @param velocity_rate how often to update the velocity registers.
     * (Specified in 0-255 Hz)
     * @param health_rate_step how often to update the health register.
     * (Specification is not in Hertz. See the UM7LT manual.)
     * @throws IOException if the setting cannot be written due to an IO
     * Exception during communication.
     */
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

    /**
     * Handle up to the specified number of packets from the RS232 input.
     *
     * @param count the maximum number of packets to handle before returning.
     * @throws IOException if an IO Exception occurs during processing.
     */
    public void handleRS232Input(int count) throws IOException {
        byte[] activeBuffer = new byte[4096];
        int from = 0, to = 0;
        while (count-- > 0) {
            int consumed = handlePacket(activeBuffer, from, to);
            if (consumed == 0) { // need more data
                if (from != 0 && to >= activeBuffer.length - 64) { // nearing
                                                                   // the end,
                                                                   // or at the
                                                                   // end -
                                                                   // shift
                                                                   // earlier.
                    System.arraycopy(activeBuffer, from, activeBuffer, 0, to - from);
                    to -= from;
                    from = 0;
                }
                if (activeBuffer.length == to) {
                    // still no matched packet...?
                    logger.warn("RS232 input buffer overflow, somehow? Resetting buffer.");
                    from = to = 0;
                }
                byte[] gotten = rs232.readBlocking(activeBuffer.length - to);
                if (gotten.length > activeBuffer.length - to) {
                    throw new RuntimeException("RS232 returned more data than expected: " + gotten.length + " > " + activeBuffer.length + " - " + to);
                }
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

    // Returns the number of consumed bytes, or zero if packet needs more data
    // to be valid.
    private int handlePacket(byte[] bytes, int from, int to) {
        // TODO: Check bounds on To.
        if (to - from < 6) { // no way for any valid packets to be ready
            return 0;
        }
        try {
            if (bytes[from] == '$' && !treatNMEAAsErroneous) {
                int end = NMEA.getPacketEnd(bytes, from, to);
                if (end != -1) {
                    correctNMEAPackets++;
                    handleNMEA(bytes, from, end);
                    return end - from;
                } else {
                    return 0;
                }
            } else if (bytes[from] == 's' && bytes[from + 1] == 'n' && bytes[from + 2] == 'p') {
                correctBinaryPackets++;
                return handleBinary(bytes, from, to);
            } else {
                throw new IOException("Invalid packet that starts with bytes " + ByteFiddling.toHex(bytes, from, Math.min(to, from + 8)));
            }
        } catch (IOException ex) {
            logger.warn("UM7 message handling failed - attempting to reset state", ex);
        }
        incorrectPackets++;
        int possibleStartNMEA = ByteFiddling.indexOf(bytes, from + 1, to, (byte) '$');
        int possibleStartBinary = ByteFiddling.indexOf(bytes, from + 1, to, (byte) 's');
        if (possibleStartBinary != -1 && (possibleStartNMEA == -1 || possibleStartBinary < possibleStartNMEA)) {
            logger.debug("Skipping " + (possibleStartBinary - from) + " bytes to Binary.");
            return possibleStartBinary - from; // skip until the start
        } else if (possibleStartNMEA != -1 && !treatNMEAAsErroneous) {
            logger.debug("Skipping " + (possibleStartNMEA - from) + " bytes to NMEA.");
            return possibleStartNMEA - from; // skip until the start
        } else {
            logger.debug("Skipping " + (to - from) + " bytes to end (" + correctBinaryPackets + "/" + correctNMEAPackets + "/" + incorrectPackets + ")");
            return to - from; // everything's bad. skip it all.
        }
    }

    // Returns the number of consumed bytes, or zero if packet needs more data
    // to be valid.
    private int handleBinary(byte[] bin, int from, int to) throws IOException {
        if (to - from < 7) {
            return 0;
        }
        from += 3; // 'snp' has already been checked
        byte packet_type = bin[from];
        int address = bin[from + 1] & 0xFF;
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
            int nextUpdateId = lastUpdateId + 1;
            for (int i = 0; i < data_count; i++) {
                int regid = address + i - DREG_BASE;
                if (regid >= 0 && regid < DREG_LAST - DREG_BASE) {
                    dregs[regid] = data[i];
                    dregsUpdateAt[regid] = nextUpdateId;
                }
            }
            lastUpdateId = nextUpdateId;
            lastUpdateTime = Time.currentTimeMillis();
            onUpdate.event();
        } else if (address == 0xAA) {
            logger.info("UM7LT firmware revision: " + new String(new char[] { (char) ((data[0] >> 24) & 0xFF), (char) ((data[0] >> 16) & 0xFF), (char) ((data[0] >> 8) & 0xFF), (char) (data[0] & 0xFF) }));
        } else if (address == 0xAD) {
            logger.debug("UM7LT gyro calibrating...");
        } else if (address == 0xC) {
            logger.debug("UM7LT gyro calibration updated.");
        } else if (address != 0) {
            logger.trace("UNHANDLED BINARY MESSAGE " + Integer.toHexString(address & 0xFF) + " [" + is_hidden + ":" + is_command_failed + "]");
        }
        return 2 + 4 * data_count + 2 + 3; // two for checksum, three for the
                                           // 'snp' that was stripped out.
    }

    /**
     * Command the UM7LT to zero the Gyroscope.
     *
     * @throws IOException if the command could not be sent.
     */
    public void zeroGyros() throws IOException {
        doReadOperation((byte) 0xAD);
    }

    /**
     * Command the UM7LT to read and report a certain register.
     *
     * @param address the register address to read.
     *
     * @throws IOException if the command could not be sent.
     */
    public void doReadOperation(byte address) throws IOException {
        sendWithChecksum(new byte[] { 's', 'n', 'p', 0x00, address, 0, 0 });
    }

    /**
     * Command the UM7LT to read and report a series of registers.
     *
     * @param address the register address to start reading at.
     * @param count the number of registers to read.
     *
     * @throws IOException if the command could not be sent.
     */
    public void doBatchReadOperation(byte address, int count) throws IOException {
        if (count < 1 || count > 15) {
            // don't allow zero - why would we?
            throw new IllegalArgumentException("Bad count in encodeBatchReadOperation: must be in [1, 15]");
        }
        sendWithChecksum(new byte[] { 's', 'n', 'p', (byte) (0x40 | (count << 2)), address, 0, 0 });
    }

    /**
     * Command the UM7LT to modify a certain register.
     *
     * @param address the register address to read.
     * @param value the new value to contain.
     *
     * @throws IOException if the command could not be sent.
     */
    public void doWriteOperation(byte address, int value) throws IOException {
        sendWithChecksum(new byte[] { 's', 'n', 'p', (byte) 0x80, address, (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value, 0, 0 });
    }

    /**
     * Command the UM7LT to modify a series of registers.
     *
     * @param address the register address to start writing at.
     * @param values the array of values to write, starting at the address.
     *
     * @throws IOException if the command could not be sent.
     */
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

    private void handleNMEA(byte[] nmea, int from, int to) {
        logger.trace("UM7LT NMEA received: " + ByteFiddling.parseASCII(nmea, from, to));
    }

    /**
     * Dump serial data until the buffer is emptied, at least temporarily.
     *
     * @throws IOException if a communication error occurs while dumping serial
     * data.
     */
    public void dumpSerialData() throws IOException {
        while (this.rs232.readNonblocking(1024).length > 0) {
            logger.trace("dumping...");
        }
    }
}

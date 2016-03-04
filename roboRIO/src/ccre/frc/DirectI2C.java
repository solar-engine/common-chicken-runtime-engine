/*
 * Copyright 2016 Cel Skeggs
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
package ccre.frc;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.hal.I2CJNI;

class DirectI2C {
    public static final byte PORT_ONBOARD = 0, PORT_MXP = 1;

    private static final int MAX_I2C_LENGTH = 256; // based on size of byte

    private static final ThreadLocal<ByteBuffer> oneByteBuffer = new ThreadLocal<ByteBuffer>() {
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocateDirect(1);
        };
    };

    private static final int[] port_uses = new int[2];

    private static void checkPort(byte port) {
        if (port != PORT_ONBOARD && port != PORT_MXP) {
            throw new IllegalArgumentException("Invalid I2C port: " + port);
        }
    }

    public static synchronized void init(byte port) {
        checkPort(port);
        if (port_uses[port]++ == 0) {
            I2CJNI.i2CInitialize(port);
        }
    }

    public static synchronized void free(byte port) {
        checkPort(port);
        if (--port_uses[port] == 0) {
            I2CJNI.i2CClose(port);
        }
    }

    // return true on success; false on failure
    public static boolean transactUnsafe(byte port, byte address, ByteBuffer send, int sendLen, ByteBuffer recv, int recvLen) {
        checkPort(port);
        if (!send.isDirect() || !recv.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer is not direct!");
        }
        if (sendLen > send.capacity() || recvLen > recv.capacity()) {
            throw new IllegalArgumentException("Length is longer than buffer!");
        }
        if (sendLen >= MAX_I2C_LENGTH || recvLen >= MAX_I2C_LENGTH) {
            throw new IllegalArgumentException("Attempting to send/receive too much data to I2C port: " + sendLen + "/" + recvLen + " (max is " + MAX_I2C_LENGTH + " each way)");
        }
        // All of the transaction, read, and write functions are thin wrappers
        // around ioctl with I2C_RWDR
        return I2CJNI.i2CTransaction(port, address, send, (byte) sendLen, recv, (byte) recvLen) == 0;
    }

    public static void transactSafe(byte port, byte address, ByteBuffer send, int sendLen, ByteBuffer recv, int recvLen) throws IOException {
        if (!transactUnsafe(port, address, send, sendLen, recv, recvLen)) {
            throw new IOException("I2C transaction failure with " + port + "." + address);
        }
    }

    private static final ByteBuffer nobytes = ByteBuffer.allocateDirect(0);

    // no data; just see if anything responds. false on failure/abort.
    public static boolean query(byte port, byte address) {
        return transactUnsafe(port, address, nobytes, 0, nobytes, 0);
    }

    public static boolean writeUnsafe(byte port, byte address, ByteBuffer buf, int len) {
        if (!buf.isDirect()) {
            throw new IllegalArgumentException("Buffer must be direct!");
        }
        if (buf.capacity() < len) {
            throw new IllegalArgumentException("Buffer is not large enough!");
        }
        if (len >= MAX_I2C_LENGTH) {
            throw new IllegalArgumentException("Size is too long:" + len);
        }
        return I2CJNI.i2CWrite(port, address, buf, (byte) len) == 0;
    }

    public static void writeSafe(byte port, byte address, ByteBuffer buf, int len) throws IOException {
        if (!writeUnsafe(port, address, buf, len)) {
            throw new IOException("I2C write failure with " + port + "." + address);
        }
    }

    public static boolean readUnsafe(byte port, byte address, byte registerAddress, ByteBuffer buf, int len) {
        if (!buf.isDirect()) {
            throw new IllegalArgumentException("Buffer must be direct!");
        }
        if (len < 1) {
            throw new IllegalArgumentException("Read transaction must read at least one byte.");
        }
        if (buf.capacity() < len) {
            throw new IllegalArgumentException("Buffer is not large enough!");
        }
        if (len >= MAX_I2C_LENGTH) {
            throw new IllegalArgumentException("Size is too long:" + len);
        }
        ByteBuffer send = oneByteBuffer.get();
        send.clear();
        send.put(registerAddress);
        return transactUnsafe(port, address, send, 1, buf, len);
    }

    public static void readSafe(byte port, byte address, byte registerAddress, ByteBuffer buf, int len) throws IOException {
        if (!readUnsafe(port, address, registerAddress, buf, len)) {
            throw new IOException("I2C read failure with " + port + "." + address);
        }
    }

    public static boolean readOnlyUnsafe(byte port, byte address, ByteBuffer buf, int len) {
        if (!buf.isDirect()) {
            throw new IllegalArgumentException("Buffer must be direct!");
        }
        if (len < 1) {
            throw new IllegalArgumentException("Read transaction must read at least one byte.");
        }
        if (buf.capacity() < len) {
            throw new IllegalArgumentException("Buffer is not large enough!");
        }
        if (len >= MAX_I2C_LENGTH) {
            throw new IllegalArgumentException("Size is too long:" + len);
        }
        return I2CJNI.i2CRead(port, address, buf, (byte) len) == 0;
    }

    public static void readOnlySafe(byte port, byte address, ByteBuffer buf, int len) throws IOException {
        if (!readOnlyUnsafe(port, address, buf, len)) {
            throw new IOException("I2C read failure with " + port + "." + address);
        }
    }
}

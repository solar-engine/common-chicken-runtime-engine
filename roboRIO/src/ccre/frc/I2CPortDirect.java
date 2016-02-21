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

import ccre.bus.I2CIO;

final class I2CPortDirect implements I2CIO {

    private final byte port, address;
    private boolean closed;
    private final ByteBuffer buf = ByteBuffer.allocateDirect(2);

    I2CPortDirect(byte port, int address) {
        this.port = port;
        if (address < 0 || address >= 256) {
            throw new IllegalArgumentException("Invalid address (not a byte): " + address);
        }
        this.address = (byte) address;
        DirectI2C.init(port);
    }

    @Override
    public boolean query() {
        if (closed) {
            return false;
        }
        return DirectI2C.query(port, address);
    }

    @Override
    public void transact(ByteBuffer send, int sendLen, ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("I2C port closed");
        }
        DirectI2C.transactSafe(port, address, send, sendLen, recv, recvLen);
    }

    @Override
    public void write(ByteBuffer send, int sendLen) throws IOException {
        if (closed) {
            throw new IOException("I2C port closed");
        }
        DirectI2C.writeSafe(port, address, send, sendLen);
    }

    @Override
    public void write(byte register, byte data) throws IOException {
        if (closed) {
            throw new IOException("I2C port closed");
        }
        synchronized (buf) {
            buf.clear();
            buf.put(register);
            buf.put(data);
            this.write(buf, 2);
        }
    }

    @Override
    public void read(byte register, ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("I2C port closed");
        }
        DirectI2C.readSafe(port, address, register, recv, recvLen);
    }

    @Override
    public byte read(byte register) throws IOException {
        if (closed) {
            throw new IOException("I2C port closed");
        }
        synchronized (buf) {
            buf.clear();
            this.read(register, buf, 1);
            return buf.get();
        }
    }

    @Override
    public void readOnly(ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("I2C port closed");
        }
        DirectI2C.readOnlySafe(port, address, recv, recvLen);
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        DirectI2C.free(port);
    }
}

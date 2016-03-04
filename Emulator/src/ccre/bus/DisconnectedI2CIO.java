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
package ccre.bus;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An emulation of a completely disconnected I2C port. Nothing comes in, and
 * everything going out is ignored.
 *
 * @author skeggsc
 */
public class DisconnectedI2CIO implements I2CIO {

    private boolean closed = false;

    @Override
    public synchronized void close() throws IOException {
        // do nothing.
        closed = true;
    }

    @Override
    public boolean query() {
        return false;
    }

    @Override
    public void transact(ByteBuffer send, int sendLen, ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedI2CIO closed.");
        }
        throw new IOException("I2C transaction failure: DisconnectedI2CIO");
    }

    @Override
    public void write(ByteBuffer send, int sendLen) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedI2CIO closed.");
        }
        throw new IOException("I2C write failure: DisconnectedI2CIO");
    }

    @Override
    public void write(byte register, byte data) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedI2CIO closed.");
        }
        throw new IOException("I2C write failure: DisconnectedI2CIO");
    }

    @Override
    public void read(byte register, ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedI2CIO closed.");
        }
        throw new IOException("I2C read failure: DisconnectedI2CIO");
    }

    @Override
    public byte read(byte register) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedI2CIO closed.");
        }
        throw new IOException("I2C read failure: DisconnectedI2CIO");
    }

    @Override
    public void readOnly(ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedI2CIO closed.");
        }
        throw new IOException("I2C readOnly failure: DisconnectedI2CIO");
    }
}

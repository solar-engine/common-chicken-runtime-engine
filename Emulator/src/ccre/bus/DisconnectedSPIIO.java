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
public class DisconnectedSPIIO implements SPIIO {

    private boolean closed = false;

    @Override
    public synchronized void close() throws IOException {
        // do nothing.
        closed = true;
    }

    @Override
    public int transact(ByteBuffer send, ByteBuffer recv, int length) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSPIIO closed.");
        }
        return 0;
    }

    @Override
    public int write(ByteBuffer send, int sendLen) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSPIIO closed.");
        }
        return 0;
    }

    @Override
    public int read(ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSPIIO closed.");
        }
        return 0;
    }

    @Override
    public int readInitiated(ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSPIIO closed.");
        }
        return 0;
    }
}

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

import ccre.bus.SPIIO;

final class SPIPortDirect implements SPIIO {

    private final byte port;
    private boolean closed;
    // a buffer of zeroed bytes - do not modify values
    private final ByteBuffer zeroed = ByteBuffer.allocateDirect(DirectSPI.MAX_SPI_LENGTH);

    SPIPortDirect(byte port, int hertz, boolean isMSB, boolean dataOnFalling, boolean clockActiveLow, boolean chipSelectActiveLow) {
        this.port = port;
        DirectSPI.init(port);
        DirectSPI.configure(port, hertz, isMSB, dataOnFalling, clockActiveLow, chipSelectActiveLow);
    }

    @Override
    public int transact(ByteBuffer send, ByteBuffer recv, int length) throws IOException {
        if (closed) {
            throw new IOException("SPI port closed");
        }
        return DirectSPI.transact(port, send, recv, length);
    }

    @Override
    public int write(ByteBuffer send, int sendLen) throws IOException {
        if (closed) {
            throw new IOException("SPI port closed");
        }
        return DirectSPI.write(port, send, sendLen);
    }

    @Override
    public int read(ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("SPI port closed");
        }
        return DirectSPI.read(port, recv, recvLen);
    }

    @Override
    public int readInitiated(ByteBuffer recv, int recvLen) throws IOException {
        if (closed) {
            throw new IOException("SPI port closed");
        }
        return DirectSPI.transact(port, zeroed, recv, recvLen);
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        DirectSPI.free(port);
    }
}

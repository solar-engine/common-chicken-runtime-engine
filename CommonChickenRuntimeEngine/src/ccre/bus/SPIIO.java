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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface SPIIO extends Closeable {

    public int transact(ByteBuffer send, ByteBuffer recv, int length) throws IOException;

    public int write(ByteBuffer send, int sendLen) throws IOException;

    public default void writeExact(ByteBuffer buf, int count) throws IOException {
        if (write(buf, count) != count) {
            throw new IOException("SPI write failure: not enough bytes written");
        }
    }

    public int read(ByteBuffer recv, int recvLen) throws IOException;

    public default void readExact(ByteBuffer buf, int count) throws IOException {
        if (read(buf, count) != count) {
            throw new IOException("SPI read failure: not enough bytes read");
        }
    }

    public int readInitiated(ByteBuffer recv, int recvLen) throws IOException;

    public default void readInitiatedExact(ByteBuffer buf, int count) throws IOException {
        if (readInitiated(buf, count) != count) {
            throw new IOException("SPI read failure: not enough bytes read");
        }
    }
}

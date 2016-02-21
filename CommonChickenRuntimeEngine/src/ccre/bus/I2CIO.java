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

public interface I2CIO extends Closeable {
    // implementation includes a device address

    public boolean query();

    public void transact(ByteBuffer send, int sendLen, ByteBuffer recv, int recvLen) throws IOException;

    public void write(ByteBuffer send, int sendLen) throws IOException;

    public void write(byte register, byte data) throws IOException;

    public void read(byte register, ByteBuffer recv, int recvLen) throws IOException;

    public byte read(byte register) throws IOException;

    public void readOnly(ByteBuffer recv, int recvLen) throws IOException;
}

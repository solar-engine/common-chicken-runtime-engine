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

import java.nio.ByteBuffer;

class ByteBufferPool {

    private final ThreadLocal<ByteBuffer> buf = new ThreadLocal<ByteBuffer>();

    public ByteBuffer get(int capacity) {
        ByteBuffer buf = this.buf.get();
        if (buf == null || buf.capacity() < capacity) {
            buf = ByteBuffer.allocateDirect(capacity);
            this.buf.set(buf);
        }
        buf.clear();
        return buf;
    }

    public ByteBuffer getZeroed(int capacity) {
        ByteBuffer buf = get(capacity);
        for (int i = 0; i < buf.capacity(); i++) {
            buf.put(i, (byte) 0);
        }
        return buf;
    }
}

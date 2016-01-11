/*
 * Copyright 2015-2016 Colby Skeggs
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
 *
 *
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.frc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

class Common {

    private static ThreadLocal<IntBuffer> directBuffer = new ThreadLocal<IntBuffer>() {
        @Override
        protected IntBuffer initialValue() {
            return allocateInt();
        }
    };

    public static IntBuffer getSharedBuffer() {
        IntBuffer out = directBuffer.get();
        out.put(0, 0);
        return out;
    }

    public static IntBuffer allocateInt() {
        ByteBuffer status = ByteBuffer.allocateDirect(4);
        status.order(ByteOrder.LITTLE_ENDIAN);
        return status.asIntBuffer();
    }

    public static LongBuffer allocateLong() {
        ByteBuffer status = ByteBuffer.allocateDirect(8);
        status.order(ByteOrder.LITTLE_ENDIAN);
        return status.asLongBuffer();
    }
}

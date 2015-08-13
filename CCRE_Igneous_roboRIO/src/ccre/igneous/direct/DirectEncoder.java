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
 *
 *
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.igneous.direct;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.EncoderJNI;

class DirectEncoder {
    public static ByteBuffer init(int channelA, int channelB, boolean reverse) {
        DirectDigital.init(channelA, true);
        DirectDigital.init(channelB, true);
        IntBuffer status = Common.getCheckBuffer();
        IntBuffer indexS = Common.allocateInt();
        ByteBuffer encoder = EncoderJNI.initializeEncoder((byte) 0, channelA, (byte) 0, (byte) 0, channelB, (byte) 0, (byte) (reverse ? 1 : 0), indexS, status);
        Common.check(status);
        indexS.get(0); // index number unused
        return encoder;
    }

    public static void free(int channelA, int channelB, ByteBuffer port) {
        DirectDigital.free(channelA);
        DirectDigital.free(channelB);

        IntBuffer status = Common.getCheckBuffer();
        EncoderJNI.freeEncoder(port, status);
        Common.check(status);
    }

    public static void reset(ByteBuffer port) {
        IntBuffer status = Common.getCheckBuffer();
        EncoderJNI.resetEncoder(port, status); // just FPGA errors
        Common.check(status);
    }

    public static int getRaw(ByteBuffer port) {
        IntBuffer status = Common.getCheckBuffer();
        int value = EncoderJNI.getEncoder(port, status); // just FPGA errors
        Common.check(status);
        return value;
    }

    public static float get(ByteBuffer port) {
        return getRaw(port) / 4;
    }
}

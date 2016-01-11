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

import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.EncoderJNI;

class DirectEncoder {
    public static long init(int channelA, int channelB, boolean reverse) {
        DirectDigital.init(channelA, true);
        DirectDigital.init(channelB, true);

        IntBuffer indexS = Common.getSharedBuffer();
        long encoder = EncoderJNI.initializeEncoder((byte) 0, channelA, false, (byte) 0, channelB, false, reverse, indexS);
        indexS.get(0); // index number unused
        return encoder;
    }

    public static void free(int channelA, int channelB, long port) {
        DirectDigital.free(channelA);
        DirectDigital.free(channelB);

        EncoderJNI.freeEncoder(port);
    }

    public static void reset(long port) {
        EncoderJNI.resetEncoder(port);
    }

    public static int getRaw(long port) {
        return EncoderJNI.getEncoder(port);
    }

    public static float get(long port) {
        return getRaw(port) / 4f;
    }
}

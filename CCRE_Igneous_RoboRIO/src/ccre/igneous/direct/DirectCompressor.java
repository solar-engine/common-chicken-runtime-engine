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

import edu.wpi.first.wpilibj.hal.CompressorJNI;

class DirectCompressor {
    public static ByteBuffer init(int pcmID) {
        return CompressorJNI.initializeCompressor((byte) pcmID);
    }

    public static void setClosedLoop(ByteBuffer pcm, boolean on) {
        IntBuffer status = Common.getCheckBuffer();
        CompressorJNI.setClosedLoopControl(pcm, on, status); // errors when not yet initialized, so should be fine since init will always be called.
        Common.check(status);
    }

    public static boolean getPressureSwitch(ByteBuffer pcm) {
        IntBuffer status = Common.getCheckBuffer();
        boolean swt = CompressorJNI.getPressureSwitch(pcm, status); // TODO: errors if timed out
        Common.check(status);
        return swt;
    }

    public static boolean getCompressorRunning(ByteBuffer pcm) {
        IntBuffer status = Common.getCheckBuffer();
        boolean on = CompressorJNI.getCompressor(pcm, status); // TODO: errors if timed out.
        Common.check(status);
        return on;
    }

    public static float getCompressorCurrent(ByteBuffer pcm) {
        IntBuffer status = Common.getCheckBuffer();
        float current = CompressorJNI.getCompressorCurrent(pcm, status); // TODO: errors if timed out
        Common.check(status);
        return current;
    }
}

/*
 * Copyright 2015-2016 Cel Skeggs
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

import edu.wpi.first.wpilibj.hal.CompressorJNI;

class DirectCompressor {
    public static long init(int pcmID) {
        return CompressorJNI.initializeCompressor((byte) pcmID);
    }

    public static void setClosedLoop(long pcm, boolean on) {
        // errors when not yet initialized, so should be fine since init will
        // always be called.
        CompressorJNI.setClosedLoopControl(pcm, on);
    }

    public static boolean getPressureSwitch(long pcm) {
        // TODO: errors if timed out
        return CompressorJNI.getPressureSwitch(pcm);
    }

    public static boolean getCompressorRunning(long pcm) {
        // TODO: errors if timed out.
        return CompressorJNI.getCompressor(pcm);
    }

    public static float getCompressorCurrent(long pcm) {
        // TODO: errors if timed out
        return CompressorJNI.getCompressorCurrent(pcm);
    }
}

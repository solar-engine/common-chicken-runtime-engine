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

import edu.wpi.first.wpilibj.hal.PDPJNI;

class DirectPDP {
    private static final int CHANNEL_NUM = 16;

    public static void checkChannel(int channel) {
        if (channel < 0 || channel >= CHANNEL_NUM) {
            throw new RuntimeException("Invalid PDP channel: " + channel);
        }
    }

    // TODO: if multiple PDPs don't work, I've probably forgotten to update the
    // WPILib binaries.

    public static float getCurrent(int channel, int module) {
        checkChannel(channel);
        // TODO: avoid timeout errors
        return (float) PDPJNI.getPDPChannelCurrent((byte) channel, module);
    }

    public static float getTotalCurrent(int module) {
        return (float) PDPJNI.getPDPTotalCurrent(module);
    }

    public static float getVoltage(int module) {
        // TODO: avoid timeout errors
        return (float) PDPJNI.getPDPVoltage(module);
    }
}

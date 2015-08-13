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

import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.PDPJNI;

class DirectPDP {
    private static final int CHANNEL_NUM = 16;

    public static void checkChannel(int channel) {
        if (channel < 0 || channel >= CHANNEL_NUM) {
            throw new RuntimeException("Invalid PDP channel: " + channel);
        }
    }

    public static float getCurrent(int channel) {
        checkChannel(channel);
        IntBuffer status = Common.getCheckBuffer();
        double current = PDPJNI.getPDPChannelCurrent((byte) channel, status); // errors are timeouts and invalid channel IDs. TODO: avoid timeout errors
        Common.check(status);
        return (float) current;
    }

    public static float getVoltage() {
        IntBuffer status = Common.getCheckBuffer();
        double voltage = PDPJNI.getPDPVoltage(status); // errors are timeouts. TODO: avoid timeouts errors
        Common.check(status);
        return (float) voltage;
    }
}

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
package ccre.frc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.SolenoidJNI;

class DirectSolenoid {
    public static final int MODULE_COUNT = 63, PORT_COUNT = 8;
    private static final ByteBuffer[][] ports = new ByteBuffer[MODULE_COUNT][PORT_COUNT];

    public static ByteBuffer init(int module, int channel) {
        if (module < 0 || module >= MODULE_COUNT || channel < 0 || channel >= PORT_COUNT) {
            throw new RuntimeException("Solenoid ID invalid: " + module + ":" + channel);
        }

        if (ports[module][channel] == null) {
            IntBuffer status = Common.getCheckBuffer();
            ports[module][channel] = SolenoidJNI.initializeSolenoidPort(SolenoidJNI.getPortWithModule((byte) module, (byte) channel), status);
            Common.check(status);
        }

        return ports[module][channel];
    }

    public static void set(ByteBuffer port, boolean value) {
        if (port == null) {
            throw new NullPointerException();
        }
        IntBuffer status = Common.getCheckBuffer();
        SolenoidJNI.setSolenoid(port, (byte) (value ? 1 : 0), status); // uhh... no errors...
        Common.check(status);
    }

    public static boolean isBlacklisted(ByteBuffer port, int channel) {
        if (port == null) {
            throw new NullPointerException();
        }
        IntBuffer status = Common.getCheckBuffer();
        int value = SolenoidJNI.getPCMSolenoidBlackList(port, status); // TODO: handle timeout errors
        Common.check(status);
        return (value & (1 << channel)) != 0;
    }
}

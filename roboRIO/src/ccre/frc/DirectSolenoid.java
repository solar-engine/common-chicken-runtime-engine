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

import edu.wpi.first.wpilibj.hal.SolenoidJNI;

class DirectSolenoid {
    public static final int MODULE_COUNT = 63, PORT_COUNT = 8;
    private static final long[][] ports = new long[MODULE_COUNT][PORT_COUNT];

    public static long init(int module, int channel) {
        if (module < 0 || module >= MODULE_COUNT || channel < 0 || channel >= PORT_COUNT) {
            throw new RuntimeException("Solenoid ID invalid: " + module + ":" + channel);
        }

        if (ports[module][channel] == 0) {
            ports[module][channel] = SolenoidJNI.initializeSolenoidPort(SolenoidJNI.getPortWithModule((byte) module, (byte) channel));
        }

        return ports[module][channel];
    }

    public static void set(long port, boolean value) {
        if (port == 0) {
            throw new NullPointerException();
        }
        // uhh... no errors...
        SolenoidJNI.setSolenoid(port, value);
    }

    public static boolean isBlacklisted(long port, int channel) {
        if (port == 0) {
            throw new NullPointerException();
        }
        // TODO: handle timeout errors
        int value = SolenoidJNI.getPCMSolenoidBlackList(port);
        return (value & (1 << channel)) != 0;
    }
}

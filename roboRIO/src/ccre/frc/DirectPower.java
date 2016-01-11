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

import edu.wpi.first.wpilibj.hal.PowerJNI;

class DirectPower {

    public static void init() {
        // mitigates any potential first-run errors from the FPGA.
        getBatteryVoltage();
    }

    public static float getBatteryVoltage() {
        // just FPGA errors - maybe FPGA startup errors, but that's handled by
        // init().
        return PowerJNI.getVinVoltage();
    }

    public static float readChannelVoltage(int powerChannel) {
        switch (powerChannel) {
        case FRC.POWER_CHANNEL_BATTERY:
            return PowerJNI.getVinVoltage();
        case FRC.POWER_CHANNEL_3V3:
            return PowerJNI.getUserVoltage3V3();
        case FRC.POWER_CHANNEL_5V:
            return PowerJNI.getUserVoltage5V();
        case FRC.POWER_CHANNEL_6V:
            return PowerJNI.getUserVoltage6V();
        default:
            return -1;
        }
    }

    public static float readChannelCurrent(int powerChannel) {
        switch (powerChannel) {
        case FRC.POWER_CHANNEL_BATTERY:
            return PowerJNI.getVinCurrent();
        case FRC.POWER_CHANNEL_3V3:
            return PowerJNI.getUserCurrent3V3();
        case FRC.POWER_CHANNEL_5V:
            return PowerJNI.getUserCurrent5V();
        case FRC.POWER_CHANNEL_6V:
            return PowerJNI.getUserCurrent6V();
        default:
            return -1;
        }
    }

    public static boolean readChannelEnabled(int powerChannel) {
        switch (powerChannel) {
        case FRC.POWER_CHANNEL_BATTERY:
            return true;
        case FRC.POWER_CHANNEL_3V3:
            return PowerJNI.getUserActive3V3();
        case FRC.POWER_CHANNEL_5V:
            return PowerJNI.getUserActive5V();
        case FRC.POWER_CHANNEL_6V:
            return PowerJNI.getUserActive6V();
        default:
            return false;
        }
    }
}

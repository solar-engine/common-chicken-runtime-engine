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

import edu.wpi.first.wpilibj.hal.PowerJNI;

class DirectPower {

    public static void init() {
        getBatteryVoltage(); // mitigates any potential first-run errors from the FPGA.
    }

    public static float getBatteryVoltage() {
        IntBuffer status = Common.getCheckBuffer();
        float voltage = PowerJNI.getVinVoltage(status); // just FPGA errors - maybe FPGA startup errors, but that's handled by init().
        Common.check(status);
        return voltage;
    }
}

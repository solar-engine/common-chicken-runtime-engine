/*
 * Copyright 2013-2014 Colby Skeggs
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
 */
package ccre.igneous;

import ccre.chan.BooleanOutput;
import ccre.device.DeviceException;
import ccre.device.DeviceHandle;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * Used in IgneousLauncherImpl as the implementation of a Solenoid shared to a
 * DeviceRegistry.
 *
 * @author skeggsc
 */
class SolenoidHandle extends DeviceHandle implements BooleanOutput {

    private final int id;
    private Solenoid sol;

    public SolenoidHandle(int sol) {
        this.id = sol;
    }

    public Class getPrimaryDeviceType() {
        return BooleanOutput.class;
    }

    public Object open() throws DeviceException {
        synchronized (this) {
            if (sol != null) {
                throw new DeviceException("Already allocated!");
            }
            sol = new Solenoid(id);
        }
        return this;
    }

    public void close(Object type) throws DeviceException {
        if (type != this) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (sol == null) {
                return;
            }
            sol.free();
            sol = null;
        }
    }

    public void writeValue(boolean bln) {
        if (sol != null) {
            sol.set(bln);
        }
    }
}

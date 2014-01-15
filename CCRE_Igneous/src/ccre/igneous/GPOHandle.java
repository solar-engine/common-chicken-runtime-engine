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
import edu.wpi.first.wpilibj.DigitalOutput;

/**
 * Used in IgneousLauncherImpl as the implementation of a GPIO channel shared to
 * a DeviceRegistry.
 *
 * @author skeggsc
 */
class GPOHandle extends DeviceHandle implements BooleanOutput {

    private final int dgt;
    private DigitalOutput dout;

    public GPOHandle(int dgt) {
        this.dgt = dgt;
    }

    public Class getPrimaryDeviceType() {
        return BooleanOutput.class;
    }

    public Object open() throws DeviceException {
        synchronized (this) {
            if (dout != null) {
                throw new DeviceException("Already allocated!");
            }
            dout = new DigitalOutput(dgt);
        }
        return this;
    }

    public void close(Object type) throws DeviceException {
        if (type != this) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (dout == null) {
                return;
            }
            dout.free();
            dout = null;
        }
    }

    public void writeValue(boolean bln) {
        if (dout != null) {
            dout.set(bln);
        }
    }
}

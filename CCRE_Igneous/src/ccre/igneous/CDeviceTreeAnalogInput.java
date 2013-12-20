/*
 * Copyright 2013 Colby Skeggs
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

import ccre.chan.FloatInputPoll;
import ccre.device.DeviceException;
import ccre.device.DeviceHandle;
import edu.wpi.first.wpilibj.AnalogChannel;

/**
 * Used in IgneousLauncherImpl as the implementation of an analog input channel
 * shared to a DeviceTree.
 *
 * @author skeggsc
 */
class CDeviceTreeAnalogInput extends DeviceHandle implements FloatInputPoll {

    private final int alg;
    private AnalogChannel ain;

    public CDeviceTreeAnalogInput(int alg) {
        this.alg = alg;
    }

    public Class getPrimaryDeviceType() {
        return FloatInputPoll.class;
    }

    public Object open() throws DeviceException {
        synchronized (this) {
            if (ain != null) {
                throw new DeviceException("Already allocated!");
            }
            ain = new AnalogChannel(alg);
        }
        return this;
    }

    public void close(Object type) throws DeviceException {
        if (type != this) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (ain == null) {
                return;
            }
            ain.free();
            ain = null;
        }
    }

    public float readValue() {
        if (ain != null) {
            return (float) ain.getAverageVoltage();
        } else {
            return 0;
        }
    }
}

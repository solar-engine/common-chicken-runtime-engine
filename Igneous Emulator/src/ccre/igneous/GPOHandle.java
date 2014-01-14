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

/**
 * Used in IgneousLauncherImpl as the implementation of a GPIO channel shared to
 * a DeviceRegistry.
 *
 * @author skeggsc
 */
class GPOHandle extends DeviceHandle<BooleanOutput> {

    private final EmulatorForm form;
    private final int dgt;
    private BooleanOutput dout;

    public GPOHandle(EmulatorForm form, int dgt) {
        this.form = form;
        this.dgt = dgt;
    }

    @Override
    public Class<BooleanOutput> getPrimaryDeviceType() {
        return BooleanOutput.class;
    }

    @Override
    public BooleanOutput open() throws DeviceException {
        synchronized (this) {
            if (dout != null) {
                throw new DeviceException("Already allocated!");
            }
            dout = form.getDigitalOutput(dgt);
            return dout;
        }
    }

    @Override
    public void close(BooleanOutput type) throws DeviceException {
        if (type != null || type != dout) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (dout == null) {
                return;
            }
            form.freeDigitalOutput(dgt);
            dout = null;
        }
    }
}

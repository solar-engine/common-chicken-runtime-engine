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

import ccre.chan.BooleanInputPoll;
import ccre.device.DeviceException;
import ccre.device.DeviceHandle;

/**
 * Used in IgneousLauncherImpl as the implementation of a GPIO channel shared to
 * a DeviceRegistry.
 *
 * @author skeggsc
 */
class GPIHandle extends DeviceHandle<BooleanInputPoll> {

    private final int dgt;
    private BooleanInputPoll din;
    private final EmulatorForm form;

    public GPIHandle(EmulatorForm form, int dgt) {
        this.form = form;
        this.dgt = dgt;
    }

    @Override
    public Class<BooleanInputPoll> getPrimaryDeviceType() {
        return BooleanInputPoll.class;
    }

    @Override
    public BooleanInputPoll open() throws DeviceException {
        synchronized (this) {
            if (din != null) {
                throw new DeviceException("Already allocated!");
            }
            din = form.getDigitalInput(dgt);
            return din;
        }
    }

    @Override
    public void close(Object type) throws DeviceException {
        if (type != null || type != din) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (din == null) {
                return;
            }
            form.freeDigitalInput(dgt);
            din = null;
        }
    }
}

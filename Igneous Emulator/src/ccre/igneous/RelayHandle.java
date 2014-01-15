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
 * Used in IgneousLauncherImpl as the implementation of a Relay shared to a
 * DeviceRegistry.
 *
 * @author skeggsc
 */
class RelayHandle extends DeviceHandle<BooleanOutput> {

    private final int id;
    private BooleanOutput rel;
    private final boolean fwd;
    private final EmulatorForm form;

    public RelayHandle(EmulatorForm form, int rid, boolean forward) {
        this.id = rid;
        this.fwd = forward;
        this.form = form;
    }

    @Override
    public Class<BooleanOutput> getPrimaryDeviceType() {
        return BooleanOutput.class;
    }

    @Override
    public BooleanOutput open() throws DeviceException {
        synchronized (this) {
            if (rel != null) {
                throw new DeviceException("Already allocated!");
            }
            rel = fwd ? form.makeRelayForward(id) : form.makeRelayReverse(id);
            return rel;
        }
    }

    @Override
    public void close(BooleanOutput type) throws DeviceException {
        if (type != null || type != rel) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (rel == null) {
                return;
            }
            form.closeRelay(id, fwd);
            rel = null;
        }
    }
}

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

import ccre.chan.FloatOutput;
import ccre.device.DeviceException;
import ccre.device.DeviceHandle;

/**
 * Used in IgneousLauncherImpl as the implementation of a Speed Controller
 * shared to a DeviceRegistry.
 *
 * @author skeggsc
 */
class PWMHandle extends DeviceHandle<FloatOutput> {

    public static final byte VICTOR = 'V', TALON = 'T', JAGUAR = 'J', SERVO = 'S';

    PWMHandle(EmulatorForm form, int port, byte type) {
        this.form = form;
        this.type = type;
        this.port = port;
    }
    private final EmulatorForm form;
    private final byte type;
    private FloatOutput out;
    private final int port;

    @Override
    public Class<FloatOutput> getPrimaryDeviceType() {
        return FloatOutput.class;
    }

    @Override
    public FloatOutput open() throws DeviceException {
        synchronized (this) {
            if (out != null) {
                throw new DeviceException("Already allocated!");
            }
            switch (type) {
                case VICTOR:
                    out = form.getVictor(port);
                    break;
                case JAGUAR:
                    out = form.getJaguar(port);
                    break;
                case TALON:
                    out = form.getTalon(port);
                    break;
                case SERVO:
                    out = form.getServo(port, 0, 1);
                    break;
                default:
                    throw new DeviceException("Internal Error");
            }
            return out;
        }
    }

    @Override
    public void close(Object type) throws DeviceException {
        if (type != out || type == null) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (out == null) {
                return;
            }
            out = null;
            form.freeMotor(port);
        }
    }
}

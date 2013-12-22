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

import ccre.chan.FloatOutput;
import ccre.device.DeviceException;
import ccre.device.DeviceHandle;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;

/**
 * Used in IgneousLauncherImpl as the implementation of a Speed Controller
 * shared to a DeviceTree.
 *
 * @author skeggsc
 */
class CDeviceTreePWM extends DeviceHandle implements FloatOutput {

    public static final byte VICTOR = 'V', TALON = 'T', JAGUAR = 'J', SERVO = 'S';

    CDeviceTreePWM(int port, byte type) {
        this.type = type;
        this.port = port;
    }
    private final byte type;
    private PWM pwm;
    private PIDOutput pidOut;
    private final int port;

    public Class getPrimaryDeviceType() {
        return FloatOutput.class;
    }

    public Object open() throws DeviceException {
        synchronized (this) {
            if (pwm != null || pidOut != null) {
                throw new DeviceException("Already allocated!");
            }
            switch (type) {
                case VICTOR:
                    Victor v = new Victor(this.port);
                    pwm = v;
                    pidOut = v;
                    break;
                case JAGUAR:
                    Jaguar j = new Jaguar(this.port);
                    pwm = j;
                    pidOut = j;
                    break;
                case TALON:
                    Talon t = new Talon(this.port);
                    pwm = t;
                    pidOut = t;
                    break;
                case SERVO:
                    final Servo s = new Servo(this.port);
                    pwm = s;
                    pidOut = new PIDOutput() {
                        public void pidWrite(double output) {
                            s.set(output);
                        }
                    };
                    break;
                default:
                    throw new DeviceException("Internal Error");
            }
        }
        return this;
    }

    public void close(Object type) throws DeviceException {
        if (type != this) {
            throw new DeviceException("Bad target for close!");
        }
        synchronized (this) {
            if (pwm == null) {
                return;
            }
            pidOut = null;
            pwm.free();
            pwm = null;
        }
    }

    public void writeValue(float f) {
        PIDOutput po = pidOut;
        if (po != null) {
            po.pidWrite(f);
        }
    }
}

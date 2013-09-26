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

import ccre.chan.BooleanInputPoll;
import ccre.chan.FloatInputPoll;
import ccre.ctrl.ISimpleJoystick;
import edu.wpi.first.wpilibj.Joystick;

/**
 * An ISimpleJoystick implementation that allows reading from a joystick on the
 * driver station.
 *
 * @author skeggsc
 */
class CSimpleJoystick implements ISimpleJoystick {

    /**
     * The joystick object that is read from.
     */
    protected Joystick joy;

    /**
     * Create a CSimpleJoystick that reads from the specified joystick index.
     *
     * @param joystick the joystick ID, from 1 to 4, inclusive.
     */
    CSimpleJoystick(int joystick) {
        joy = new Joystick(joystick);
    }

    public FloatInputPoll getAxisChannel(final int axis) {
        return new FloatInputPoll() {
            public float readValue() {
                return (float) joy.getRawAxis(axis);
            }
        };
    }

    public FloatInputPoll getXChannel() {
        return new FloatInputPoll() {
            public float readValue() {
                return (float) joy.getX();
            }
        };
    }

    public FloatInputPoll getYChannel() {
        return new FloatInputPoll() {
            public float readValue() {
                return (float) joy.getY();
            }
        };
    }

    public BooleanInputPoll getButtonChannel(final int button) {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return joy.getRawButton(button);
            }
        };
    }
}

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

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.AbstractJoystickWithPOV;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.log.Logger;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.KinectStick;

/**
 * An IJoystick implementation that allows reading from a joystick on the driver
 * station.
 *
 * @author skeggsc
 */
final class CJoystick extends AbstractJoystickWithPOV {

    /**
     * The joystick object that is read from.
     */
    private final GenericHID joy;

    /**
     * Create a new CJoystick for a specific joystick ID.
     *
     * @param joystick the joystick ID
     */
    CJoystick(int joystick, EventInput check) {
        super(check);
        if (joystick == 5) {
            joy = new KinectStick(1);
        } else if (joystick == 6) {
            joy = new KinectStick(2);
        } else {
            joy = new Joystick(joystick);
        }
    }

    public FloatInputPoll getAxisChannel(final int axis) {
        return new FloatInputPoll() {
            public float get() {
                return (float) joy.getRawAxis(axis);
            }
        };
    }

    public BooleanInputPoll getButtonChannel(final int button) {
        return new BooleanInputPoll() {
            public boolean get() {
                return joy.getRawButton(button);
            }
        };
    }

    public BooleanInputPoll isPOVPressed(int id) {
        Logger.warning("POVs not supported on the cRIO.");
        return BooleanMixing.alwaysFalse;
    }

    public FloatInputPoll getPOVAngle(int id) {
        Logger.warning("POVs not supported on the cRIO.");
        return FloatMixing.always(0);
    }
}

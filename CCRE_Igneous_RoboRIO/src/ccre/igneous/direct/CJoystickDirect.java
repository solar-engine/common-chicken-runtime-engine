/*
 * Copyright 2013-2015 Colby Skeggs
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
package ccre.igneous.direct;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatStatus;
import ccre.ctrl.AbstractJoystick;
import ccre.ctrl.IJoystick;
import ccre.ctrl.IJoystickWithPOV;

/**
 * An IJoystick implementation that allows reading from a joystick on the driver
 * station.
 *
 * @author skeggsc
 */
public final class CJoystickDirect extends AbstractJoystick {

    private final int port;

    /**
     * Create a new CJoystick for a specific joystick ID. It will be important
     * to call attach() to add a source to update this object.
     *
     * @param joystick the joystick ID
     * @see #attach(ccre.channel.EventInput)
     */
    public CJoystickDirect(int joystick, EventInput check) {
        super(check);
        if (joystick == 5 || joystick == 6) {
            throw new IllegalArgumentException("Kinect Joysticks are not supported by the RoboRIO.");
        } else if (joystick < 1 || joystick > 4) {
            throw new IllegalArgumentException("Joystick " + joystick + " is not a valid joystick number.");
        } else {
            port = joystick - 1;
        }
        DirectDriverStation.verifyPortNumber(port);
    }

    public FloatInputPoll getAxisChannel(final int axis) {
        if (axis < 1 || axis > DirectDriverStation.AXIS_NUM) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        return () -> DirectDriverStation.getStickAxis(port, axis - 1);
    }

    public BooleanInputPoll getButtonChannel(final int button) {
        if (button < 1) {
            throw new IllegalArgumentException("Invalid button ID: " + button);
        }
        return () -> DirectDriverStation.getStickButton(port, button - 1);
    }

    public BooleanInputPoll isPOVPressed(int pov) {
        return () -> DirectDriverStation.getStickPOV(port, pov - 1) != -1;
    }

    public FloatInputPoll getPOVAngle(int pov) {
        return () -> DirectDriverStation.getStickPOV(port, pov - 1);
    }
}

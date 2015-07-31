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
package ccre.igneous;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.AbstractJoystickWithPOV;
import edu.wpi.first.wpilibj.Joystick;

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
    private final Joystick joy;

    /**
     * Create a new CJoystick for a specific joystick ID.
     *
     * @param joystick the joystick ID
     */
    CJoystick(int joystick, EventInput check) {
        super(check);
        if (joystick < 1 || joystick > 6) {
            throw new IllegalArgumentException("Joystick " + joystick + " is not a valid joystick number.");
        } else {
            joy = new Joystick(joystick - 1);
        }
    }

    public FloatInputPoll getAxisChannel(final int axis) {
        return () -> (float) joy.getRawAxis(axis - 1);
    }

    public BooleanInputPoll getButtonChannel(final int button) {
        return () -> joy.getRawButton(button);
    }

    public BooleanInputPoll isPOVPressed(int id) {
        return () -> joy.getPOV(id - 1) != -1;
    }

    public FloatInputPoll getPOVAngle(int id) {
        return () -> joy.getPOV(id - 1);
    }
}

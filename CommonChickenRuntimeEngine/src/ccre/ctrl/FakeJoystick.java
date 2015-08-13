/*
 * Copyright 2015 Colby Skeggs
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
package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;

/**
 * A Joystick that doesn't actually exist and instead just throws exceptions.
 *
 * Used when you try to access Joystick #5 or Joystick #6 from a cRIO.
 *
 * @author skeggsc
 */
public class FakeJoystick implements IJoystickWithPOV {

    private final String errorMessage;

    /**
     * Create a new FakeJoystick that errors with the specified error message.
     *
     * @param errorMessage the message to throw as a RuntimeException.
     */
    public FakeJoystick(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public BooleanInput button(int id) {
        throw new RuntimeException(errorMessage);
    }

    public FloatInput axis(int axis) {
        throw new RuntimeException(errorMessage);
    }

    public BooleanInput isPOV(int direction) {
        throw new RuntimeException(errorMessage);
    }
}

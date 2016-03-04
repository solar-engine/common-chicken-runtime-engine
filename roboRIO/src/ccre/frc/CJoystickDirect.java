/*
 * Copyright 2013-2016 Cel Skeggs
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
package ccre.frc;

import ccre.channel.EventInput;
import ccre.ctrl.AbstractJoystick;

/**
 * An IJoystick implementation that allows reading from a joystick on the driver
 * station.
 *
 * @author skeggsc
 */
public final class CJoystickDirect extends AbstractJoystick {

    private final int port;

    /**
     * Create a new CJoystick for a specific joystick ID.
     *
     * @param joystick the joystick ID
     * @param check when to update the input sources.
     */
    public CJoystickDirect(int joystick, EventInput check) {
        super(check, 12, 32);
        if (joystick < 1 || joystick > 6) {
            throw new IllegalArgumentException("Joystick " + joystick + " is not a valid joystick number.");
        } else {
            port = joystick - 1;
        }
        DirectDriverStation.verifyPortNumber(port);
    }

    @Override
    protected boolean getButton(int btn) {
        return DirectDriverStation.getStickButton(port, btn - 1);
    }

    @Override
    protected float getAxis(int axis) {
        return DirectDriverStation.getStickAxis(port, axis - 1);
    }

    @Override
    protected boolean getPOV(int direction) {
        return DirectDriverStation.getStickPOV(port, 0) == direction;
    }

    @Override
    protected void setRumble(float left, float right) {
        DirectDriverStation.setStickRumble(port, left, right);
    }
}

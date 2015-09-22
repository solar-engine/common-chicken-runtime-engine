/*
 * Copyright 2013-2014 Colby Skeggs, Alexander Mackworth
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
import ccre.channel.EventInput;
import ccre.channel.FloatInput;

/**
 * A joystick with axes and buttons.
 *
 * @author skeggsc
 */
public interface IJoystick {

    public static final int POV_NORTH = 0;
    public static final int POV_NORTHEAST = 45;
    public static final int POV_EAST = 90;
    public static final int POV_SOUTHEAST = 135;
    public static final int POV_SOUTH = 180;
    public static final int POV_SOUTHWEST = 225;
    public static final int POV_WEST = 270;
    public static final int POV_NORTHWEST = 315;
    public static final int[] POV_DIRECTIONS = { POV_NORTH, POV_NORTHEAST, POV_EAST, POV_SOUTHEAST, POV_SOUTH, POV_SOUTHWEST, POV_WEST, POV_NORTHWEST };

    public default EventInput onPress(int btn) {
        return button(btn).onPress();
    }

    public default EventInput onRelease(int btn) {
        return button(btn).onRelease();
    }

    public BooleanInput button(int btn);

    public FloatInput axis(int axis);

    public default FloatInput axisX() {
        return axis(1);
    }

    public default FloatInput axisY() {
        return axis(2);
    }

    public BooleanInput isPOV(int direction);

    public default EventInput onPressPOV(int direction) {
        return isPOV(direction).onPress();
    }

    public default EventInput onReleasePOV(int direction) {
        return isPOV(direction).onRelease();
    }
}

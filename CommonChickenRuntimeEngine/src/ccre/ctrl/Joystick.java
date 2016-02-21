/*
 * Copyright 2013-2016 Cel Skeggs, 2014 Alexander Mackworth
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
import ccre.channel.FloatOutput;

/**
 * A Joystick with axes and buttons.
 *
 * Axes and buttons are 1-based, not 0-based.
 *
 * @author skeggsc
 */
public interface Joystick {

    /**
     * The degree angle that represents north.
     */
    public static final int POV_NORTH = 0;
    /**
     * The degree angle that represents northeast.
     */
    public static final int POV_NORTHEAST = 45;
    /**
     * The degree angle that represents east.
     */
    public static final int POV_EAST = 90;
    /**
     * The degree angle that represents southeast.
     */
    public static final int POV_SOUTHEAST = 135;
    /**
     * The degree angle that represents south.
     */
    public static final int POV_SOUTH = 180;
    /**
     * The degree angle that represents southwest.
     */
    public static final int POV_SOUTHWEST = 225;
    /**
     * The degree angle that represents west.
     */
    public static final int POV_WEST = 270;
    /**
     * The degree angle that represents northwest.
     */
    public static final int POV_NORTHWEST = 315;
    /**
     * An array of all of the POV hat directions. The directions are
     * {@link #POV_NORTH}, {@link #POV_NORTHEAST}, {@link #POV_EAST},
     * {@link #POV_SOUTHEAST}, {@link #POV_SOUTH}, {@link #POV_SOUTHWEST},
     * {@link #POV_WEST}, and {@link #POV_NORTHWEST}.
     */
    public static final int[] POV_DIRECTIONS = { POV_NORTH, POV_NORTHEAST, POV_EAST, POV_SOUTHEAST, POV_SOUTH, POV_SOUTHWEST, POV_WEST, POV_NORTHWEST };

    /**
     * Provides an EventInput that is fired when button number <code>btn</code>
     * is pressed.
     *
     * @param btn the button number to check, indexed starting at one.
     * @return the EventInput for when the button is pressed.
     */
    public default EventInput onPress(int btn) {
        return button(btn).onPress();
    }

    /**
     * Provides an EventInput that is fired when button number <code>btn</code>
     * is released.
     *
     * @param btn the button number to check, indexed starting at one.
     * @return the EventInput for when the button is released.
     */
    public default EventInput onRelease(int btn) {
        return button(btn).onRelease();
    }

    /**
     * Provides a BooleanInput representing whether button number
     * <code>btn</code> is currently pressed (as in, held down.)
     *
     * @param btn the button number to check, indexed starting at one.
     * @return the BooleanInput for the button.
     */
    public BooleanInput button(int btn);

    /**
     * Provides a FloatInput representing the position of axis number
     * <code>axis</code>. This is a value from -1.0 to 1.0, inclusive, where 0.0
     * is neutral (resting.)
     *
     * @param axis the axis number to check, indexed starting at one.
     * @return the FloatInput for the axis.
     */
    public FloatInput axis(int axis);

    public FloatOutput rumble(boolean right);

    public default FloatOutput rumbleLeft() {
        return rumble(false);
    }

    public default FloatOutput rumbleRight() {
        return rumble(true);
    }

    /**
     * Provides a FloatInput representing the position of the X axis (axis
     * number 1.) This is a value from -1.0 to 1.0, inclusive, where 0.0 is
     * neutral (resting.)
     *
     * @return the FloatInput for the X axis.
     */
    public default FloatInput axisX() {
        return axis(1);
    }

    /**
     * Provides a FloatInput representing the position of the Y axis (axis
     * number 2.) This is a value from -1.0 to 1.0, inclusive, where 0.0 is
     * neutral (resting.)
     *
     * @return the FloatInput for the Y axis.
     */
    public default FloatInput axisY() {
        return axis(2);
    }

    /**
     * Provides a BooleanInput representing whether the POV hat is currently
     * being pushed in <code>direction</code>.
     *
     * The directions are {@link #POV_NORTH}, {@link #POV_NORTHEAST},
     * {@link #POV_EAST}, {@link #POV_SOUTHEAST}, {@link #POV_SOUTH},
     * {@link #POV_SOUTHWEST}, {@link #POV_WEST}, and {@link #POV_NORTHWEST}.
     *
     * @param direction the direction of the POV hat to check.
     * @return the BooleanInput representing whether the POV is being pushed in
     * the specified direction.
     */
    public BooleanInput isPOV(int direction);

    /**
     * Provides an EventInput that is fired when the POV hat starts being pushed
     * in <code>direction</code>, including changing from being pushed in a
     * different direction.
     *
     * The directions are {@link #POV_NORTH}, {@link #POV_NORTHEAST},
     * {@link #POV_EAST}, {@link #POV_SOUTHEAST}, {@link #POV_SOUTH},
     * {@link #POV_SOUTHWEST}, {@link #POV_WEST}, and {@link #POV_NORTHWEST}.
     *
     * @param direction the direction of the POV hat to check.
     * @return the EventInput for when the POV starts being pushed in
     * <code>direction</code>.
     */
    public default EventInput onPressPOV(int direction) {
        return isPOV(direction).onPress();
    }

    /**
     * Provides an EventInput that is fired when the POV hat stops being pushed
     * in <code>direction</code>, including changing to being pushed in a
     * different direction.
     *
     * The directions are {@link #POV_NORTH}, {@link #POV_NORTHEAST},
     * {@link #POV_EAST}, {@link #POV_SOUTHEAST}, {@link #POV_SOUTH},
     * {@link #POV_SOUTHWEST}, {@link #POV_WEST}, and {@link #POV_NORTHWEST}.
     *
     * @param direction the direction of the POV hat to check.
     * @return the EventInput for when the POV stops being pushed in
     * <code>direction</code>.
     */
    public default EventInput onReleasePOV(int direction) {
        return isPOV(direction).onRelease();
    }
}

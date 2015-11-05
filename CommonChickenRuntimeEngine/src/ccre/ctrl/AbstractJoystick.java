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

import java.util.HashMap;

import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.DerivedFloatInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;

/**
 * An abstract Joystick implementation, that allows one to convert a poll-based
 * interface into an event-driven dataflow Joystick.
 *
 * @author skeggsc
 */
public abstract class AbstractJoystick implements Joystick {

    private final EventInput check;
    private final int axisCount, buttonCount;

    /**
     * Creates a new AbstractJoystick. When <code>check</code> is fired, the
     * Joystick checks to see if any buttons, any axes, or the POV hat have
     * changed state. If they have, their corresponding channels are updated.
     *
     * @param check when to poll the Joystick.
     * @param axisCount the number of axes on the Joystick.
     * @param buttonCount the number of buttons on the Joystick.
     */
    public AbstractJoystick(EventInput check, int axisCount, int buttonCount) {
        this.check = check;
        this.axisCount = axisCount;
        this.buttonCount = buttonCount;
    }

    private final HashMap<Integer, BooleanInput> buttons = new HashMap<>();
    private final HashMap<Integer, FloatInput> floats = new HashMap<>();
    private final HashMap<Integer, BooleanInput> povs = new HashMap<>();

    @Override
    public BooleanInput button(int btn) {
        BooleanInput old = buttons.get(btn);
        if (old != null) {
            return old;
        }
        if (btn < 1 || btn > buttonCount) {
            throw new IllegalArgumentException("button index must be in range of 1 ... " + buttonCount);
        }
        BooleanInput out = new DerivedBooleanInput(check) {
            @Override
            protected boolean apply() {
                return getButton(btn);
            }
        };
        buttons.put(btn, out);
        return out;
    }

    /**
     * Polls button number <code>btn</code> for its state.
     *
     * @param btn the button number, which is in the range 1 to
     * <code>buttonCount</code>, inclusive.
     * @return true if the button is currently pressed, or false if it is not.
     */
    protected abstract boolean getButton(int btn);

    @Override
    public FloatInput axis(int axis) {
        FloatInput old = floats.get(axis);
        if (old != null) {
            return old;
        }
        if (axis < 1 || axis > axisCount) {
            throw new IllegalArgumentException("axis index must be in range of 1 ... " + axisCount);
        }
        FloatInput out = new DerivedFloatInput(check) {
            @Override
            protected float apply() {
                return getAxis(axis);
            }
        };
        floats.put(axis, out);
        return out;
    }

    /**
     * Polls axis number <code>axis</code> for its state.
     *
     * @param axis the axis number, which is in the range 1 to
     * <code>axisCount</code>, inclusive.
     * @return the axis's current position, from -1.0 to +1.0.
     */
    protected abstract float getAxis(int axis);

    @Override
    public BooleanInput isPOV(int direction) {
        BooleanInput old = povs.get(direction);
        if (old != null) {
            return old;
        }
        if (direction < 0 || direction >= 360) {
            throw new IllegalArgumentException("isPOV index must be in range of 0 ... 359");
        }
        BooleanInput out = new DerivedBooleanInput(check) {
            @Override
            protected boolean apply() {
                return getPOV(direction);
            }
        };
        povs.put(direction, out);
        return out;
    }

    /**
     * Polls the POV hat and checks if it points in a certain direction.
     *
     * @param direction the direction to check, in degrees from 0 to 359,
     * inclusive.
     * @return true if the POV hat is currently pointing in this direction, or
     * false otherwise.
     */
    protected abstract boolean getPOV(int direction);
}

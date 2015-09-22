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
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatStatus;

public abstract class AbstractJoystick implements IJoystick {

    private final EventInput check;
    private final int axisCount, buttonCount;

    public AbstractJoystick(EventInput check, int axisCount, int buttonCount) {
        this.check = check;
        this.axisCount = axisCount;
        this.buttonCount = buttonCount;
    }

    private final HashMap<Integer, BooleanStatus> buttons = new HashMap<>();
    private final HashMap<Integer, FloatStatus> floats = new HashMap<>();
    private final HashMap<Integer, BooleanStatus> povs = new HashMap<>();

    @Override
    public BooleanInput button(int btn) {
        BooleanStatus old = buttons.get(btn);
        if (old != null) {
            return old;
        }
        if (btn < 1 || btn > buttonCount) {
            throw new IllegalArgumentException("button index must be in range of 1 ... " + buttonCount);
        }
        BooleanStatus out = new BooleanStatus();
        check.send(() -> out.set(getButton(btn)));
        buttons.put(btn, out);
        return out;
    }

    protected abstract boolean getButton(int btn);

    @Override
    public FloatInput axis(int axis) {
        FloatStatus old = floats.get(axis);
        if (old != null) {
            return old;
        }
        if (axis < 1 || axis > axisCount) {
            throw new IllegalArgumentException("axis index must be in range of 1 ... " + axisCount);
        }
        FloatStatus out = new FloatStatus();
        check.send(() -> out.set(getAxis(axis)));
        floats.put(axis, out);
        return out;
    }

    protected abstract float getAxis(int axis);

    @Override
    public BooleanInput isPOV(int direction) {
        BooleanStatus old = povs.get(direction);
        if (old != null) {
            return old;
        }
        if (direction < 0 || direction >= 360) {
            throw new IllegalArgumentException("isPOV index must be in range of 0 ... 359");
        }
        BooleanStatus out = new BooleanStatus();
        check.send(() -> out.set(getPOV(direction)));
        povs.put(direction, out);
        return out;
    }

    protected abstract boolean getPOV(int direction);
}

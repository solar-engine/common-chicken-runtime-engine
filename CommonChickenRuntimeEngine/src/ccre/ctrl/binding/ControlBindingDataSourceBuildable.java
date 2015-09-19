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
package ccre.ctrl.binding;

import java.util.HashMap;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.ctrl.IJoystick;

/**
 * A ControlBindingDataSourceBuildable is an easy way to define a
 * ControlBindingDataSource.
 *
 * With this interface, you can add a set of inputs to the builder, and then all
 * the inputs will be made available over the ControlBindingDataSource
 * interface.
 *
 * @author skeggsc
 */
public class ControlBindingDataSourceBuildable implements ControlBindingDataSource {
    private final HashMap<String, BooleanInput> booleans = new HashMap<String, BooleanInput>();
    private final HashMap<String, FloatInput> floats = new HashMap<String, FloatInput>();

    /**
     * Add inputs for all the buttons and axes of a Joystick, and its POV hat.
     *
     * @param name a name for the Joystick.
     * @param joy the Joystick.
     * @param buttonCount the number of buttons.
     * @param axisCount the number of axes.
     * @see #addJoystick(String, IJoystick, int, int)
     */
    public void addJoystick(String name, IJoystick joy, int buttonCount, int axisCount) {
        for (int i = 1; i <= buttonCount; i++) {
            addButton(name + " BTN " + i, joy.button(i));
        }
        for (int i = 1; i <= axisCount; i++) {
            addAxis(name + " AXIS " + i, joy.axis(i));
        }
        addButton(name + " POV UP", joy.isPOV(IJoystick.POV_NORTH));
        addButton(name + " POV DOWN", joy.isPOV(IJoystick.POV_SOUTH));
        addButton(name + " POV LEFT", joy.isPOV(IJoystick.POV_WEST));
        addButton(name + " POV RIGHT", joy.isPOV(IJoystick.POV_EAST));
    }

    /**
     * Add a BooleanInput as a control input.
     *
     * @param name the name of the input.
     * @param buttonChannel the BooleanInput.
     */
    public void addButton(String name, BooleanInput buttonChannel) {
        if (booleans.containsKey(name)) {
            throw new IllegalArgumentException("Boolean source already registered: '" + name + "'");
        }
        booleans.put(name, buttonChannel);
    }

    /**
     * Add a FloatInput axis, both as a raw axis and as buttons for the extremes
     * of the axis.
     *
     * @param name the name for the axis.
     * @param axis the FloatInput to add.
     */
    public void addAxis(String name, FloatInput axis) {
        addAxisRaw(name, axis);
        addButton(name + " AS BTN+", axis.atLeast(0.8f));
        addButton(name + " AS BTN-", axis.atMost(-0.8f));
    }

    /**
     * Add a FloatInput.
     *
     * @param name the name for the axis.
     * @param axisSource the FloatInput to add.
     */
    public void addAxisRaw(String name, FloatInput axisSource) {
        if (floats.containsKey(name)) {
            throw new IllegalArgumentException("Float source already registered: '" + name + "'");
        }
        floats.put(name, axisSource);
    }

    public String[] listBooleans() {
        return booleans.keySet().toArray(new String[booleans.keySet().size()]);
    }

    public BooleanInput getBoolean(String name) {
        return booleans.get(name);
    }

    public String[] listFloats() {
        return floats.keySet().toArray(new String[floats.keySet().size()]);
    }

    public FloatInput getFloat(String name) {
        return floats.get(name);
    }
}

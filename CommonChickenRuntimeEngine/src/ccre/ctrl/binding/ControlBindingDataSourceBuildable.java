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

import java.util.ConcurrentModificationException;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;
import ccre.ctrl.IJoystickWithPOV;
import ccre.igneous.Igneous;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CHashMap;

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
    private final EventInput updateOn;
    private final CHashMap<String, BooleanInput> booleans = new CHashMap<String, BooleanInput>();
    private final CHashMap<String, FloatInput> floats = new CHashMap<String, FloatInput>();

    /**
     * Create a new ControlBindingDataSourceBuildable that updates when the
     * specified event is produced.
     *
     * Since certain inputs will update when, and only when, updateOn is
     * pressed, make sure that it always keeps firing.
     *
     * For example, you could use <code>Igneous.globalPeriodic</code>.
     *
     * @param updateOn when to update any InputPolls provided to this buildable.
     */
    public ControlBindingDataSourceBuildable(EventInput updateOn) {
        if (updateOn == null) {
            throw new NullPointerException();
        }
        this.updateOn = updateOn;
    }

    /**
     * Add inputs for all the buttons and axes of a Joystick.
     *
     * @param name a name for the Joystick.
     * @param joy the Joystick.
     * @param buttonCount how many buttons to include.
     * @param axisCount how many axes to include.
     */
    public void addJoystick(String name, IJoystick joy, int buttonCount, int axisCount) {
        for (int i = 1; i <= buttonCount; i++) {
            addButton(name + " BTN " + i, joy.button(i));
        }
        for (int i = 1; i <= axisCount; i++) {
            addAxis(name + " AXIS " + i, joy.axis(i));
        }
    }

    /**
     * Add a BooleanInputPoll as a control input.
     *
     * @param name the name of the input.
     * @param buttonChannel the BooleanInputPoll.
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
     * @param axisSource the FloatInput to add.
     */
    public void addAxis(String name, FloatInput axisSource) {
        addAxisRaw(name, axisSource);
        addButton(name + " AS BTN+", FloatMixing.floatIsAtLeast(axisSource, 0.8f));
        addButton(name + " AS BTN-", FloatMixing.floatIsAtMost(axisSource, -0.8f));
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

    /**
     * Add inputs for all the buttons and axes of a Joystick, and its POV hat.
     *
     * @param name a name for the Joystick.
     * @param joy the Joystick.
     * @param buttonCount the number of buttons.
     * @param axisCount the number of axes.
     * @see #addJoystick(String, IJoystick, int, int)
     */
    public void addJoystick(String name, IJoystickWithPOV joy, int buttonCount, int axisCount) {
        addJoystick(name, (IJoystick) joy, buttonCount, axisCount);
        addPOVHandler(name, joy);
    }

    private void addPOVHandler(String name, IJoystickWithPOV joy) {
        addButton(name + " POV UP", Igneous.joystick2.isPOV(IJoystick.POV_NORTH));
        addButton(name + " POV DOWN", Igneous.joystick2.isPOV(IJoystick.POV_SOUTH));
        addButton(name + " POV LEFT", Igneous.joystick2.isPOV(IJoystick.POV_WEST));
        addButton(name + " POV RIGHT", Igneous.joystick2.isPOV(IJoystick.POV_EAST));
    }

    public String[] listBooleans() {
        String[] stra;
        CArrayList<String> strs = CArrayUtils.collectIterable(booleans);
        stra = new String[strs.size()];
        if (strs.fillArray(stra) != 0) {
            throw new ConcurrentModificationException();
        }
        return stra;
    }

    public BooleanInput getBoolean(String name) {
        return booleans.get(name);
    }

    public String[] listFloats() {
        String[] stra;
        CArrayList<String> strs = CArrayUtils.collectIterable(floats);
        stra = new String[strs.size()];
        if (strs.fillArray(stra) != 0) {
            throw new ConcurrentModificationException();
        }
        return stra;
    }

    public FloatInput getFloat(String name) {
        return floats.get(name);
    }
}

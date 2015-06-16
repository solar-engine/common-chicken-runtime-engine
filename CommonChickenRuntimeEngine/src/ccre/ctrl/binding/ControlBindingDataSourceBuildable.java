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
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CHashMap;

public class ControlBindingDataSourceBuildable implements ControlBindingDataSource {
    private final EventInput updateOn;
    private final CHashMap<String, BooleanInput> booleans = new CHashMap<String, BooleanInput>();
    private final CHashMap<String, FloatInput> floats = new CHashMap<String, FloatInput>();

    public ControlBindingDataSourceBuildable(EventInput updateOn) {
        this.updateOn = updateOn;
    }

    public void addJoystick(String name, IJoystick joy, int buttonCount, int axisCount) {
        addJoystickButtons(name, joy, buttonCount);
        addJoystickAxes(name, joy, axisCount);
    }

    public void addJoystickButtons(String name, IJoystick joy, int buttonCount) {
        for (int i = 1; i <= buttonCount; i++) {
            addButton(name + " BTN " + i, joy.getButtonChannel(i));
        }
    }

    public void addButton(String name, BooleanInputPoll buttonChannel) {
        addButton(name, BooleanMixing.createDispatch(buttonChannel, updateOn));
    }

    public void addButton(String name, BooleanInput buttonChannel) {
        if (booleans.containsKey(name)) {
            throw new IllegalArgumentException("Boolean source already registered: '" + name + "'");
        }
        booleans.put(name, buttonChannel);
    }

    public void addJoystickAxes(String name, IJoystick joy, int axisCount) {
        for (int i = 1; i <= axisCount; i++) {
            addAxis(name + " AXIS " + i, joy.getAxisSource(i));
        }
    }

    public void addAxis(String name, FloatInput axisSource) {
        addAxisRaw(name, axisSource);
        addButton(name + " AS BTN+", FloatMixing.floatIsAtLeast(axisSource, 0.8f));
        addButton(name + " AS BTN-", FloatMixing.floatIsAtMost(axisSource, -0.8f));
    }

    public void addAxisRaw(String name, FloatInput axisSource) {
        if (floats.containsKey(name)) {
            throw new IllegalArgumentException("Float source already registered: '" + name + "'");
        }
        floats.put(name, axisSource);
    }

    public void addJoystick(String name, IJoystickWithPOV joy, int buttonCount, int axisCount) {
        addJoystick(name, (IJoystick) joy, buttonCount, axisCount);
        addPOVHandler(name, joy);
    }

    private void addPOVHandler(String name, IJoystickWithPOV joy) {
        BooleanInput povPressed = joy.isPOVPressedSource(1);
        FloatInput povAngle = joy.getPOVAngleSource(1);
        addButton(name + " POV UP", BooleanMixing.andBooleans(FloatMixing.floatIsInRange(povAngle, -0.1f, 0.1f), povPressed));
        addButton(name + " POV DOWN", BooleanMixing.andBooleans(FloatMixing.floatIsInRange(povAngle, 179.9f, 180.1f), povPressed));
        addButton(name + " POV LEFT", BooleanMixing.andBooleans(FloatMixing.floatIsInRange(povAngle, 269.9f, 270.1f), povPressed));
        addButton(name + " POV RIGHT", BooleanMixing.andBooleans(FloatMixing.floatIsInRange(povAngle, 89.9f, 90.1f), povPressed));
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

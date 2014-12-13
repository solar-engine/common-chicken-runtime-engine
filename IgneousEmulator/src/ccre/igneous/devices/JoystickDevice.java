/*
 * Copyright 2014 Colby Skeggs
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
package ccre.igneous.devices;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.IJoystick;
import ccre.igneous.DeviceGroup;
import ccre.igneous.DeviceListPanel;

public class JoystickDevice extends DeviceGroup implements IJoystick {

    private FloatControlDevice[] axes = new FloatControlDevice[6];
    private BooleanControlDevice[] buttons = new BooleanControlDevice[12];

    private boolean wasAddedToMaster = false;
    private final DeviceListPanel master;

    public JoystickDevice(String name, DeviceListPanel master) {
        add(new HeadingDevice(name));
        this.master = master;
    }

    public JoystickDevice(int id, DeviceListPanel master) {
        this("Joystick " + id, master);
    }
    
    public synchronized JoystickDevice addToMaster() {
        if (!wasAddedToMaster) {
            wasAddedToMaster = true;
            master.add(this);
        }
        return this;
    }

    private FloatControlDevice getAxis(int id) {
        if (id < 1 || id > axes.length) {
            throw new IllegalArgumentException("Invalid axis number: " + id);
        }
        if (axes[id - 1] == null) {
            axes[id - 1] = new FloatControlDevice("Axis " + id);
            add(axes[id - 1]);
            addToMaster();
        }
        return axes[id - 1];
    }

    public EventInput getButtonSource(int id) {
        if (id < 1 || id > buttons.length) {
            throw new IllegalArgumentException("Invalid button number: " + id);
        }
        if (buttons[id - 1] == null) {
            buttons[id - 1] = new BooleanControlDevice("Button " + id);
            add(buttons[id - 1]);
            addToMaster();
        }
        return buttons[id - 1].whenPressed();
    }

    public FloatInput getAxisSource(int axis) {
        return getAxis(axis);
    }

    public FloatInputPoll getAxisChannel(int axis) {
        return getAxis(axis);
    }

    public BooleanInputPoll getButtonChannel(int id) {
        if (id < 1 || id > buttons.length) {
            throw new IllegalArgumentException("Invalid button number: " + id);
        }
        if (buttons[id - 1] == null) {
            buttons[id - 1] = new BooleanControlDevice("Button " + id);
            add(buttons[id - 1]);
            addToMaster();
        }
        return buttons[id - 1];
    }

    public FloatInputPoll getXChannel() {
        return getAxisChannel(1);
    }

    public FloatInputPoll getYChannel() {
        return getAxisChannel(2);
    }

    public FloatInput getXAxisSource() {
        return getAxisSource(1);
    }

    public FloatInput getYAxisSource() {
        return getAxisSource(2);
    }

}

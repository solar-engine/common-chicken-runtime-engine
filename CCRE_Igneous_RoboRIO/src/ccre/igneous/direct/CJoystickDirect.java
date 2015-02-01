/*
 * Copyright 2013-2015 Colby Skeggs
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
package ccre.igneous.direct;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatStatus;
import ccre.ctrl.IJoystick;

/**
 * An IJoystick implementation that allows reading from a joystick on the driver
 * station.
 *
 * @author skeggsc
 */
public final class CJoystickDirect implements EventOutput, IJoystick {

    /**
     * The joystick reference that is read from.
     */
    private final int port;
    
    public static final int MAX_BUTTONS = 12;

    /**
     * Events to fire when the buttons are pressed.
     */
    private final EventStatus[] buttons = new EventStatus[MAX_BUTTONS];
    /**
     * The last known states of the buttons, used to calculate when to send
     * press events.
     */
    private final boolean[] states = new boolean[MAX_BUTTONS];
    /**
     * The objects behind the provided FloatInputs that represent the current
     * values of the joysticks.
     */
    private final FloatStatus[] axes = new FloatStatus[DirectDriverStation.AXIS_NUM];

    /**
     * Create a new CJoystick for a specific joystick ID. It will be important
     * to call attach() to add a source to update this object.
     *
     * @param joystick the joystick ID
     * @see #attach(ccre.channel.EventInput)
     */
    public CJoystickDirect(int joystick) {
        if (joystick == 5 || joystick == 6) {
            throw new IllegalArgumentException("Kinect Joysticks are not supported by the RoboRIO.");
        } else if (joystick < 1 || joystick > 4) {
            throw new IllegalArgumentException("Joystick " + joystick + " is not a valid joystick number.");
        } else {
            port = joystick - 1;
        }
        DirectDriverStation.verifyPortNumber(port);
    }

    /**
     * Attach the specified event input to update this (if it's not null), and
     * then return this object itself for the purpose of method chaining.
     *
     * @param input The input to update this with.
     * @return this object.
     */
    public CJoystickDirect attach(EventInput input) {
        if (input != null) {
            input.send(this);
        }
        return this;
    }

    public FloatInputPoll getAxisChannel(final int axis) {
        if (axis < 1 || axis > DirectDriverStation.AXIS_NUM) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        return new FloatInputPoll() {
            public float get() {
                return DirectDriverStation.getStickAxis(port, axis - 1);
            }
        };
    }

    public FloatInputPoll getXChannel() {
        return getAxisChannel(1);
    }

    public FloatInputPoll getYChannel() {
        return getAxisChannel(2);
    }

    public BooleanInputPoll getButtonChannel(final int button) {
        if (button < 1) {
            throw new IllegalArgumentException("Invalid button ID: " + button);
        }
        return new BooleanInputPoll() {
            public boolean get() {
                return DirectDriverStation.getStickButton(port, button - 1);
            }
        };
    }

    public EventInput getButtonSource(int button) {
        if (button < 1) {
            throw new IllegalArgumentException("Invalid button ID: " + button);
        }
        EventStatus cur = buttons[button - 1];
        if (cur == null) {
            cur = new EventStatus();
            buttons[button - 1] = cur;
            states[button - 1] = DirectDriverStation.getStickButton(port, button - 1);
        }
        return cur;
    }

    public FloatInput getAxisSource(int axis) {
        FloatStatus fpb = axes[axis - 1];
        if (fpb == null) {
            fpb = new FloatStatus();
            fpb.set(DirectDriverStation.getStickAxis(port, axis - 1));
            axes[axis - 1] = fpb;
        }
        return fpb;
    }

    public void event() {
        for (int i = 0; i < MAX_BUTTONS; i++) {
            EventStatus e = buttons[i];
            if (e == null) {
                continue;
            }
            boolean state = DirectDriverStation.getStickButton(port, i);
            if (state != states[i]) {
                if (state && e.hasConsumers()) {
                    e.produce();
                }
                states[i] = state;
            }
        }
        for (int i = 0; i < DirectDriverStation.AXIS_NUM; i++) {
            FloatStatus fpb = axes[i];
            if (fpb == null) {
                continue;
            }
            fpb.set(DirectDriverStation.getStickAxis(port, i));
        }
    }

    public FloatInput getXAxisSource() {
        return getAxisSource(1);
    }

    public FloatInput getYAxisSource() {
        return getAxisSource(2);
    }

}

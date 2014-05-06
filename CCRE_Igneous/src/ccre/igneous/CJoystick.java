/*
 * Copyright 2013 Colby Skeggs
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
package ccre.igneous;

import ccre.channel.*;
import ccre.ctrl.IJoystick;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.KinectStick;

/**
 * An IJoystick implementation that allows reading from a joystick on the driver
 * station.
 *
 * @author skeggsc
 */
class CJoystick implements EventOutput, IJoystick {

    /**
     * Create a new CJoystick for a specific joystick ID and a specific
     * EventSource that is listened to in order to update the outputs.
     *
     * @param joystick the joystick ID
     * @param source when to update the outputs.
     */
    CJoystick(int joystick, EventInput source) {
        if (joystick == 5) {
            joy = new KinectStick(1);
        } else if (joystick == 6) {
            joy = new KinectStick(2);
        } else {
            joy = new Joystick(joystick);
        }
        if (source != null) {
            source.send(this);
        }
    }
    /**
     * The joystick object that is read from.
     */
    protected GenericHID joy;

    public FloatInputPoll getAxisChannel(final int axis) {
        return new FloatInputPoll() {
            public float get() {
                return (float) joy.getRawAxis(axis);
            }
        };
    }

    public FloatInputPoll getXChannel() {
        return new FloatInputPoll() {
            public float get() {
                return (float) joy.getX();
            }
        };
    }

    public FloatInputPoll getYChannel() {
        return new FloatInputPoll() {
            public float get() {
                return (float) joy.getY();
            }
        };
    }

    public BooleanInputPoll getButtonChannel(final int button) {
        return new BooleanInputPoll() {
            public boolean get() {
                return joy.getRawButton(button);
            }
        };
    }

    /**
     * Events to fire when the buttons are pressed.
     */
    protected EventStatus[] buttons = new EventStatus[12];
    /**
     * The last known states of the buttons, used to calculate when to send
     * press events.
     */
    protected boolean[] states = new boolean[12];
    /**
     * The objects behind the provided FloatInputs that represent the current
     * values of the joysticks.
     */
    protected FloatStatus[] axes = new FloatStatus[6];

    public EventInput getButtonSource(int id) {
        EventStatus cur = buttons[id - 1];
        if (cur == null) {
            cur = new EventStatus();
            buttons[id - 1] = cur;
            states[id - 1] = joy.getRawButton(id);
        }
        return cur;
    }

    public FloatInput getAxisSource(int axis) {
        FloatStatus fpb = axes[axis - 1];
        if (fpb == null) {
            fpb = new FloatStatus();
            fpb.set((float) joy.getRawAxis(axis));
            axes[axis - 1] = fpb;
        }
        return fpb;
    }

    public void event() {
        for (int i = 0; i < 12; i++) {
            EventStatus e = buttons[i];
            if (e == null) {
                continue;
            }
            boolean state = joy.getRawButton(i + 1);
            if (state != states[i]) {
                if (state && e.hasConsumers()) {
                    e.produce();
                }
                states[i] = state;
            }
        }
        for (int i = 0; i < 6; i++) {
            FloatStatus fpb = axes[i];
            if (fpb == null) {
                continue;
            }
            fpb.set((float) joy.getRawAxis(i + 1));
        }
    }
}

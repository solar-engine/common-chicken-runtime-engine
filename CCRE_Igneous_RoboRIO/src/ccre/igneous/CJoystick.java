/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatStatus;
import ccre.ctrl.IJoystick;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;

/**
 * An IJoystick implementation that allows reading from a joystick on the driver
 * station.
 *
 * @author skeggsc
 */
final class CJoystick implements EventOutput, IJoystick {

    /**
     * The joystick object that is read from.
     */
    private final GenericHID joy;

    /**
     * Events to fire when the buttons are pressed.
     */
    private final EventStatus[] buttons = new EventStatus[12];
    /**
     * The last known states of the buttons, used to calculate when to send
     * press events.
     */
    private final boolean[] states = new boolean[12];
    /**
     * The objects behind the provided FloatInputs that represent the current
     * values of the joysticks.
     */
    private final FloatStatus[] axes = new FloatStatus[6];

    /**
     * Create a new CJoystick for a specific joystick ID. It will be important
     * to call attach() to add a source to update this object.
     *
     * @param joystick the joystick ID
     * @see #attach(ccre.channel.EventInput)
     */
    CJoystick(int joystick) {
        if (joystick == 5 || joystick == 6) {
            throw new IllegalArgumentException("Kinect Joysticks are not supported by the RoboRIO.");
        } else if (joystick < 1 || joystick > 4) {
            throw new IllegalArgumentException("Joystick " + joystick + " is not a valid joystick number.");
        } else {
            joy = new Joystick(joystick - 1);
        }
    }

    /**
     * Attach the specified event input to update this (if it's not null), and
     * then return this object itself for the purpose of method chaining.
     *
     * @param input The input to update this with.
     * @return this object.
     */
    public CJoystick attach(EventInput input) {
        if (input != null) {
            input.send(this);
        }
        return this;
    }

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
            fpb.set((float) joy.getRawAxis(i));
        }
    }
    
	public FloatInput getXAxisSource() {
		return getAxisSource(1);
	}

	public FloatInput getYAxisSource() {
		return getAxisSource(2);
	}

}

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

import ccre.chan.FloatInput;
import ccre.chan.FloatStatus;
import ccre.ctrl.IDispatchJoystick;
import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;

/**
 * An IDispatchJoystick implementation that allows reading from a joystick on
 * the driver station.
 *
 * @author skeggsc
 */
class CDispatchJoystick extends CSimpleJoystick implements IDispatchJoystick, EventConsumer {

    /**
     * Events to fire when the buttons are pressed.
     */
    protected Event[] buttons = new Event[12];
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

    /**
     * Create a new CDispatchJoystick for a specific joystick ID and a specific
     * EventSource that is listened to in order to update the outputs.
     *
     * @param joystick the joystick ID
     * @param source when to update the outputs.
     */
    CDispatchJoystick(int joystick, EventSource source) {
        super(joystick);
        if (source != null) {
            source.addListener(this);
        }
    }

    public EventSource getButtonSource(int id) {
        Event cur = buttons[id - 1];
        if (cur == null) {
            cur = new Event();
            buttons[id - 1] = cur;
            states[id - 1] = joy.getRawButton(id);
        }
        return cur;
    }

    public FloatInput getAxisSource(int axis) {
        FloatStatus fpb = axes[axis - 1];
        if (fpb == null) {
            fpb = new FloatStatus();
            fpb.writeValue((float) joy.getRawAxis(axis));
            axes[axis - 1] = fpb;
        }
        return fpb;
    }

    public void eventFired() {
        for (int i = 0; i < 12; i++) {
            Event e = buttons[i];
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
            fpb.writeValue((float) joy.getRawAxis(i + 1));
        }
    }
}

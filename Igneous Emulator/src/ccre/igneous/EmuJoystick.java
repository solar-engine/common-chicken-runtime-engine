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
import javax.swing.JSlider;
import javax.swing.JToggleButton;

/**
 * A helper class for EmulatorForm, used to implement GUI-driven joysticks.
 * @author skeggsc
 */
public final class EmuJoystick implements IJoystick, EventOutput {

    private JToggleButton[] btns;
    private JSlider[] axes;

    public EmuJoystick(JToggleButton[] btns, JSlider[] axes) {
        if (btns.length != 12 || axes.length != 6) {
            throw new RuntimeException("Bad number for joystick!");
        }
        this.btns = btns;
        this.axes = axes;
    }
    /**
     * Events to fire when the buttons are pressed.
     */
    EventStatus[] buttons = new EventStatus[12];
    /**
     * The last known states of the buttons, used to calculate when to send
     * press events.
     */
    boolean[] states = new boolean[12];
    /**
     * The objects behind the provided FloatInputs that represent the current
     * values of the joysticks.
     */
    FloatStatus[] valaxes = new FloatStatus[6];
    /**
     * The current eventsource for updating the dispatch outputs.
     */
    EventInput cursource = null;

    /**
     * Set the update source for this joystick to the specific source. Throw an
     * error if this is a different source than last time.
     *
     * @param source when to update the dispatch outputs.
     */
    public void addSource(EventInput source) {
        if (cursource != source && cursource != null) {
            throw new RuntimeException("Already had a source!");
        }
        source.send(this);
    }

    @Override
    public EventInput getButtonSource(int id) {
        EventStatus cur = buttons[id - 1];
        if (cur == null) {
            cur = new EventStatus();
            buttons[id - 1] = cur;
            states[id - 1] = btns[id - 1].isSelected();
        }
        return cur;
    }

    @Override
    public FloatInput getAxisSource(int axis) {
        FloatStatus fpb = valaxes[axis - 1];
        if (fpb == null) {
            fpb = new FloatStatus();
            JSlider sli = axes[axis - 1];
            fpb.set(2f * sli.getValue() / (sli.getMaximum() - sli.getMinimum()));
            valaxes[axis - 1] = fpb;
        }
        return fpb;
    }

    @Override
    public void event() {
        for (int i = 0; i < 12; i++) {
            EventStatus e = buttons[i];
            if (e == null) {
                continue;
            }
            boolean state = btns[i].isSelected();
            if (state != states[i]) {
                if (state && e.hasConsumers()) {
                    e.produce();
                }
                states[i] = state;
            }
        }
        for (int i = 0; i < 6; i++) {
            FloatStatus fpb = valaxes[i];
            if (fpb == null) {
                continue;
            }
            JSlider sli = axes[i];
            fpb.set(2f * sli.getValue() / (sli.getMaximum() - sli.getMinimum()));
        }
    }

    @Override
    public FloatInputPoll getAxisChannel(int i) {
        final JSlider axis = axes[i - 1];
        return new FloatInputPoll() {
            @Override
            public float get() {
                return 2f * axis.getValue() / (axis.getMaximum() - axis.getMinimum());
            }
        };
    }

    @Override
    public FloatInputPoll getXChannel() {
        return getAxisChannel(1);
    }

    @Override
    public FloatInputPoll getYChannel() {
        return getAxisChannel(2);
    }

    @Override
    public BooleanInputPoll getButtonChannel(int i) {
        final JToggleButton btn = btns[i - 1];
        return new BooleanInputPoll() {
            @Override
            public boolean get() {
                return btn.isSelected();
            }
        };
    }
}

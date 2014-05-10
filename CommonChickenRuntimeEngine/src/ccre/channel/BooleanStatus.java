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
package ccre.channel;

import ccre.util.CArrayList;
import ccre.util.CArrayUtils;

/**
 * A virtual node that is both a BooleanOutput and a BooleanInput. You can
 * modify its value, read its value, and subscribe to changes in its value.
 * BooleanStatus also provides a number of useful helper functions.
 *
 * @author skeggsc
 */
public class BooleanStatus implements BooleanOutput, BooleanInput {

    /**
     * The current state (true or false) of this BooleanStatus. Do not directly
     * modify this field. Use the writeValue method instead.
     *
     * @see #set(boolean)
     */
    private boolean value;
    /**
     * The list of all the BooleanOutputs to modify when this BooleanStatus
     * changes value.
     *
     * @see #send(ccre.chan.BooleanOutput)
     * @see #unsend(ccre.chan.BooleanOutput)
     */
    private CArrayList<BooleanOutput> consumers = null;
    /**
     * The cached EventOutput that sets the current value to true. Use
     * getSetTrueEvent() instead, because this might be null.
     *
     * @see #getSetTrueEvent()
     */
    private EventOutput setTrue;
    /**
     * The cached EventOutput that sets the current value to false. Use
     * getSetFalseEvent() instead, because this might be null.
     *
     * @see #getSetFalseEvent()
     */
    private EventOutput setFalse;
    /**
     * The cached EventOutput that toggles the current value. Use
     * getToggleEvent() instead, because this might be null.
     *
     * @see #getToggleEvent()
     */
    private EventOutput toggle;

    /**
     * Create a new BooleanStatus with the value of false.
     */
    public BooleanStatus() {
    }

    /**
     * Create a new BooleanStatus with a specified value.
     *
     * @param default_ The default value.
     */
    public BooleanStatus(boolean default_) {
        this.set(default_);
    }

    /**
     * Create a new BooleanStatus with the value of false that automatically
     * updates the specified BooleanOutput with the current state of this
     * BooleanStatus. This is the same as creating a new BooleanStatus and then
     * adding the BooleanOutput as a target.
     *
     * @see BooleanStatus#send(ccre.channel.BooleanOutput)
     * @param target The BooleanOutput to automatically update.
     */
    public BooleanStatus(BooleanOutput target) {
        consumers = new CArrayList<BooleanOutput>();
        consumers.add(target);
        target.set(false);
    }

    /**
     * Create a new BooleanStatus with the value of false that automatically
     * updates all of the specified BooleanOutputs with the current state of
     * this BooleanStatus. This is the same as creating a new BooleanStatus and
     * then adding all of the BooleanOutputs as targets.
     *
     * @see BooleanStatus#send(ccre.channel.BooleanOutput)
     * @param targets The BooleanOutputs to automatically update.
     */
    public BooleanStatus(BooleanOutput... targets) {
        consumers = new CArrayList<BooleanOutput>(CArrayUtils.asList(targets));
        for (BooleanOutput t : targets) {
            t.set(false);
        }
    }

    /**
     * When the specified event occurs, set the status to true.
     *
     * @param event When to set the status to true.
     * @see #getSetTrueEvent()
     */
    public final void setTrueWhen(EventInput event) {
        event.send(getSetTrueEvent());
    }

    /**
     * When the specified event occurs, set the status to false.
     *
     * @param event When to set the status to false.
     * @see #getSetFalseEvent()
     */
    public final void setFalseWhen(EventInput event) {
        event.send(getSetFalseEvent());
    }

    /**
     * When the specified event occurs, toggle the status.
     *
     * @param event When to toggle the status.
     * @see #getToggleEvent()
     */
    public final void toggleWhen(EventInput event) {
        event.send(getToggleEvent());
    }

    /**
     * Get an EventOutput that, when fired, will set the state to true.
     *
     * @return the firable EventOutput.
     * @see #setTrueWhen(ccre.channel.EventInput)
     */
    public final EventOutput getSetTrueEvent() {
        if (setTrue == null) {
            setTrue = new EventOutput() {
                public void event() {
                    set(true);
                }
            };
        }
        return setTrue;
    }

    /**
     * Get an EventOutput that, when fired, will set the state to false.
     *
     * @return the firable EventOutput.
     * @see #setFalseWhen(ccre.channel.EventInput)
     */
    public final EventOutput getSetFalseEvent() {
        if (setFalse == null) {
            setFalse = new EventOutput() {
                public void event() {
                    set(false);
                }
            };
        }
        return setFalse;
    }

    /**
     * Get an EventOutput that, when fired, will toggle the state.
     *
     * @return the firable EventOutput.
     * @see #toggleWhen(ccre.channel.EventInput)
     */
    public final EventOutput getToggleEvent() {
        if (toggle == null) {
            toggle = new EventOutput() {
                public void event() {
                    set(!get());
                }
            };
        }
        return toggle;
    }

    public final synchronized void set(boolean value) {
        if (this.value == value) {
            return;
        }
        this.value = value;
        if (consumers != null) {
            for (BooleanOutput output : consumers) {
                output.set(value);
            }
        }
    }

    public final synchronized boolean get() {
        return value;
    }

    public synchronized void send(BooleanOutput output) {
        if (consumers == null) {
            consumers = new CArrayList<BooleanOutput>();
        }
        consumers.add(output);
        output.set(value);
    }

    public synchronized void unsend(BooleanOutput output) {
        if (consumers != null) {
            if (consumers.remove(output) && consumers.isEmpty()) {
                consumers = null;
            }
        }
    }
}

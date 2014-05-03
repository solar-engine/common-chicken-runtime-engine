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
package ccre.chan;

import ccre.event.EventConsumer;
import ccre.event.EventSource;
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
     * @see BooleanStatus#send(ccre.chan.BooleanOutput)
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
     * @see BooleanStatus#send(ccre.chan.BooleanOutput)
     * @param targets The BooleanOutputs to automatically update.
     */
    public BooleanStatus(BooleanOutput... targets) {
        consumers = new CArrayList<BooleanOutput>(CArrayUtils.asList(targets));
        for (BooleanOutput t : targets) {
            t.set(false);
        }
    }
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
     * The cached EventConsumer that sets the current value to true. Use
     * getSetTrueEvent() instead, because this might be null.
     *
     * @see #getSetTrueEvent()
     */
    private EventConsumer setTrue;
    /**
     * The cached EventConsumer that sets the current value to false. Use
     * getSetFalseEvent() instead, because this might be null.
     *
     * @see #getSetFalseEvent()
     */
    private EventConsumer setFalse;
    /**
     * The cached EventConsumer that toggles the current value. Use
     * getToggleEvent() instead, because this might be null.
     *
     * @see #getToggleEvent()
     */
    private EventConsumer toggle;

    /**
     * When the specified event occurs, set the status to true.
     *
     * @param event When to set the status to true.
     * @see #getSetTrueEvent()
     */
    public final void setTrueWhen(EventSource event) {
        event.addListener(getSetTrueEvent());
    }

    /**
     * When the specified event occurs, set the status to false.
     *
     * @param event When to set the status to false.
     * @see #getSetFalseEvent()
     */
    public final void setFalseWhen(EventSource event) {
        event.addListener(getSetFalseEvent());
    }

    /**
     * When the specified event occurs, toggle the status.
     *
     * @param event When to toggle the status.
     * @see #getToggleEvent()
     */
    public final void toggleWhen(EventSource event) {
        event.addListener(getToggleEvent());
    }

    /**
     * Get an EventConsumer that, when fired, will set the state to true.
     *
     * @return the firable EventConsumer.
     * @see #setTrueWhen(ccre.event.EventSource)
     */
    public final EventConsumer getSetTrueEvent() {
        if (setTrue == null) {
            setTrue = new EventConsumer() {
                public void eventFired() {
                    set(true);
                }
            };
        }
        return setTrue;
    }

    /**
     * Get an EventConsumer that, when fired, will set the state to false.
     *
     * @return the firable EventConsumer.
     * @see #setFalseWhen(ccre.event.EventSource)
     */
    public final EventConsumer getSetFalseEvent() {
        if (setFalse == null) {
            setFalse = new EventConsumer() {
                public void eventFired() {
                    set(false);
                }
            };
        }
        return setFalse;
    }

    /**
     * Get an EventConsumer that, when fired, will toggle the state.
     *
     * @return the firable EventConsumer.
     * @see #toggleWhen(ccre.event.EventSource)
     */
    public final EventConsumer getToggleEvent() {
        if (toggle == null) {
            toggle = new EventConsumer() {
                public void eventFired() {
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

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
package ccre.chan;

import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.holders.FloatTuner;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;

/**
 * A virtual node that is both a FloatOutput and a FloatInput. You can modify
 * its value, read its value, and subscribe to changes in its value. FloatStatus
 * also provides a number of useful helper functions.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @author skeggsc
 */
public class FloatStatus implements FloatOutput, FloatInput, FloatTuner {

    /**
     * Create a new FloatStatus with a value of zero.
     */
    public FloatStatus() {
    }

    /**
     * Create a new FloatStatus with the specified default value.
     *
     * @param value The default value.
     */
    public FloatStatus(float value) {
        this.writeValue(value);
    }

    /**
     * Create a new FloatStatus that automatically updates the specified
     * FloatOutput with the current state of this FloatStatus. This is the same
     * as creating a new FloatStatus and then adding the FloatOutput as a
     * target.
     *
     * @see FloatStatus#addTarget(ccre.chan.FloatOutput)
     * @param target The FloatOutput to automatically update.
     */
    public FloatStatus(FloatOutput target) {
        consumers = new CArrayList<FloatOutput>();
        consumers.add(target);
        target.writeValue(0);
    }

    /**
     * Create a new FloatStatus that automatically updates all of the specified
     * FloatOutputs with the current state of this FloatStatus. This is the same
     * as creating a new FloatStatus and then adding all of the FloatOutputs as
     * targets.
     *
     * @see FloatStatus#addTarget(ccre.chan.FloatOutput)
     * @param targets The FloatOutputs to automatically update.
     */
    public FloatStatus(FloatOutput... targets) {
        consumers = new CArrayList<FloatOutput>(CArrayUtils.asList(targets));
        for (FloatOutput t : targets) {
            t.writeValue(0);
        }
    }
    /**
     * The current state of this FloatStatus. Do not directly modify this field.
     * Use the writeValue method instead.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @see #writeValue(float)
     */
    protected float value = 0;
    /**
     * Has this FloatStatus been modified since this flag was last cleared?
     */
    public boolean hasBeenModified;
    /**
     * The list of all the FloatOutputs to modify when this FloatStatus changes
     * value.
     *
     * @see #addTarget(ccre.chan.FloatOutput)
     * @see #removeTarget(ccre.chan.FloatOutput)
     */
    protected CArrayList<FloatOutput> consumers = null;
    /**
     * By default, setting a FloatStatus to the same value that it already has
     * will have no effect. However, in the case of certain Watchdog setups,
     * this may be unwanted because it would prevent the Watchdog from being fed
     * by the value changing. Set this field to false to pass through all values
     * instead of just changes.
     */
    public boolean optimizeEqualValues = true;

    @Override
    public synchronized float readValue() {
        return value;
    }

    @Override
    public synchronized void writeValue(float newValue) {
        if (value == newValue && optimizeEqualValues) {
            return; // Do nothing
        }
        hasBeenModified = true;
        value = newValue;
        if (consumers != null) {
            for (FloatOutput fws : consumers) {
                fws.writeValue(newValue);
            }
        }
    }

    /**
     * Get an EventConsumer that, when fired, will set the state to the given
     * float.
     *
     * @param value the value to set the state to.
     * @return the firable EventConsumer.
     * @see #setWhen(float, ccre.event.EventSource)
     */
    public EventConsumer getSetEvent(float value) {
        return new SetEvent(this, value);
    }

    /**
     * Implementation detail - used in getSetEvent.
     */
    private static class SetEvent implements EventConsumer {

        private final FloatStatus status;
        private final float value;

        SetEvent(FloatStatus status, float value) {
            this.status = status;
            this.value = value;
        }

        public void eventFired() {
            status.writeValue(value);
        }
    }

    /**
     * When the specified event occurs, set the state to the specified value.
     *
     * @param value the value to set the state to.
     * @param event when to set the status.
     * @see #getSetEvent(float)
     */
    public void setWhen(float value, EventSource event) {
        event.addListener(getSetEvent(value));
    }

    @Override
    public synchronized void addTarget(FloatOutput csm) {
        if (consumers == null) {
            consumers = new CArrayList<FloatOutput>();
        }
        consumers.add(csm);
        csm.writeValue(value);
    }

    @Override
    public synchronized boolean removeTarget(FloatOutput consum) {
        if (consumers != null) {
            boolean out = consumers.remove(consum);
            if (consumers.isEmpty()) {
                consumers = null;
            }
            return out;
        } else {
            return false;
        }
    }

    @Override
    public FloatInputProducer getAutomaticChannel() {
        return null;
    }

    @Override
    public Float getCurrentValue() {
        return readValue();
    }

    @Override
    public void tuneTo(float newValue) {
        writeValue(newValue);
    }
}

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
package ccre.channel;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

import ccre.util.Utils;

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
public class FloatStatus implements FloatOutput, FloatInput, Serializable {

    private static final long serialVersionUID = -579209218982597622L;

    /**
     * The current state of this FloatStatus. Do not directly modify this field.
     * Use the writeValue method instead.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @see #set(float)
     */
    private float value = 0;

    /**
     * The list of all the FloatOutputs to modify when this FloatStatus changes
     * value.
     */
    private final CopyOnWriteArrayList<EventOutput> consumers = new CopyOnWriteArrayList<>();

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
        this.set(value);
    }

    /**
     * Create a new FloatStatus that automatically updates the specified
     * FloatOutput with the current state of this FloatStatus. This is the same
     * as creating a new FloatStatus and then adding the FloatOutput as a
     * target.
     *
     * @see FloatStatus#send(ccre.channel.FloatOutput)
     * @param target The FloatOutput to automatically update.
     */
    public FloatStatus(FloatOutput target) {
        send(target);
    }

    /**
     * Create a new FloatStatus that automatically updates all of the specified
     * FloatOutputs with the current state of this FloatStatus. This is the same
     * as creating a new FloatStatus and then adding all of the FloatOutputs as
     * targets.
     *
     * @see FloatStatus#send(ccre.channel.FloatOutput)
     * @param targets The FloatOutputs to automatically update.
     */
    public FloatStatus(FloatOutput... targets) {
        for (FloatOutput o : targets) {
            send(o);
        }
    }

    public final synchronized float get() {
        return value;
    }

    /**
     * Returns whether or not this has any targets that will get modified when
     * the value changes If this returns false, the set() method will not notify
     * anyone.
     *
     * @return whether or not the set() method would notify any targets.
     * @see #set(float)
     */
    public boolean hasConsumers() {
        return !consumers.isEmpty();
    }

    public final synchronized void set(float newValue) {
        if (Float.floatToIntBits(value) == Float.floatToIntBits(newValue)) {
            return; // Do nothing. We want to ignore the value if it's the same.
        }
        value = newValue;
        for (EventOutput evt : consumers) {
            evt.event();
        }
    }

    /**
     * Returns a version of this status as an output. This is equivalent to
     * upcasting to FloatOutput.
     *
     * @return this status, as an output.
     */
    public FloatOutput asOutput() {
        return this;
    }

    /**
     * Returns a version of this status as an input. This is equivalent to
     * upcasting to FloatInput.
     *
     * @return this status, as an input.
     */
    public FloatInput asInput() {
        return this;
    }

    @Override
    public void onUpdate(EventOutput notify) {
        if (notify == null) {
            throw new NullPointerException();
        }
        consumers.add(notify);
    }

    @Override
    public EventOutput onUpdateR(EventOutput notify) {
        return Utils.addR(consumers, notify);
    }
}

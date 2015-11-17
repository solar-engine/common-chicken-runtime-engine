/*
 * Copyright 2015 Colby Skeggs, Jake Springer
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

/**
 * A FloatIO is both a FloatInput and a FloatOutput.
 * 
 * @author skeggsc
 */
public interface FloatIO extends FloatInput, FloatOutput {

    /**
     * Alias for set(get() + value).
     * 
     * @return the new value of this FloatIO
     */
    public default void accumulate(float value) {
        set(get() + value);
    }

    /**
     * Adds the value of <code>amount</code> to this FloatIO whenever the
     * supplied EventInput fires.
     */
    public default void accumulateWhen(EventInput when, float amount) {
        when.send(eventAccumulate(amount));
    }

    /**
     * Adds the current value of <code>amount</code> to this FloatIO whenever
     * the supplied EventInput fires. 
     */
    public default void accumulateWhen(EventInput when, FloatInput amount) {
        when.send(eventAccumulate(amount));
    }

    /**
     * Gets an EventOutput that, when fired, will add the value of
     * <code>amount</code> to this FloatIO.
     * 
     * @param amount the amount to add
     * @return the EventOutput
     */
    public default EventOutput eventAccumulate(float amount) {
        return () -> accumulate(amount);
    }

    /**
     * Gets an EventOutput that, when fired, will add the current value of
     * <code>amount</code> to this FloatIO.
     * 
     * @param amount the amount to add
     * @return the EventOutput
     */
    public default EventOutput eventAccumulate(FloatInput amount) {
        return () -> accumulate(amount.get());
    }

    /**
     * Returns the output side of this FloatIO. This is equivalent to upcasting
     * to FloatOutput.
     *
     * @return this status, as an output.
     */
    public default FloatOutput asOutput() {
        return this;
    }

    /**
     * Returns the input side of this FloatIO. This is equivalent to upcasting
     * to FloatInput.
     *
     * @return this status, as an input.
     */
    public default FloatInput asInput() {
        return this;
    }

    /**
     * Compose a FloatInput and a FloatOutput into a single FloatIO, which
     * dispatches to the two implementations.
     *
     * @param input the input to dispatch to.
     * @param output the output to dispatch to.
     * @return the composed FloatIO.
     */
    public static FloatIO compose(FloatInput input, FloatOutput output) {
        return new FloatIO() {
            @Override
            public float get() {
                return input.get();
            }

            @Override
            public EventOutput onUpdate(EventOutput notify) {
                return input.onUpdate(notify);
            }

            @Override
            public void set(float value) {
                output.set(value);
            }
        };
    }
}

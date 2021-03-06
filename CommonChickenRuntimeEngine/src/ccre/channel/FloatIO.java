/*
 * Copyright 2015-2016 Cel Skeggs, Jake Springer
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

import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

/**
 * A FloatIO is both a FloatInput and a FloatOutput.
 * 
 * @author skeggsc
 */
public interface FloatIO extends FloatInput, FloatOutput {

    /**
     * Alias for set(get() + value).
     *
     * @param increment the amount to add
     */
    @FlowPhase
    public default void accumulate(float increment) {
        set(get() + increment);
    }

    /**
     * Adds the value of <code>amount</code> to this FloatIO whenever the
     * supplied EventInput fires.
     *
     * @param when when to add to the value
     * @param amount the amount to add
     */
    @SetupPhase
    public default void accumulateWhen(EventInput when, float amount) {
        when.send(eventAccumulate(amount));
    }

    /**
     * Adds the current value of <code>amount</code> to this FloatIO whenever
     * the supplied EventInput fires.
     *
     * @param when when to add to the value
     * @param amount the amount to add
     */
    @SetupPhase
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
    @SetupPhase
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
    @SetupPhase
    public default EventOutput eventAccumulate(FloatInput amount) {
        return () -> accumulate(amount.get());
    }

    /**
     * Returns the output side of this FloatIO. This is equivalent to upcasting
     * to FloatOutput.
     *
     * @return this io, as an output.
     */
    @SetupPhase
    public default FloatOutput asOutput() {
        return this;
    }

    /**
     * Returns the input side of this FloatIO. This is equivalent to upcasting
     * to FloatInput.
     *
     * @return this io, as an input.
     */
    @SetupPhase
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
    @SetupPhase
    public static FloatIO compose(FloatInput input, FloatOutput output) {
        return new FloatIO() {
            @Override
            public float get() {
                return input.get();
            }

            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                return input.onUpdate(notify);
            }

            @Override
            public void set(float value) {
                output.set(value);
            }
        };
    }

    @Override
    @SetupPhase
    public default FloatIO cell(float default_value) {
        this.set(default_value); // replicate behavior of superclass
        return this;
    }
}

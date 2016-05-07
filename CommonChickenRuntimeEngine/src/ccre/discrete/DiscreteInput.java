/*
 * Copyright 2016 Cel Skeggs
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
package ccre.discrete;

import ccre.channel.CancelOutput;
import ccre.channel.UpdatingInput;
import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

/**
 * A DiscreteInput is a way to get the current state of a discrete input, and to
 * subscribe to notifications of changes in the discrete input's value.
 *
 * A DiscreteInput also acts as an UpdatingInput that updates when the value
 * changes, and never updates when the value doesn't change.
 *
 * A DiscreteInput has a specific discrete type. Discrete data is a selection
 * from a list of options, rather than a continuum like floats.
 *
 * @author skeggsc
 * @param <E> the type of the discrete data
 */
public interface DiscreteInput<E> extends UpdatingInput {
    /**
     * Gets the type of this DiscreteInput.
     *
     * @return the type, as a DiscreteType instance.
     */
    @SetupPhase // TODO: maybe ignored phase?
    public DiscreteType<E> getType();

    /**
     * Gets the current value of this discrete input.
     *
     * @return The current value.
     */
    @FlowPhase
    public E get();

    /**
     * Subscribe to changes in this discrete input's value. The discrete output
     * will be modified whenever the value of this input changes.
     *
     * If available, the current value of the input will be written at this
     * time.
     *
     * If the same listener is added multiple times, it has the same effect as
     * if it was added once.
     *
     * @param output The discrete output to notify when the value changes.
     * @return a CancelOutput that deregisters the registered EventOutput. DO
     * NOT FIRE THIS RETURNED EVENT MORE THAN ONCE: UNDEFINED BEHAVIOR MAY
     * RESULT.
     */
    @SetupPhase
    public default CancelOutput send(DiscreteOutput<E> output) {
        if (!output.getType().equals(this.getType())) {
            throw new IllegalArgumentException("Not a compatible discrete type!");
        }
        output.safeSet(get());
        return onUpdate(() -> output.set(get()));
    }
}

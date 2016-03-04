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

import java.util.Objects;

import ccre.channel.CancelOutput;
import ccre.channel.EventOutput;

/**
 * A DiscreteIO is both a DiscreteInput and a DiscreteOutput.
 * 
 * @author skeggsc
 * @param <E> the type of the discrete data
 */
public interface DiscreteIO<E> extends DiscreteInput<E>, DiscreteOutput<E> {

    /**
     * Returns the output side of this DiscreteIO. This is equivalent to
     * upcasting to DiscreteOutput.
     *
     * @return this io, as an output.
     */
    public default DiscreteOutput<E> asOutput() {
        return this;
    }

    /**
     * Returns the input side of this DiscreteIO. This is equivalent to
     * upcasting to DiscreteInput.
     *
     * @return this io, as an input.
     */
    public default DiscreteInput<E> asInput() {
        return this;
    }

    /**
     * Compose a BooleanInput and a BooleanOutput into a single BooleanIO, which
     * dispatches to the two implementations.
     *
     * @param <E> the type of the discrete element.
     * @param input the input to dispatch to.
     * @param output the output to dispatch to.
     * @return the composed BooleanIO.
     */
    public static <E> DiscreteIO<E> compose(DiscreteInput<E> input, DiscreteOutput<E> output) {
        DiscreteType<E> type = input.getType();
        if (!Objects.equals(type, output.getType())) {
            throw new IllegalArgumentException("Mismatched types on input and output of composed DiscreteIO");
        }
        return new DiscreteIO<E>() {
            @Override
            public DiscreteType<E> getType() {
                return type;
            }

            @Override
            public E get() {
                return input.get();
            }

            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                return input.onUpdate(notify);
            }

            @Override
            public void set(E value) {
                output.set(value);
            }
        };
    }
}

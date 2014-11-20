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

/**
 * A BooleanFilter is a wrapper that can be wrapped around any kind of Output,
 * Input, InputPoll, or InputProducer, and will apply the same transformation in
 * any case.
 *
 * @author skeggsc
 */
public abstract class BooleanFilter {

    /**
     * Filter this value according to the subclass's implementation.
     *
     * @param input The input to filter.
     * @return The filtered value.
     */
    public abstract boolean filter(boolean input);

    /**
     * Returns a BooleanInput representing the filtered version of the specified
     * input.
     *
     * @param input The input to filter.
     * @return the filtered input.
     */
    public BooleanInput wrap(BooleanInput input) {
        if (input == null) {
            throw new NullPointerException();
        }
        BooleanStatus out = new BooleanStatus(filter(input.get()));
        input.send(wrap((BooleanOutput) out));
        return out;
    }

    /**
     * Returns a BooleanInputPoll representing the filtered version of the
     * specified input.
     *
     * @param input The input to filter.
     * @return the filtered input.
     */
    public BooleanInputPoll wrap(final BooleanInputPoll input) {
        if (input == null) {
            throw new NullPointerException();
        }
        return new BooleanInputPoll() {
            public boolean get() {
                return filter(input.get());
            }
        };
    }

    /**
     * Returns a BooleanOutput that, when written to, writes the filtered
     * version of the value through to the specified output.
     *
     * @param output the output to write filtered values to.
     * @return the output to write values to in order to filter them.
     */
    public BooleanOutput wrap(final BooleanOutput output) {
        if (output == null) {
            throw new NullPointerException();
        }
        return new BooleanOutput() {
            public void set(boolean value) {
                output.set(filter(value));
            }
        };
    }
}

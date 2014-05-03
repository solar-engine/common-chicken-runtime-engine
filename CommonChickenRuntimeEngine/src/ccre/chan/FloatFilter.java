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

/**
 * A FloatFilter is a wrapper that can be wrapped around any kind of Output,
 * Input, InputPoll, or InputProducer, and will apply the same transformation in
 * any case.
 *
 * @author skeggsc
 */
public abstract class FloatFilter {

    /**
     * Filter this value according to the subclass's implementation.
     *
     * @param input The input to filter.
     * @return The filtered value.
     */
    public abstract float filter(float input);

    /**
     * Returns a FloatInput representing the filtered version of the specified
     * input.
     *
     * @param input The input to filter.
     * @return the filtered input.
     */
    public FloatInput wrap(final FloatInput input) {
        FloatStatus out = new FloatStatus(filter(input.get()));
        input.send(wrap((FloatOutput) out));
        return out;
    }

    /**
     * Returns a FloatInputPoll representing the filtered version of the
     * specified input.
     *
     * @param input The input to filter.
     * @return the filtered input.
     */
    public FloatInputPoll wrap(final FloatInputPoll input) {
        return new FloatInputPoll() {
            public float get() {
                return filter(input.get());
            }
        };
    }

    /**
     * Returns a FloatOutput that, when written to, writes the filtered version
     * of the value through to the specified output.
     *
     * @param output the output to write filtered values to.
     * @return the output to write values to in order to filter them.
     */
    public FloatOutput wrap(final FloatOutput output) {
        return new FloatOutput() {
            public void set(float value) {
                output.set(filter(value));
            }
        };
    }
}

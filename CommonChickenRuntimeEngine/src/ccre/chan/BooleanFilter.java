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
     * @param inp The input to filter.
     * @return the filtered input.
     */
    public BooleanInput wrap(final BooleanInput inp) {
        BooleanInputProducer producer = inp;
        final BooleanInputProducer prod = wrap(producer);
        return new BooleanInput() {
            public boolean readValue() {
                return filter(inp.readValue());
            }

            public void addTarget(BooleanOutput output) {
                prod.addTarget(output);
            }

            public boolean removeTarget(BooleanOutput output) {
                return prod.removeTarget(output);
            }
        };
    }

    /**
     * Returns a BooleanInputProducer representing the filtered version of the
     * specified input.
     *
     * @param prd The input to filter.
     * @return the filtered input.
     */
    public BooleanInputProducer wrap(final BooleanInputProducer prd) {
        BooleanStatus out = new BooleanStatus();
        prd.addTarget(wrap((BooleanOutput) out));
        return out;
    }

    /**
     * Returns a BooleanInputPoll representing the filtered version of the
     * specified input.
     *
     * @param inp The input to filter.
     * @return the filtered input.
     */
    public BooleanInputPoll wrap(final BooleanInputPoll inp) {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return filter(inp.readValue());
            }
        };
    }

    /**
     * Returns a BooleanOutput that, when written to, writes the filtered
     * version of the value through to the specified output.
     *
     * @param out the output to write filtered values to.
     * @return the output to write values to in order to filter them.
     */
    public BooleanOutput wrap(final BooleanOutput out) {
        return new BooleanOutput() {
            public void writeValue(boolean value) {
                out.writeValue(filter(value));
            }
        };
    }
}

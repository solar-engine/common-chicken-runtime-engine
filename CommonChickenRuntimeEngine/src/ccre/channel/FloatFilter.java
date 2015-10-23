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

import ccre.util.Utils;

/**
 * A FloatFilter is a stateless transformer that can be wrapped around any
 * Output or Input.
 *
 * @author skeggsc
 */
public abstract class FloatFilter {

    /**
     * A FloatFilter that negates a value.
     */
    public static final FloatFilter negate = new FloatFilter() {
        @Override
        public float filter(float input) {
            return -input;
        }
    };

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
        if (input == null) {
            throw new NullPointerException();
        }
        FloatStatus out = new FloatStatus(filter(input.get()));
        input.send(wrap((FloatOutput) out));
        return out;
    }

    /**
     * Returns a FloatOutput that, when written to, writes the filtered version
     * of the value through to the specified output.
     *
     * @param output the output to write filtered values to.
     * @return the output to write values to in order to filter them.
     */
    public FloatOutput wrap(final FloatOutput output) {
        if (output == null) {
            throw new NullPointerException();
        }
        return value -> output.set(FloatFilter.this.filter(value));
    }

    /**
     * Return a Filter that applies the specified-size deadzone as defined in
     * Utils.deadzone.
     *
     * @param deadzone The deadzone size to apply, which must be greater than
     * zero and less than infinity.
     * @return The filter representing this deadzone size.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatFilter deadzone(final float deadzone) {
        if (!Float.isFinite(deadzone) || deadzone < 0) {
            throw new IllegalArgumentException("deadzones cannot be NaN, infinite, or less than or equal to zero!");
        }
        return new FloatFilter() {
            @Override
            public float filter(float input) {
                return Utils.deadzone(input, deadzone);
            }
        };
    }

    /**
     * Return a Filter that applies the specified limitation to the value.
     *
     * If the original is NaN, the filtered result is always NaN. If either
     * bound is NaN, then it will be ignored.
     *
     * @param minimum The minimum value to limit to. Use Float.NEGATIVE_INFINITY
     * if you want no lower bound.
     * @param maximum The maximum value to limit to. Use Float.POSITIVE_INFINITY
     * if you want no upper bound.
     * @return The filter representing the specified limit.
     * @throws IllegalArgumentException if maximum is less than minimum
     */
    public static FloatFilter limit(final float minimum, final float maximum) throws IllegalArgumentException {
        if (maximum < minimum) {
            throw new IllegalArgumentException("Maximum is smaller than minimum!");
        }
        return new FloatFilter() {
            @Override
            public float filter(float input) {
                if (input < minimum) {
                    return minimum;
                } else if (input > maximum) {
                    return maximum;
                } else {
                    return input;
                }
            }
        };
    }
}

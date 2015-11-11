/*
 * Copyright 2014-2015 Colby Skeggs
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
 * A FloatOperation provides a generic operation that takes two Float values as
 * inputs.
 *
 * An example of such an operation could be addition, could be used like
 *
 * <code>FloatMixing.sum.of(channelA, channelB)</code>
 *
 * @author skeggsc
 */
public abstract class FloatOperation {

    /**
     * An operation representing summation, aka addition.
     */
    public static final FloatOperation addition = new FloatOperation() {
        @Override
        public float of(float augend, float addend) {
            return augend + addend;
        }
    };
    /**
     * An operation representing a difference, aka subtracting.
     */
    public static final FloatOperation subtraction = new FloatOperation() {
        @Override
        public float of(float minend, float subtrahend) {
            return minend - subtrahend;
        }
    };
    /**
     * An operation representing a product, aka multiplication.
     */
    public static final FloatOperation multiplication = new FloatOperation() {
        @Override
        public float of(float multiplicand, float multiplier) {
            return multiplicand * multiplier;
        }
    };
    /**
     * An operation representing a quotient, aka division.
     */
    public static final FloatOperation division = new FloatOperation() {
        @Override
        public float of(float dividend, float divisor) {
            return dividend / divisor;
        }
    };

    /**
     * Compute the result of the operation for the specified inputs.
     *
     * This should always yield the same result for the same inputs.
     *
     * @param a the first operand to the operation.
     * @param b the second operand to the operation.
     * @return the result of the operation applied to these operands.
     */
    public abstract float of(float a, float b);

    /**
     * Return a new channel that represents the operation applied to the values
     * of the specified channels.
     *
     * @param a a channel representing the first operand to the operation.
     * @param b a channel representing the second operand to the operation.
     * @return a channel representing the result of the operation applied to
     * these operands.
     */
    public FloatInput of(float a, FloatInput b) {
        return new DerivedFloatInput(b) {
            @Override
            protected float apply() {
                return of(a, b.get());
            }
        };
    }

    /**
     * Return a new channel that represents the operation applied to the values
     * of the specified channels.
     *
     * @param a a channel representing the first operand to the operation.
     * @param b a channel representing the second operand to the operation.
     * @return a channel representing the result of the operation applied to
     * these operands.
     */
    public FloatInput of(FloatInput a, float b) {
        return new DerivedFloatInput(a) {
            @Override
            protected float apply() {
                return of(a.get(), b);
            }
        };
    }

    /**
     * Return a new channel that represents the operation applied to the values
     * of the specified channels.
     *
     * @param a a channel representing the first operand to the operation.
     * @param b a channel representing the second operand to the operation.
     * @return a channel representing the result of the operation applied to
     * these operands.
     */
    public FloatInput of(FloatInput a, FloatInput b) {
        return new DerivedFloatInput(a, b) {
            @Override
            protected float apply() {
                return of(a.get(), b.get());
            }
        };
    }

    /**
     * Return a new channel that represents the specified output, with the
     * operation applied with the specified argument.
     *
     * @param out a channel representing the output of the operation.
     * @param b the second operand to the operation.
     * @return a channel representing the first operand to the operation.
     */
    public FloatOutput of(final FloatOutput out, final float b) {
        if (out == null) {
            throw new NullPointerException();
        }
        return value -> out.set(of(value, b));
    }

    /**
     * Return a new channel that represents the specified output, with the
     * operation applied with the specified argument.
     *
     * @param out a channel representing the output of the operation.
     * @param b a channel representing the second operand to the operation.
     * @return a channel representing the first operand to the operation.
     */
    public FloatOutput of(final FloatOutput out, final FloatInput b) {
        if (out == null || b == null) {
            throw new NullPointerException();
        }
        return new FloatOutput() {
            private boolean anyValue;
            private float lastValue;

            {
                b.send((o) -> {
                    if (anyValue) {
                        out.set(of(lastValue, o));
                    }
                });
            }

            public void set(float value) {
                lastValue = value;
                anyValue = true;
                out.set(of(value, b.get()));
            }
        };
    }

    /**
     * Return a new channel that represents the specified output, with the
     * operation applied with the specified argument.
     *
     * @param a the first operand to the operation.
     * @param out a channel representing the output of the operation.
     * @return a channel representing the second operand to the operation.
     */
    public FloatOutput of(final float a, final FloatOutput out) {
        if (out == null) {
            throw new NullPointerException();
        }
        return value -> out.set(of(a, value));
    }

    /**
     * Return a new channel that represents the specified output, with the
     * operation applied with the specified argument.
     *
     * @param a a channel representing the second operand to the operation.
     * @param out a channel representing the output of the operation.
     * @return a channel representing the second operand to the operation.
     */
    public FloatOutput of(final FloatInput a, final FloatOutput out) {
        if (a == null || out == null) {
            throw new NullPointerException();
        }
        return new FloatOutput() {
            private boolean anyValue;
            private float lastValue;

            {
                a.send((o) -> {
                    if (anyValue) {
                        out.set(of(o, lastValue));
                    }
                });
            }

            public void set(float value) {
                lastValue = value;
                anyValue = true;
                out.set(of(a.get(), value));
            }
        };
    }
}

/*
 * Copyright 2014 Colby Skeggs
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
package ccre.ctrl;

import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;

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
    public FloatInputPoll of(final FloatInputPoll a, final FloatInputPoll b) {
        return new FloatInputPoll() {
            public float get() {
                return of(a.get(), b.get());
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
    public FloatInputPoll of(final FloatInputPoll a, final float b) {
        return new FloatInputPoll() {
            public float get() {
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
    public FloatInputPoll of(final float a, final FloatInputPoll b) {
        return new FloatInputPoll() {
            public float get() {
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
    public FloatInput of(float a, FloatInput b) {
        return FloatMixing.createDispatch(of(a, (FloatInputPoll) b), FloatMixing.onUpdate(b));
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
        return FloatMixing.createDispatch(of((FloatInputPoll) a, b), FloatMixing.onUpdate(a));
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
    public FloatInput of(FloatInput a, FloatInputPoll b) {
        return FloatMixing.createDispatch(of((FloatInputPoll) a, b), FloatMixing.onUpdate(a));
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
    public FloatInput of(FloatInputPoll a, FloatInput b) {
        return FloatMixing.createDispatch(of(a, (FloatInputPoll) b), FloatMixing.onUpdate(b));
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
        return FloatMixing.createDispatch(of((FloatInputPoll) a, (FloatInputPoll) b), EventMixing.combine(FloatMixing.onUpdate(a), FloatMixing.onUpdate(b)));
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
        return new FloatOutput() {
            public void set(float value) {
                out.set(of(value, b));
            }
        };
    }

    /**
     * Return a new channel that represents the specified output, with the
     * operation applied with the specified argument.
     *
     * @param out a channel representing the output of the operation.
     * @param b a channel representing the second operand to the operation.
     * @return a channel representing the first operand to the operation.
     */
    public FloatOutput of(final FloatOutput out, final FloatInputPoll b) {
        return new FloatOutput() {
            public void set(float value) {
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
        return new FloatOutput() {
            public void set(float value) {
                out.set(of(a, value));
            }
        };
    }

    /**
     * Return a new channel that represents the specified output, with the
     * operation applied with the specified argument.
     *
     * @param a a channel representing the second operand to the operation.
     * @param out a channel representing the output of the operation.
     * @return a channel representing the second operand to the operation.
     */
    public FloatOutput of(final FloatInputPoll a, final FloatOutput out) {
        return new FloatOutput() {
            public void set(float value) {
                out.set(of(a.get(), value));
            }
        };
    }
}

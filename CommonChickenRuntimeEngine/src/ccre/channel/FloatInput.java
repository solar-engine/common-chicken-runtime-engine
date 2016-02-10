/*
 * Copyright 2013-2016 Cel Skeggs
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

import ccre.timers.PauseTimer;
import ccre.util.Utils;

/**
 * A FloatInput is a way to get the current state of a float input, and to
 * subscribe to notifications of changes in the float input's value.
 *
 * A FloatInput also acts as an UpdatingInput that updates when the value
 * changes, and never updates when the value doesn't change.
 *
 * TODO: Make sure that's actually true everywhere.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @author skeggsc
 */
public interface FloatInput extends UpdatingInput {

    /**
     * Creates a FloatInput that is always the specified value.
     *
     * @param value the value to always have.
     * @return the FloatInput representing that value.
     */
    public static FloatInput always(final float value) {
        return new FloatInput() {
            public float get() {
                return value;
            }

            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                if (notify == null) {
                    throw new NullPointerException();
                }
                return CancelOutput.nothing;
            }
        };
    }

    /**
     * A FloatInput that is always zero.
     *
     * Equivalent to <code>FloatInput.always(0)</code>.
     *
     * @see #always(float)
     */
    public static final FloatInput zero = always(0);

    /**
     * Gets the current value of this float input.
     *
     * @return The current value.
     */
    public float get();

    /**
     * Subscribe to changes in this float input's value. The float output will
     * be modified whenever the value of this input changes.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * If available, the current value of the input will be written at this
     * time.
     *
     * If the same listener is added multiple times, it has the same effect as
     * if it was added once.
     *
     * @param output The float output to notify when the value changes.
     * @return an EventOutput that deregisters the registered EventOutput. DO
     * NOT FIRE THIS RETURNED EVENT MORE THAN ONCE: UNDEFINED BEHAVIOR MAY
     * RESULT.
     */
    public default CancelOutput send(FloatOutput output) {
        output.safeSet(get());
        return onUpdate(() -> output.set(get()));
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput plus
     * the value of <code>other</code>.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput plus(FloatInput other) {
        return FloatOperation.addition.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput minus
     * the value of <code>other</code>.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput minus(FloatInput other) {
        return FloatOperation.subtraction.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is the value of <code>other</code>
     * minus the value of this FloatInput.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput minusRev(FloatInput other) {
        return FloatOperation.subtraction.of(other, this);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput
     * multiplied by the value of <code>other</code>.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput multipliedBy(FloatInput other) {
        return FloatOperation.multiplication.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput divided
     * by the value of <code>other</code>.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput dividedBy(FloatInput other) {
        return FloatOperation.division.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is the value of <code>other</code>
     * divided by the value of this FloatInput.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput dividedByRev(FloatInput other) {
        return FloatOperation.division.of(other, this);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput divided
     * by the value of <code>other</code>.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput modulo(FloatInput other) {
        return FloatOperation.modulation.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is the value of <code>other</code>
     * modulo the value of this FloatInput.
     *
     * @param other the other FloatInput to include.
     * @return the combined FloatInput.
     */
    public default FloatInput moduloRev(FloatInput other) {
        return FloatOperation.modulation.of(other, this);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput plus
     * <code>other</code>.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput plus(float other) {
        return FloatOperation.addition.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput minus
     * <code>other</code>.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput minus(float other) {
        return FloatOperation.subtraction.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is <code>other</code> minus the value
     * of this FloatInput.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput minusRev(float other) {
        return FloatOperation.subtraction.of(other, this);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput
     * multiplied by <code>other</code>.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput multipliedBy(float other) {
        return FloatOperation.multiplication.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput divided
     * by <code>other</code>.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput dividedBy(float other) {
        return FloatOperation.division.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is <code>other</code> divided by the
     * value of this FloatInput.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput dividedByRev(float other) {
        return FloatOperation.division.of(other, this);
    }

    /**
     * Provides a FloatInput whose value is the value of this FloatInput modulo
     * <code>other</code>.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput modulo(float other) {
        return FloatOperation.modulation.of(this, other);
    }

    /**
     * Provides a FloatInput whose value is <code>other</code> modulo the value
     * of this FloatInput.
     *
     * @param other the other value to include.
     * @return the combined FloatInput.
     */
    public default FloatInput moduloRev(float other) {
        return FloatOperation.modulation.of(other, this);
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * at least <code>minimum</code>.
     *
     * @param minimum the lower bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput atLeast(float minimum) {
        if (Float.isNaN(minimum)) {
            throw new IllegalArgumentException("Cannot have NaN boundary in atLeast!");
        }
        return new DerivedBooleanInput(this) {
            @Override
            protected boolean apply() {
                return FloatInput.this.get() >= minimum;
            }
        };
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * at least the value of <code>minimum</code>.
     *
     * @param minimum the lower bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput atLeast(FloatInput minimum) {
        return new DerivedBooleanInput(this, minimum) {
            @Override
            protected boolean apply() {
                return FloatInput.this.get() >= minimum.get();
            }
        };
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * at most <code>maximum</code>.
     *
     * @param maximum the upper bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput atMost(final float maximum) {
        if (Float.isNaN(maximum)) {
            throw new IllegalArgumentException("Cannot have NaN boundary in atMost!");
        }
        return new DerivedBooleanInput(this) {
            @Override
            protected boolean apply() {
                return FloatInput.this.get() <= maximum;
            }
        };
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * at most the value of <code>maximum</code>.
     *
     * @param maximum the upper bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput atMost(final FloatInput maximum) {
        return new DerivedBooleanInput(this, maximum) {
            @Override
            protected boolean apply() {
                return FloatInput.this.get() <= maximum.get();
            }
        };
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * outside the range of <code>minimum</code> to <code>maximum</code>. If it
     * is equal to <code>minimum</code> or <code>maximum</code>, that counts as
     * within the range and so the value will be false.
     *
     * @param minimum the lower bound.
     * @param maximum the upper bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput outsideRange(final float minimum, final float maximum) {
        if (Float.isNaN(minimum) || Float.isNaN(maximum)) {
            throw new IllegalArgumentException("Cannot have NaN boundary in outsideRange!");
        }
        return new DerivedBooleanInput(this) {
            @Override
            protected boolean apply() {
                float value = FloatInput.this.get();
                return value < minimum || value > maximum;
            }
        };
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * outside the range of the value of <code>minimum</code> to the value of
     * <code>maximum</code>. If it is equal to the value of <code>minimum</code>
     * or the value of <code>maximum</code>, that counts as within the range and
     * so the value will be false.
     *
     * @param minimum the lower bound.
     * @param maximum the upper bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput outsideRange(final FloatInput minimum, final FloatInput maximum) {
        return new DerivedBooleanInput(this) {
            @Override
            protected boolean apply() {
                float value = FloatInput.this.get();
                return value < minimum.get() || value > maximum.get();
            }
        };
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * inside the range of <code>minimum</code> to <code>maximum</code>. If it
     * is equal to <code>minimum</code> or <code>maximum</code>, that counts as
     * within the range and so the value will be true.
     *
     * @param minimum the lower bound.
     * @param maximum the upper bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput inRange(final float minimum, final float maximum) {
        if (Float.isNaN(minimum) || Float.isNaN(maximum)) {
            throw new IllegalArgumentException("Cannot have NaN boundary in inRange!");
        }
        return new DerivedBooleanInput(this) {
            public boolean apply() {
                float val = FloatInput.this.get();
                return val >= minimum && val <= maximum;
            }
        };
    }

    /**
     * Provides a BooleanInput that is true iff the value of this FloatInput is
     * inside the range of the value of <code>minimum</code> to the value of
     * <code>maximum</code>. If it is equal to the value of <code>minimum</code>
     * or the value of <code>maximum</code>, that counts as within the range and
     * so the value will be true.
     *
     * @param minimum the lower bound.
     * @param maximum the upper bound.
     * @return the comparison result as a BooleanInput.
     */
    public default BooleanInput inRange(final FloatInput minimum, final FloatInput maximum) {
        return new DerivedBooleanInput(this, minimum, maximum) {
            public boolean apply() {
                float val = FloatInput.this.get();
                return val >= minimum.get() && val <= maximum.get();
            }
        };
    }

    /**
     * Provides a FloatInput whose value is this FloatInput's value, but
     * negated.
     *
     * @return the negated version of this FloatInput.
     */
    public default FloatInput negated() {
        return FloatFilter.negate.wrap(this);
    }

    /**
     * Provides an EventInput that is fired whenever this FloatInput changes by
     * any amount.
     *
     * @return the derived EventInput.
     */
    public default EventInput onChange() {
        return new DerivedEventInput(this) {
            @Override
            protected boolean shouldProduce() {
                return true;
            }
        };
    }

    /**
     * Returns an EventInput that fires whenever the value changes by at least
     * the specified amount, relative to the value the last time it changed.
     *
     * Note that this means that if the value changes by eight at once and the
     * specified value is four, the input will only fire once.
     *
     * @param magnitude the nonnegative and finite number that acts as the
     * threshold for whether or not the event should fire.
     * @return the EventInput to fire.
     * @throws IllegalArgumentException if magnitude is negative, infinite, or
     * NaN.
     */
    public default EventInput onChangeBy(float magnitude) throws IllegalArgumentException {
        if (!Float.isFinite(magnitude) || magnitude < 0) {
            throw new IllegalArgumentException("delta must be nonnegative and finite, but was: " + magnitude);
        }
        final float deltaAbs = Math.abs(magnitude);
        return new DerivedEventInput(this) {
            float last = get();

            protected boolean shouldProduce() {
                float value = get();
                if (Math.abs(last - value) >= deltaAbs) {
                    last = value;
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Provides a version of this FloatInput with a deadzone: if the value is
     * within <code>deadzone</code> of zero, then the result will be zero.
     * Otherwise, the value will be unchanged.
     *
     * @param deadzone the size of the deadzone to apply.
     * @return the deadzoned version of this FloatInput.
     */
    public default FloatInput deadzone(float deadzone) {
        return FloatFilter.deadzone(deadzone).wrap(this);
    }

    /**
     * Provides a translated and scaled version of this FloatInput such that the
     * result will be zero if this FloatInput is equal to <code>zeroV</code>,
     * the result will be one if this FloatInput is equal to <code>oneV</code>,
     * and otherwise the result is interpolated linearly between those points.
     *
     * @param zeroV the value that should be converted to zero.
     * @param oneV the value that should be converted to one.
     * @return the translated and scaled version of this FloatInput.
     * @throws IllegalArgumentException if either of zeroV or oneV are infinite
     * or NaN, or they are the same, or are far apart enough that their
     * difference is infinite or NaN.
     */
    public default FloatInput normalize(float zeroV, float oneV) throws IllegalArgumentException {
        if (!Float.isFinite(zeroV) || !Float.isFinite(oneV)) {
            throw new IllegalArgumentException("Infinite or NaN bound to normalize: " + zeroV + ", " + oneV);
        }
        if (zeroV == oneV) {
            throw new IllegalArgumentException("Equal zero and one bounds to normalize: " + zeroV);
        }
        final float range = oneV - zeroV;
        if (!Float.isFinite(range)) {
            throw new IllegalArgumentException("normalize range is large enough to provide invalid results: " + zeroV + ", " + oneV);
        }
        FloatInput original = this;
        return new DerivedFloatInput(original) {
            protected float apply() {
                return (original.get() - zeroV) / range;
            }
        };
    }

    /**
     * Provides a translated and scaled version of this FloatInput such that the
     * result will be zero if this FloatInput is equal to <code>zeroV</code>,
     * the result will be one if this FloatInput is equal to the value of
     * <code>oneV</code>, and otherwise the result is interpolated linearly
     * between those points.
     *
     * @param zeroV the value that should be converted to zero.
     * @param oneV the input for the value that should be converted to one.
     * @return the translated and scaled version of this FloatInput.
     * @throws IllegalArgumentException if zeroV is infinite or NaN.
     */
    public default FloatInput normalize(final float zeroV, final FloatInput oneV) {
        if (oneV == null) {
            throw new NullPointerException();
        }
        if (!Float.isFinite(zeroV)) {
            throw new IllegalArgumentException("Infinite or NaN zero bound to normalize: " + zeroV);
        }
        FloatInput original = this;
        return new DerivedFloatInput(original, oneV) {
            protected float apply() {
                float deltaN = oneV.get() - zeroV;
                if (deltaN == 0) {
                    return Float.NaN;// as opposed to either infinity or
                                     // negative infinity
                }
                return (original.get() - zeroV) / deltaN;
            }
        };
    }

    /**
     * Provides a translated and scaled version of this FloatInput such that the
     * result will be zero if this FloatInput is equal to the value of
     * <code>zeroV</code>, the result will be one if this FloatInput is equal to
     * <code>oneV</code>, and otherwise the result is interpolated linearly
     * between those points.
     *
     * @param zeroV the input for the value that should be converted to zero.
     * @param oneV the value that should be converted to one.
     * @return the translated and scaled version of this FloatInput.
     * @throws IllegalArgumentException if oneV is infinite or NaN.
     */
    public default FloatInput normalize(final FloatInput zeroV, final float oneV) {
        if (zeroV == null) {
            throw new NullPointerException();
        }
        if (!Float.isFinite(oneV)) {
            throw new IllegalArgumentException("Infinite or NaN one bound to normalize: " + oneV);
        }
        FloatInput original = this;
        return new DerivedFloatInput(original, zeroV) {
            protected float apply() {
                float zeroN = zeroV.get(), deltaN = oneV - zeroN;
                if (deltaN == 0) {
                    return Float.NaN;// as opposed to either infinity or
                                     // negative infinity
                }
                return (original.get() - zeroN) / deltaN;
            }
        };
    }

    /**
     * Provides a translated and scaled version of this FloatInput such that the
     * result will be zero if this FloatInput is equal to the value of
     * <code>zeroV</code>, the result will be one if this FloatInput is equal to
     * the value of <code>oneV</code>, and otherwise the result is interpolated
     * linearly between those points.
     *
     * @param zeroV the input for the value that should be converted to zero.
     * @param oneV the input for the value that should be converted to one.
     * @return the translated and scaled version of this FloatInput.
     */
    public default FloatInput normalize(final FloatInput zeroV, final FloatInput oneV) {
        if (zeroV == null || oneV == null) {
            throw new NullPointerException();
        }
        FloatInput original = this;
        return new DerivedFloatInput(original, zeroV, oneV) {
            protected float apply() {
                float zeroN = zeroV.get(), deltaN = oneV.get() - zeroN;
                if (deltaN == 0) {
                    // as opposed to either infinity or negative infinity
                    return Float.NaN;
                }
                return (original.get() - zeroN) / deltaN;
            }
        };
    }

    /**
     * Provides a version of this FloatInput with ramping applied.
     *
     * @param limit the maximum delta value per time when
     * <code>updateWhen</code> is fired.
     * @param updateWhen when the ramping should update.
     * @return a ramped version of this FloatInput.
     */
    public default FloatInput withRamping(final float limit, EventInput updateWhen) {
        if (updateWhen == null) {
            throw new NullPointerException();
        }
        FloatCell temp = new FloatCell();
        updateWhen.send(this.createRampingEvent(limit, temp));
        return temp;
    }

    /**
     * Provides an event that ramps the value of this FloatInput, and sends the
     * result of the ramping to <code>target</code>.
     *
     * @param limit the maximum delta value per time that the event is fired.
     * @param target the output to control with this ramping.
     * @return an event that continues ramping.
     */
    public default EventOutput createRampingEvent(final float limit, final FloatOutput target) {
        if (target == null) {
            throw new NullPointerException();
        }
        if (Float.isNaN(limit)) {
            throw new IllegalArgumentException("Ramping rate cannot be NaN!");
        }
        return new EventOutput() {
            private float last = get();

            public void event() {
                last = Utils.updateRamping(last, get(), limit);
                target.set(last);
            }
        };
    }

    /**
     * Provides the derivative of this FloatInput as another FloatInput. This
     * will only update when the current value of this FloatInput changes, and
     * will be based on the change and on the amount of time that it took.
     *
     * WARNING: since this only updates when the value changes, it might not be
     * suitable for all applications!
     *
     * @return the derivative of this FloatInput.
     */
    // TODO: find a solution to the limited-update issue.
    public default FloatInput derivative() {
        FloatCell out = new FloatCell();
        FloatOutput deriv = out.viaDerivative();
        onUpdate(() -> deriv.set(get()));
        return out;
    }

    public default FloatInput derivative(int millis) {
        FloatCell out = new FloatCell();
        FloatOutput deriv = out.viaDerivative();
        PauseTimer t = new PauseTimer(millis);

        EventOutput update = t.combine(deriv.eventSet(this));

        t.triggerAtEnd(update);
        onUpdate(update);
        return out;
    }

    /**
     * Provides a version of this FloatInput whose value only changes when
     * <code>allow</code> is true. When <code>allow</code> changes to true, the
     * value is immediately updated and continues to update, and when
     * <code>allow</code> changes to false, the value is locked.
     *
     * @param allow when updating should be allowed.
     * @return the lockable version of this FloatInput.
     */
    public default FloatInput filterUpdates(BooleanInput allow) {
        final FloatInput original = this;
        return new DerivedFloatInput(this, allow) {
            private float lastValue = original.get();

            @Override
            public float apply() {
                if (allow.get()) {
                    lastValue = original.get();
                }
                return lastValue;
            }
        };
    }

    /**
     * Provides a version of this FloatInput whose value only changes when
     * <code>deny</code> is false. When <code>deny</code> changes to false, the
     * value is immediately updated and continues to update, and when
     * <code>deny</code> changes to true, the value is locked.
     *
     * @param deny when updating should be disallowed.
     * @return the lockable version of this FloatInput.
     */
    public default FloatInput filterUpdatesNot(BooleanInput deny) {
        final FloatInput original = this;
        return new DerivedFloatInput(this, deny) {
            private float lastValue = original.get();

            @Override
            public float apply() {
                if (!deny.get()) {
                    lastValue = original.get();
                }
                return lastValue;
            }
        };
    }

    /**
     * Provides a FloatInput that has the same value as this FloatInput, or its
     * negation. If <code>negate</code> is true, it is negated, and if
     * <code>negate</code> is false, it is not negated.
     *
     * @param negate whether or not the input should be negated
     * @return the possibly negated version of this FloatInput
     */
    public default FloatInput negatedIf(BooleanInput negate) {
        return negate.toFloat(this, this.negated());
    }

    /**
     * Provides a FloatInput whose value is this FloatInput's value, but always
     * positive, or in other words an absolute value.
     *
     * @return the absolute value version of this FloatInput.
     */
    public default FloatInput absolute() {
        return FloatFilter.absolute.wrap(this);
    }

    // TODO: integrals!
}

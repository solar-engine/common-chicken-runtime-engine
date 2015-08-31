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
 * A FloatInput is a way to get the current state of a float input, and to
 * subscribe to notifications of changes in the float input's value.
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
            public EventOutput onUpdateR(EventOutput notify) {
                return EventOutput.ignored;
            }
        };
    }

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
     * @see FloatOutput#set(float)
     */
    public default void send(FloatOutput output) {
        output.set(get());
        onUpdate(() -> output.set(get()));
    }

    public default EventOutput sendR(FloatOutput output) {
        output.set(get());
        return onUpdateR(() -> output.set(get()));
    }

    public default FloatInput plus(FloatInput other) {
        return FloatOperation.addition.of(this, other);
    }

    public default FloatInput minus(FloatInput other) {
        return FloatOperation.subtraction.of(this, other);
    }

    public default FloatInput minusRev(FloatInput other) {
        return FloatOperation.subtraction.of(other, this);
    }

    public default FloatInput multipliedBy(FloatInput other) {
        return FloatOperation.multiplication.of(this, other);
    }

    public default FloatInput dividedBy(FloatInput other) {
        return FloatOperation.division.of(this, other);
    }

    public default FloatInput dividedByRev(FloatInput other) {
        return FloatOperation.division.of(other, this);
    }

    public default FloatInput plus(float other) {
        return FloatOperation.addition.of(this, other);
    }

    public default FloatInput minus(float other) {
        return FloatOperation.subtraction.of(this, other);
    }

    public default FloatInput minusRev(float other) {
        return FloatOperation.subtraction.of(other, this);
    }

    public default FloatInput multipliedBy(float other) {
        return FloatOperation.multiplication.of(this, other);
    }

    public default FloatInput dividedBy(float other) {
        return FloatOperation.division.of(this, other);
    }

    public default FloatInput dividedByRev(float other) {
        return FloatOperation.division.of(other, this);
    }

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

    public default BooleanInput atLeast(FloatInput minimum) {
        return new DerivedBooleanInput(this, minimum) {
            @Override
            protected boolean apply() {
                return FloatInput.this.get() >= minimum.get();
            }
        };
    }

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

    public default BooleanInput atMost(final FloatInput maximum) {
        return new DerivedBooleanInput(this, maximum) {
            @Override
            protected boolean apply() {
                return FloatInput.this.get() <= maximum.get();
            }
        };
    }

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

    public default BooleanInput outsideRange(final FloatInput minimum, final FloatInput maximum) {
        return new DerivedBooleanInput(this) {
            @Override
            protected boolean apply() {
                float value = FloatInput.this.get();
                return value < minimum.get() || value > maximum.get();
            }
        };
    }

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

    public default BooleanInput inRange(final FloatInput minimum, final FloatInput maximum) {
        return new DerivedBooleanInput(this, minimum, maximum) {
            public boolean apply() {
                float val = FloatInput.this.get();
                return val >= minimum.get() && val <= maximum.get();
            }
        };
    }

    public default FloatInput negated() {
        return FloatFilter.negate.wrap(this);
    }

    public default EventInput onChange() {
        return new DerivedEventInput(this) {
            @Override
            protected boolean shouldProduce() {
                return true;
            }
        };
    }

    public default EventInput onChangeBy(final float delta) throws IllegalArgumentException {
        if (!Float.isFinite(delta)) {
            throw new IllegalArgumentException("delta must be finite and non-NaN");
        }
        return new DerivedEventInput(this) {
            float last = get();

            protected boolean shouldProduce() {
                float value = get();
                if (Math.abs(last - value) > delta) {
                    last = value;
                    return true;
                }
                return false;
            }
        };
    }

    public default FloatInput deadzone(float deadzone) {
        return FloatFilter.deadzone(deadzone).wrap(this);
    }

    public default FloatInput normalize(final float zero, float one) throws IllegalArgumentException {
        if (!Float.isFinite(zero) || !Float.isFinite(one)) {
            throw new IllegalArgumentException("Infinite or NaN bound to normalizeFloat!");
        }
        if (zero == one) {
            throw new IllegalArgumentException("Equal zero and one bounds to normalizeFloat!");
        }
        final float range = one - zero;
        if (!Float.isFinite(range)) {
            throw new IllegalArgumentException("normalizeFloat range is large enough to provide invalid results");
        }
        FloatInput original = this;
        return new DerivedFloatInput(original) {
            protected float apply() {
                return (original.get() - zero) / range;
            }
        };
    }

    public default FloatInput normalize(final float zero, final FloatInput one) {
        Utils.checkNull(zero, one);
        FloatInput original = this;
        return new DerivedFloatInput(original, one) {
            protected float apply() {
                float deltaN = one.get() - zero;
                if (deltaN == 0) {
                    return Float.NaN;// as opposed to either infinity or negative infinity
                }
                return (original.get() - zero) / deltaN;
            }
        };
    }

    public default FloatInput normalize(final FloatInput zero, final float one) {
        Utils.checkNull(zero, one);
        FloatInput original = this;
        return new DerivedFloatInput(original, zero) {
            protected float apply() {
                float zeroN = zero.get(), deltaN = one - zeroN;
                if (deltaN == 0) {
                    return Float.NaN;// as opposed to either infinity or negative infinity
                }
                return (original.get() - zeroN) / deltaN;
            }
        };
    }

    public default FloatInput normalize(final FloatInput zero, final FloatInput one) {
        Utils.checkNull(zero, one);
        FloatInput original = this;
        return new DerivedFloatInput(original, zero, one) {
            protected float apply() {
                float zeroN = zero.get(), deltaN = one.get() - zeroN;
                if (deltaN == 0) {
                    return Float.NaN;// as opposed to either infinity or negative infinity
                }
                return (original.get() - zeroN) / deltaN;
            }
        };
    }

    public default FloatInput withRamping(final float limit, EventInput updateWhen) {
        Utils.checkNull(updateWhen);
        FloatStatus temp = new FloatStatus();
        updateWhen.send(this.createRampingEvent(limit, temp));
        return temp;
    }

    public default EventOutput createRampingEvent(final float limit, final FloatOutput target) {
        Utils.checkNull(target);
        return new EventOutput() {
            private float last = get();

            public void event() {
                last = Utils.updateRamping(last, get(), limit);
                target.set(last);
            }
        };
    }

    public default FloatInput derivative() {
        final FloatStatus out = new FloatStatus();
        send(out.viaDerivative());
        return out;
    }

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

    public default FloatInput filterUpdatesNot(BooleanInput allow) {
        final FloatInput original = this;
        return new DerivedFloatInput(this, allow) {
            private float lastValue = original.get();

            @Override
            public float apply() {
                if (!allow.get()) {
                    lastValue = original.get();
                }
                return lastValue;
            }
        };
    }
}

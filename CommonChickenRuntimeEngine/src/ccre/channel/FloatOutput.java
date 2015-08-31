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
 * A FloatOutput is an interface for anything that can be set to an analog
 * value.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @see FloatInput
 * @author skeggsc
 */
public interface FloatOutput {

    /**
     * A FloatOutput that goes nowhere. All data sent here is ignored.
     */
    FloatOutput ignored = new FloatOutput() {
        public void set(float newValue) {
        }

        // TODO: optimize?
    };

    /**
     * Set the float value of this output.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @param value The new value to send to this output.
     */
    public void set(float value);

    public default FloatOutput outputPlus(FloatInput other) {
        return FloatOperation.addition.of(this, other);
    }

    public default FloatOutput outputMinus(FloatInput other) {
        return FloatOperation.subtraction.of(this, other);
    }

    public default FloatOutput outputMinusRev(FloatInput other) {
        return FloatOperation.subtraction.of(other, this);
    }

    public default FloatOutput outputMultipliedBy(FloatInput other) {
        return FloatOperation.multiplication.of(this, other);
    }

    public default FloatOutput outputDividedBy(FloatInput other) {
        return FloatOperation.division.of(this, other);
    }

    public default FloatOutput outputDividedByRev(FloatInput other) {
        return FloatOperation.division.of(other, this);
    }

    public default FloatOutput outputPlus(float other) {
        return FloatOperation.addition.of(this, other);
    }

    public default FloatOutput outputMinus(float other) {
        return FloatOperation.subtraction.of(this, other);
    }

    public default FloatOutput outputMinusRev(float other) {
        return FloatOperation.subtraction.of(other, this);
    }

    public default FloatOutput outputMultipliedBy(float other) {
        return FloatOperation.multiplication.of(this, other);
    }

    public default FloatOutput outputDividedBy(float other) {
        return FloatOperation.division.of(this, other);
    }

    public default FloatOutput outputDividedByRev(float other) {
        return FloatOperation.division.of(other, this);
    }

    public default EventOutput getSetEvent(final float value) {
        return () -> set(value);
    }

    public default EventOutput getSetEvent(final FloatInput value) {
        return () -> set(value.get());
    }

    public default void setWhen(float value, EventInput when) {
        Utils.checkNull(when);
        when.send(getSetEvent(value));
    }

    public default void setWhen(FloatInput value, EventInput when) {
        Utils.checkNull(when);
        when.send(getSetEvent(value));
    }

    public default FloatOutput combine(FloatOutput other) {
        FloatOutput original = this;
        return new FloatOutput() {
            @Override
            public void set(float value) {
                original.set(value);
                other.set(value);
            }
        };
    }

    public default FloatOutput negate() {
        return FloatFilter.negate.wrap(this);
    }

    public default FloatOutput outputDeadzone(float deadzone) {
        return FloatFilter.deadzone(deadzone).wrap(this);
    }

    public default FloatOutput addRamping(final float limit, EventInput updateWhen) {
        Utils.checkNull(updateWhen);
        FloatStatus temp = new FloatStatus();
        updateWhen.send(temp.createRampingEvent(limit, this));
        return temp;
    }

    public default FloatOutput viaDerivative() {
        FloatOutput original = this;
        return new FloatOutput() {
            private static final int MILLISECONDS_PER_SECOND = 1000;
            private long lastUpdateMillis = 0;
            private float lastValue = Float.NaN;

            public synchronized void set(float value) {
                if (lastUpdateMillis == 0) {
                    lastValue = value;
                    return;
                }
                long timeMillis = System.currentTimeMillis();
                if (lastUpdateMillis == timeMillis) {
                    return;
                }
                original.set(MILLISECONDS_PER_SECOND * (value - lastValue) / (timeMillis - lastUpdateMillis));
                lastValue = value;
                lastUpdateMillis = timeMillis;
            }
        };
    }

    public default FloatOutput filter(BooleanInput allow) {
        FloatOutput original = this;
        return (value) -> {
            if (allow.get()) {
                original.set(value);
            }
        };
    }

    public default FloatOutput filterNot(BooleanInput deny) {
        FloatOutput original = this;
        return (value) -> {
            if (!deny.get()) {
                original.set(value);
            }
        };
    }

    public default BooleanOutput fromBoolean(final float off, final float on) {
        return fromBoolean(off, on, false);
    }

    public default BooleanOutput fromBoolean(float off, FloatInput on) {
        return fromBoolean(off, on, false);
    }

    public default BooleanOutput fromBoolean(FloatInput off, float on) {
        return fromBoolean(off, on, false);
    }

    public default BooleanOutput fromBoolean(FloatInput off, FloatInput on) {
        return fromBoolean(off, on, false);
    }

    public default BooleanOutput fromBoolean(final float off, final float on, boolean default_) {
        set(default_ ? on : off);
        return new BooleanOutput() {
            private boolean lastValue = default_;
            @Override
            public synchronized void set(boolean value) {
                if (value != lastValue) {
                    lastValue = value;
                    FloatOutput.this.set(value ? on : off);
                }
            }
        };
    }

    public default BooleanOutput fromBoolean(float off, FloatInput on, boolean default_) {
        BooleanStatus temp = new BooleanStatus(default_);
        temp.toFloat(off, on).send(this);
        return temp;
    }

    public default BooleanOutput fromBoolean(FloatInput off, float on, boolean default_) {
        BooleanStatus temp = new BooleanStatus(default_);
        temp.toFloat(off, on).send(this);
        return temp;
    }

    public default BooleanOutput fromBoolean(FloatInput off, FloatInput on, boolean default_) {
        BooleanStatus temp = new BooleanStatus(default_);
        temp.toFloat(off, on).send(this);
        return temp;
    }
}

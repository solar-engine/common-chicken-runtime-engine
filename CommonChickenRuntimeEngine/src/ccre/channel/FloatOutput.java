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

import ccre.time.Time;
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

    public default EventOutput getSetEvent(final float value) {
        return () -> set(value);
    }

    public default EventOutput getSetEvent(final FloatInput value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return () -> set(value.get());
    }

    public default void setWhen(float value, EventInput when) {
        Utils.checkNull(when);
        when.send(getSetEvent(value));
    }

    public default void setWhen(FloatInput value, EventInput when) {
        Utils.checkNull(when, value);
        when.send(getSetEvent(value));
    }

    public default FloatOutput combine(FloatOutput other) {
        if (other == null) {
            throw new NullPointerException();
        }
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
            // not zero because then FakeTime might break...
            private static final long UNINITIALIZED = -1;
            private long lastUpdateNanos = UNINITIALIZED;
            private float lastValue = Float.NaN;

            public synchronized void set(float value) {
                long timeNanos = Time.currentTimeNanos();
                if (lastUpdateNanos == UNINITIALIZED) {
                    lastValue = value;
                    lastUpdateNanos = timeNanos;
                    return;
                }
                if (lastUpdateNanos == timeNanos) {
                    return; // extremely unlikely... but just in case.
                }
                original.set(Time.NANOSECONDS_PER_SECOND * (value - lastValue) / (timeNanos - lastUpdateNanos));
                lastValue = value;
                lastUpdateNanos = timeNanos;
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
        return fromBoolean(FloatInput.always(off), FloatInput.always(on));
    }

    public default BooleanOutput fromBoolean(float off, FloatInput on) {
        return fromBoolean(FloatInput.always(off), on);
    }

    public default BooleanOutput fromBoolean(FloatInput off, float on) {
        return fromBoolean(off, FloatInput.always(on));
    }

    public default BooleanOutput fromBoolean(FloatInput off, FloatInput on) {
        return new BooleanOutput() {
            private boolean lastValue, anyValue = false;

            {
                off.onUpdate(() -> {
                    if (anyValue && !lastValue) {
                        update();
                    }
                });
                on.onUpdate(() -> {
                    if (anyValue && lastValue) {
                        update();
                    }
                });
            }

            @Override
            public synchronized void set(boolean value) {
                if (value != lastValue || !anyValue) {
                    lastValue = value;
                    anyValue = true;
                    update();
                }
            }

            private void update() {
                FloatOutput.this.set(lastValue ? on.get() : off.get());
            }
        };
    }
}

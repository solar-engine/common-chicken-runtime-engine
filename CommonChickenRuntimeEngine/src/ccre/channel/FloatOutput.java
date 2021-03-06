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

import java.io.Serializable;

import ccre.log.Logger;
import ccre.time.Time;
import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

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
public interface FloatOutput extends Serializable {

    /**
     * A FloatOutput that goes nowhere. All data sent here is ignored.
     */
    FloatOutput ignored = new FloatOutput() {
        @Override
        public void set(float newValue) {
        }

        // Important to the functioning of static BooleanOutput.combine
        @Override
        public FloatOutput combine(FloatOutput other) {
            if (other == null) {
                throw new NullPointerException();
            }
            return other;
        };
    };

    /**
     * Sets the float value of this output.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * If any exception occurs during the propagation of the changes, it will be
     * passed on by <code>set</code>.
     *
     * @param value the new value to send to this output.
     * @see #safeSet(float) for a version that catches any errors that occur.
     */
    @FlowPhase
    public void set(float value);

    /**
     * Sets the float value of this output.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * If any exception occurs during the propagation of the changes,
     * <code>safeSet</code> will catch and log it as a
     * {@link ccre.log.LogLevel#SEVERE} error.
     *
     * @param value the new value to send to this output.
     * @see #set(float) for a version that throws any errors that occur.
     */
    @FlowPhase
    public default void safeSet(float value) {
        try {
            set(value);
        } catch (Throwable ex) {
            Logger.severe("Error during channel propagation", ex);
        }
    }

    /**
     * Provides an EventOutput that sets the value of this FloatOutput to
     * <code>value</code>.
     *
     * @param value the value to use.
     * @return an event that sets the value.
     */
    @SetupPhase
    public default EventOutput eventSet(float value) {
        return () -> set(value);
    }

    /**
     * Provides an EventOutput that sets the value of this FloatOutput to the
     * value of <code>value</code>.
     *
     * @param value the input to read the new value from.
     * @return an event that sets the value.
     */
    @SetupPhase
    public default EventOutput eventSet(FloatInput value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return () -> set(value.get());
    }

    /**
     * Sets the value of this FloatOutput to <code>value</code> when
     * <code>when</code> is fired.
     *
     * @param value the value to set.
     * @param when when to set the value.
     */
    @SetupPhase
    public default void setWhen(float value, EventInput when) {
        if (when == null) {
            throw new NullPointerException();
        }
        when.send(eventSet(value));
    }

    /**
     * Sets the value of this FloatOutput to the value of <code>value</code>
     * when <code>when</code> is fired.
     *
     * @param value the input to read the new value from.
     * @param when when to set the value.
     */
    @SetupPhase
    public default void setWhen(FloatInput value, EventInput when) {
        if (value == null || when == null) {
            throw new NullPointerException();
        }
        when.send(eventSet(value));
    }

    /**
     * Provides a FloatOutput that controls both this FloatOutput and
     * <code>other</code>. When the new FloatOutput is set to any number, both
     * this FloatOutput and <code>other</code> will be set to that value.
     *
     * If any error occurs during propagation of changes to either EventOutput,
     * the other target will still be modified. If both throw exceptions, then
     * one of the exceptions will be added as a suppressed exception to the
     * other.
     *
     * @param other the EventOutput to combine this EventOutput with.
     * @return the combined EventOutput.
     */
    @SetupPhase
    public default FloatOutput combine(FloatOutput other) {
        if (other == null) {
            throw new NullPointerException();
        }
        FloatOutput original = this;
        return value -> {
            try {
                original.set(value);
            } catch (Throwable thr) {
                try {
                    other.set(value);
                } catch (Throwable thr2) {
                    thr.addSuppressed(thr2);
                }
                throw thr;
            }
            other.set(value);
        };
    }

    /**
     * Combines any number of FloatOutputs into a single FloatOutput, such that
     * when the returned FloatOutput is set to a value, all of the FloatOutputs
     * are set to that value.
     *
     * If any error occurs during propagation of values to any FloatOutput, the
     * other outputs will still be modified. If multiple outputs throw
     * exceptions, then one of them will be chosen arbitrarily and all of the
     * others will be added as suppressed exceptions either to the thrown
     * exception or to other suppressed expressions.
     *
     * @param outputs the FloatOutputs to include.
     * @return the combined version of the FloatOutputs.
     */
    @SetupPhase
    public static FloatOutput combine(FloatOutput... outputs) {
        // This works without including 'ignored' in the actual data structure
        // by having 'ignored' drop itself during combine.
        FloatOutput out = ignored;
        for (FloatOutput o : outputs) {
            out = out.combine(o);
        }
        return out;
    }

    /**
     * Provide a negated version of this FloatOutput, such that every value is
     * negated before being propagated to this FloatOutput.
     *
     * @return the negated version of this FloatOutput.
     */
    @SetupPhase
    public default FloatOutput negate() {
        return FloatFilter.negate.wrap(this);
    }

    /**
     * Provides a version of this FloatOutput with a deadzone: if a set value is
     * within <code>deadzone</code> of zero, then this FloatInput will be set to
     * zero. Otherwise, this FloatInput will be set to the original value.
     *
     * @param deadzone the size of the deadzone to apply.
     * @return the deadzoned version of this FloatOutput.
     */
    @SetupPhase
    public default FloatOutput outputDeadzone(float deadzone) {
        return FloatFilter.deadzone(deadzone).wrap(this);
    }

    /**
     * Provides a version of this FloatOutput with ramping applied.
     *
     * @param limit the maximum delta value per time when
     * <code>updateWhen</code> is fired.
     * @param updateWhen when the ramping should update.
     * @return a ramped version of this FloatOutput.
     */
    @SetupPhase
    public default FloatOutput addRamping(final float limit, EventInput updateWhen) {
        if (updateWhen == null) {
            throw new NullPointerException();
        }
        FloatCell temp = new FloatCell();
        updateWhen.send(temp.createRampingEvent(limit, this));
        return temp;
    }

    /**
     * Sets this FloatOutput to the derivative of the provided FloatOutput. The
     * values sent to this FloatOutput will be based on the change in value of
     * the provided FloatOutput and the amount of time that it took for the
     * value to change.
     *
     * @return the FloatOutput to take the derivative of.
     */
    @SetupPhase
    public default FloatOutput viaDerivative() {
        FloatOutput original = this;
        return new FloatOutput() {
            // not zero because then FakeTime might break...
            private static final long UNINITIALIZED = -1;
            private long lastUpdateNanos = UNINITIALIZED;
            private float lastValue = Float.NaN;

            @Override
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
                float f = Time.NANOSECONDS_PER_SECOND * (value - lastValue) / (timeNanos - lastUpdateNanos);
                lastValue = value;
                lastUpdateNanos = timeNanos;
                original.set(f);
            }
        };
    }

    /**
     * Provides a version of this FloatOutput that only propagates changes if
     * <code>allow</code> is currently true.
     *
     * When <code>allow</code> changes to false, the output is locked, and when
     * <code>allow</code> changes to true, the output is unlocked and this
     * BooleanOutput is set to its last received value.
     *
     * @param allow when to allow changing of the result.
     * @return the lockable version of this FloatOutput.
     */
    @SetupPhase
    public default FloatOutput filter(BooleanInput allow) {
        FloatOutput original = this;
        return new FloatOutput() {
            private boolean anyValue;
            private float lastValue;

            {
                allow.onPress().send(() -> {
                    if (anyValue) {
                        original.set(lastValue);
                    }
                });
            }

            @Override
            public void set(float value) {
                lastValue = value;
                anyValue = true;
                if (allow.get()) {
                    original.set(value);
                }
            }
        };
    }

    /**
     * Provides a version of this FloatOutput that only propagates changes if
     * <code>deny</code> is currently false.
     *
     * When <code>deny</code> changes to true, the output is locked, and when
     * <code>deny</code> changes to false, the output is unlocked and this
     * BooleanOutput is set to its last received value.
     *
     * @param deny when to deny changing of the result.
     * @return the lockable version of this FloatOutput.
     */
    @SetupPhase
    public default FloatOutput filterNot(BooleanInput deny) {
        return this.filter(deny.not());
    }

    /**
     * Provides a BooleanOutput that controls this FloatOutput by choosing
     * between a value for true and a value for false.
     *
     * @param off the value for this FloatOutput when the BooleanOutput is
     * false.
     * @param on the value for this FloatOutput when the BooleanOutput is true.
     * @return the BooleanOutput that controls this FloatOutput.
     */
    @SetupPhase
    public default BooleanOutput fromBoolean(final float off, final float on) {
        return fromBoolean(FloatInput.always(off), FloatInput.always(on));
    }

    /**
     * Provides a BooleanOutput that controls this FloatOutput by choosing
     * between a value for true and a value for false.
     *
     * @param off the value for this FloatOutput when the BooleanOutput is
     * false.
     * @param on the input representing the value for this FloatOutput when the
     * BooleanOutput is true.
     * @return the BooleanOutput that controls this FloatOutput.
     */
    @SetupPhase
    public default BooleanOutput fromBoolean(float off, FloatInput on) {
        return fromBoolean(FloatInput.always(off), on);
    }

    /**
     * Provides a BooleanOutput that controls this FloatOutput by choosing
     * between a value for true and a value for false.
     *
     * @param off the input representing the value for this FloatOutput when the
     * BooleanOutput is false.
     * @param on the value for this FloatOutput when the BooleanOutput is true.
     * @return the BooleanOutput that controls this FloatOutput.
     */
    @SetupPhase
    public default BooleanOutput fromBoolean(FloatInput off, float on) {
        return fromBoolean(off, FloatInput.always(on));
    }

    /**
     * Provides a BooleanOutput that controls this FloatOutput by choosing
     * between a value for true and a value for false.
     *
     * @param off the input representing the value for this FloatOutput when the
     * BooleanOutput is false.
     * @param on the input representing the value for this FloatOutput when the
     * BooleanOutput is true.
     * @return the BooleanOutput that controls this FloatOutput.
     */
    @SetupPhase
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

            @FlowPhase
            private void update() {
                FloatOutput.this.set(lastValue ? on.get() : off.get());
            }
        };
    }

    /**
     * Provides a FloatOutput that controls this FloatOutput by choosing between
     * an unchanged and a negated version of the values sent to this
     * FloatOutput.
     *
     * @param negate whether or not the output should be negated
     * @return the possibly negated version of this FloatOutput
     */
    @SetupPhase
    public default FloatOutput negateIf(BooleanInput negate) {
        final FloatOutput aThis = this;
        return new FloatOutput() {

            private boolean anyValue;
            private float lastValue;

            {
                negate.send((negate2) -> {
                    if (anyValue) {
                        aThis.set(negate2 ? -lastValue : lastValue);
                    }
                });
            }

            @Override
            public void set(float value) {
                lastValue = value;
                anyValue = true;
                aThis.set(negate.get() ? -value : value);
            }

        };
    }

    /**
     * Returns a FloatIO version of this value. If it is already a FloatIO, it
     * will be returned directly. If it is not, a new FloatCell will be created
     * around this FloatOutput.
     *
     * @param default_value the initial value for the returned FloatIO, which
     * will also be sent to this FloatOutput immediately
     * @return a new IO
     */
    @SetupPhase
    public default FloatIO cell(float default_value) {
        FloatIO fio = new FloatCell(default_value);
        fio.send(this);
        return fio;
    }
}

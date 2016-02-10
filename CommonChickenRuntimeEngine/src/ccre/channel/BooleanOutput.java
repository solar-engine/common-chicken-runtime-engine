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

import ccre.log.Logger;

/**
 * A BooleanOutput is an interface for anything that can be turned on or off. It
 * can be set to true or false.
 *
 * @see BooleanInput
 * @author skeggsc
 */
public interface BooleanOutput {

    /**
     * A BooleanOutput that goes nowhere. All data sent here is ignored.
     */
    BooleanOutput ignored = new BooleanOutput() {
        public void set(boolean newValue) {
            // Do nothing.
        }

        // Important to the functioning of static BooleanOutput.combine
        public BooleanOutput combine(BooleanOutput other) {
            if (other == null) {
                throw new NullPointerException();
            }
            return other;
        };
    };

    /**
     * Sets the boolean value of this output. In other words, turns it on or
     * off.
     *
     * If any exception occurs during the propagation of the changes, it will be
     * passed on by <code>set</code>.
     *
     * @param value the new value to send to this output.
     * @see #safeSet(boolean) for a version that catches any errors that occur.
     */
    public void set(boolean value);

    /**
     * Sets the boolean value of this output. In other words, turns it on or
     * off.
     *
     * If any exception occurs during the propagation of the changes,
     * <code>safeSet</code> will catch and log it as a
     * {@link ccre.log.LogLevel#SEVERE} error.
     *
     * @param value the new value to send to this output.
     * @see #set(boolean) for a version that throws any errors that occur.
     */
    public default void safeSet(boolean value) {
        try {
            set(value);
        } catch (Throwable ex) {
            Logger.severe("Error during channel propagation", ex);
        }
    }

    /**
     * Provides a version of this BooleanOutput that is inverted: when it is set
     * to true, this output will be set to false, and vice versa.
     *
     * @return the inverted version of this BooleanOutput.
     */
    public default BooleanOutput invert() {
        BooleanOutput original = this;
        return new BooleanOutput() {
            @Override
            public void set(boolean value) {
                original.set(!value);
            }

            @Override
            public BooleanOutput invert() {
                return original;
            }
        };
    }

    /**
     * Provides a BooleanOutput that controls both this BooleanOutput and
     * <code>other</code>. When the new BooleanOutput is set to true, both this
     * BooleanOutput and <code>other</code> will be set to true, and the same
     * for false.
     *
     * If any error occurs during propagation of changes to either
     * BooleanOutput, the other target will still be modified. If both throw
     * exceptions, then one of the exceptions will be added as a suppressed
     * exception to the other.
     *
     * @param other the BooleanOutput to combine this BooleanOutput with.
     * @return the combined BooleanOutput.
     */
    public default BooleanOutput combine(BooleanOutput other) {
        if (other == null) {
            throw new NullPointerException();
        }
        BooleanOutput self = this;
        return value -> {
            try {
                self.set(value);
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
     * Combines any number of BooleanOutputs into a single BooleanOutput, such
     * that when the returned BooleanOutput is set to a value, all of the
     * BooleanOutputs are set to that value.
     *
     * If any error occurs during propagation of values to any BooleanOutput,
     * the other outputs will still be modified. If multiple outputs throw
     * exceptions, then one of them will be chosen arbitrarily and all of the
     * others will be added as suppressed exceptions either to the thrown
     * exception or to other suppressed expressions.
     *
     * @param outputs the BooleanOutputs to include.
     * @return the combined version of the BooleanOutputs.
     */
    public static BooleanOutput combine(BooleanOutput... outputs) {
        // This works without including 'ignored' in the actual data structure
        // by having 'ignored' drop itself during combine.
        BooleanOutput out = ignored;
        for (BooleanOutput o : outputs) {
            out = out.combine(o);
        }
        return out;
    }

    /**
     * Provides a version of this BooleanOutput that only propagates to this
     * BooleanOutput when <code>update</code> is fired.
     *
     * When <code>update</code> is fired, and the provided BooleanOutput has had
     * a value set, the most recent value set on that BooleanOutput will be sent
     * to this BooleanOutput.
     *
     * The value of this BooleanOutput will not change when the provided
     * BooleanOutput changes.
     *
     * @param update when to pass values through.
     * @return the update-limited version of this BooleanOutput.
     */
    public default BooleanOutput limitUpdatesTo(EventInput update) {
        if (update == null) {
            throw new NullPointerException();
        }
        BooleanOutput original = this;
        return new BooleanOutput() {
            private boolean lastValue, anyValue;

            {
                update.send(() -> {
                    if (anyValue) {
                        original.set(lastValue);
                    }
                });
            }

            @Override
            public void set(boolean value) {
                this.lastValue = value;
                this.anyValue = true;
            }
        };
    }

    /**
     * Provides an EventOutput that, when fired, will set this BooleanOutput to
     * <code>value</code>.
     *
     * @param value the value to set this BooleanOutput to.
     * @return the EventOutput that modifies this BooleanOutput.
     */
    public default EventOutput eventSet(boolean value) {
        if (value) {
            return () -> set(true);
        } else {
            return () -> set(false);
        }
    }

    /**
     * Provides an EventOutput that, when fired, will set this BooleanOutput to
     * the current value of <code>value</code>.
     *
     * @param value the input to set this BooleanOutput to.
     * @return the EventOutput that modifies this BooleanOutput.
     */
    public default EventOutput eventSet(BooleanInput value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return () -> set(value.get());
    }

    /**
     * Sets this BooleanOutput to <code>value</code> when <code>when</code> is
     * produced.
     *
     * @param value the value to set this BooleanOutput to.
     * @param when when to modify this BooleanOutput.
     */
    public default void setWhen(boolean value, EventInput when) {
        when.send(this.eventSet(value));
    }

    /**
     * Sets this BooleanOutput to the value of <code>value</code> when
     * <code>when</code> fires.
     *
     * @param value the input to set this BooleanOutput to.
     * @param when when to modify this BooleanOutput.
     */
    public default void setWhen(BooleanInput value, EventInput when) {
        when.send(this.eventSet(value));
    }

    /**
     * Sets this BooleanOutput to true when <code>when</code> fires.
     *
     * @param when when to modify this BooleanOutput.
     */
    public default void setTrueWhen(EventInput when) {
        setWhen(true, when);
    }

    /**
     * Sets this BooleanOutput to false when <code>when</code> fires.
     *
     * @param when when to modify this BooleanOutput.
     */
    public default void setFalseWhen(EventInput when) {
        setWhen(false, when);
    }

    /**
     * Returns a BooleanOutput, and when the value written to it changes, it
     * fires the associated event. This will only fire when the value changes,
     * and is false by default.
     *
     * Either parameter can be null, which is equivalent to passing
     * <code>EventOutput.ignored</code>. They cannot both be null - this will
     * throw a NullPointerException.
     *
     * @param toFalse if the output becomes false.
     * @param toTrue if the output becomes true.
     * @return the output that can trigger the events.
     */
    public static BooleanOutput polarize(final EventOutput toFalse, final EventOutput toTrue) {
        if (toFalse == null && toTrue == null) {
            throw new NullPointerException("Both toFalse and toTrue are null in onChange! You can only have at most one be null.");
        }
        return new BooleanOutput() {
            private boolean last;

            public void set(boolean value) {
                if (value == last) {
                    return;
                }
                if (value) {
                    last = true;
                    if (toTrue != null) {
                        toTrue.event();
                    }
                } else {
                    last = false;
                    if (toFalse != null) {
                        toFalse.event();
                    }
                }
            }
        };
    }

    /**
     * Provides a version of this BooleanOutput that only propagates changes if
     * <code>allow</code> is currently true.
     *
     * When <code>allow</code> changes to false, the output is locked, and when
     * <code>allow</code> changes to true, the output is unlocked and this
     * BooleanOutput is set to its last received value.
     *
     * @param allow when to allow changing of the result.
     * @return the lockable version of this BooleanOutput.
     */
    public default BooleanOutput filter(BooleanInput allow) {
        BooleanOutput original = this;
        return new BooleanOutput() {
            private boolean lastValue, anyValue;

            {
                allow.onPress().send(() -> {
                    if (anyValue) {
                        original.set(lastValue);
                    }
                });
            }

            @Override
            public void set(boolean value) {
                lastValue = value;
                anyValue = true;
                if (allow.get()) {
                    original.set(value);
                }
            }
        };
    }

    /**
     * Provides a version of this BooleanOutput that only propagates changes if
     * <code>deny</code> is currently false.
     *
     * When <code>deny</code> changes to true, the output is locked, and when
     * <code>deny</code> changes to false, the output is unlocked and this
     * BooleanOutput is set to its last received value.
     *
     * @param deny when to deny changing of the result.
     * @return the lockable version of this BooleanOutput.
     */
    public default BooleanOutput filterNot(BooleanInput deny) {
        return this.filter(deny.not());
    }

    /**
     * Returns a BooleanIO version of this value. If it is already a BooleanIO,
     * it will be returned directly. If it is not, a new BooleanCell will be
     * created around this BooleanOutput.
     *
     * @param default_value the initial value for the returned BooleanIO, which
     * will also be sent to this BooleanOutput immediately
     * @return a new IO
     */
    public default BooleanIO cell(boolean default_value) {
        BooleanIO bio = new BooleanCell(default_value);
        bio.send(this);
        return bio;
    }
}

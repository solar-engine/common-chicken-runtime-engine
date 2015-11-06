/*
 * Copyright 2013-2015 Colby Skeggs
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
 * A BooleanInput is a way to get the current state of a boolean input, and to
 * subscribe to notifications of changes in the boolean input's value.
 *
 * A BooleanInput also acts as an UpdatingInput that updates when the value
 * changes, and never updates when the value doesn't change.
 * 
 * TODO: Make sure that's actually true everywhere.
 *
 * @author skeggsc
 */
public interface BooleanInput extends UpdatingInput {

    /**
     * A BooleanInput that is always false.
     */
    public static BooleanInput alwaysFalse = new BooleanInput() {
        public boolean get() {
            return false;
        }

        @Override
        public EventOutput onUpdate(EventOutput notify) {
            if (notify == null) {
                throw new NullPointerException();
            }
            return EventOutput.ignored;
        }
    };
    /**
     * A BooleanInput that is always true.
     */
    public static BooleanInput alwaysTrue = new BooleanInput() {
        public boolean get() {
            return true;
        }

        @Override
        public EventOutput onUpdate(EventOutput notify) {
            if (notify == null) {
                throw new NullPointerException();
            }
            return EventOutput.ignored;
        }
    };

    /**
     * Returns a BooleanInput that is always the specified value, and never
     * changes.
     *
     * @see #alwaysFalse
     * @see #alwaysTrue
     * @param b the value that the input should always hold.
     * @return the BooleanInput.
     */
    public static BooleanInput always(boolean b) {
        return b ? alwaysTrue : alwaysFalse;
    }

    /**
     * Gets the current state of this boolean input.
     *
     * @return The current value.
     */
    public boolean get();

    /**
     * Subscribes to changes in this boolean input's value. The boolean output
     * will be modified whenever the value of this input changes. The boolean
     * output will not be modified if the value of this input does not change.
     *
     * TODO: ensure that's true everywhere.
     *
     * If available, the current value of the input will be written at this
     * time.
     *
     * If the same listener is added multiple times, it has the same effect as
     * if it was added once.
     *
     * @param output The boolean output to notify when the value changes.
     * @return an EventOutput that deregisters the registered EventOutput. DO
     * NOT FIRE THIS RETURNED EVENT MORE THAN ONCE: UNDEFINED BEHAVIOR MAY
     * RESULT.
     * @see BooleanOutput#set(boolean)
     */
    public default EventOutput send(BooleanOutput output) {
        output.safeSet(get());
        return onUpdate(() -> output.set(get()));
    }

    /**
     * Provides an inverted version of this BooleanInput.
     *
     * This is defined as:
     * <ul>
     * <li>The get() method always returns the opposite of the original.</li>
     * <li>The onUpdate() and onUpdateR() methods always dispatch to the same
     * methods on the original.</li>
     * <li>Other operations may be overridden, but must have the same effective
     * result implied by the first two rules.</li>
     * </ul>
     *
     * @return the inverted version.
     */
    public default BooleanInput not() {
        BooleanInput original = this;
        return new BooleanInput() {
            @Override
            public EventOutput onUpdate(EventOutput notify) {
                return original.onUpdate(notify);
            }

            @Override
            public boolean get() {
                return !original.get();
            }

            @Override
            public BooleanInput not() {
                return original;
            }
        };
    }

    /**
     * Provides a new BooleanInput that is true exactly when the two specified
     * BooleanInputs are true, and false otherwise.
     *
     * @param b the other BooleanInput.
     * @return the combined BooleanInput.
     */
    public default BooleanInput and(final BooleanInput b) {
        Utils.checkNull(b);
        final BooleanInput a = this;
        return new DerivedBooleanInput(a, b) {
            @Override
            protected boolean apply() {
                return a.get() && b.get();
            }
        };
    }

    /**
     * Provides a new BooleanInput that is true exactly when this BooleanInput
     * is true, and the specified BooleanInput is false, and false otherwise.
     *
     * @param b the other BooleanInput.
     * @return the combined BooleanInput.
     */
    public default BooleanInput andNot(final BooleanInput b) {
        Utils.checkNull(b);
        final BooleanInput a = this;
        return new DerivedBooleanInput(a, b) {
            @Override
            protected boolean apply() {
                return a.get() && !b.get();
            }
        };
    }

    /**
     * Provides a new BooleanInput that is true exactly when the two specified
     * BooleanInputs have different values, and false otherwise.
     *
     * @param b the other BooleanInput.
     * @return the combined BooleanInput.
     */
    public default BooleanInput xor(final BooleanInput b) {
        Utils.checkNull(b);
        final BooleanInput a = this;
        return new DerivedBooleanInput(a, b) {
            @Override
            protected boolean apply() {
                return a.get() ^ b.get();
            }
        };
    }

    /**
     * Provides a new BooleanInput that is true exactly when either of the two
     * specified BooleanInputs is true, and false otherwise.
     *
     * @param b the other BooleanInput.
     * @return the combined BooleanInput.
     */
    public default BooleanInput or(final BooleanInput b) {
        Utils.checkNull(b);
        final BooleanInput a = this;
        return new DerivedBooleanInput(a, b) {
            @Override
            protected boolean apply() {
                return a.get() || b.get();
            }
        };
    }

    /**
     * Provides a new BooleanInput that is true exactly either this BooleanInput
     * is true, or the specified BooleanInput is false, and false otherwise.
     *
     * @param b the other BooleanInput.
     * @return the combined BooleanInput.
     */
    public default BooleanInput orNot(final BooleanInput b) {
        Utils.checkNull(b);
        final BooleanInput a = this;
        return new DerivedBooleanInput(a, b) {
            @Override
            protected boolean apply() {
                return a.get() || !b.get();
            }
        };
    }

    /**
     * Provides an EventInput that is produced exactly once for each time that
     * the BooleanInput changes from false to true.
     *
     * @return the event that occurs when the value changes to true.
     */
    public default EventInput onPress() {
        return new DerivedEventInput(this) {
            @Override
            protected boolean shouldProduce() {
                return get();
            }
        };
    }

    /**
     * Provides an EventInput that is produced exactly once for each time that
     * the BooleanInput changes from true to false.
     *
     * @return the event that occurs when the value changes to false.
     */
    public default EventInput onRelease() {
        return new DerivedEventInput(this) {
            @Override
            protected boolean shouldProduce() {
                return !get();
            }
        };
    }

    /**
     * Provides an EventInput that is produced exactly once for each time that
     * the BooleanInput changes between true and false.
     *
     * @return the event that occurs when the value changes.
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
     * Provides a FloatInput whose value is selected from <code>off</code> and
     * <code>on</code> based on the value of the BooleanInput.
     *
     * @param off the value used when the BooleanInput's value is false.
     * @param on the value used when the BooleanInput's value is true.
     * @return the FloatInput based on this BooleanInput.
     */
    public default FloatInput toFloat(final float off, final float on) {
        return new DerivedFloatInput(this) {
            @Override
            protected float apply() {
                return BooleanInput.this.get() ? on : off;
            }
        };
    }

    /**
     * Provides a FloatInput whose value is selected from <code>off</code> and
     * <code>on</code> based on the value of the BooleanInput.
     *
     * @param off the value used when the BooleanInput's value is false.
     * @param on the input used when the BooleanInput's value is true.
     * @return the FloatInput based on this BooleanInput.
     */
    public default FloatInput toFloat(float off, FloatInput on) {
        return new DerivedFloatInput(this, on) {
            @Override
            protected float apply() {
                return BooleanInput.this.get() ? on.get() : off;
            }
        };
    }

    /**
     * Provides a FloatInput whose value is selected from <code>off</code> and
     * <code>on</code> based on the value of the BooleanInput.
     *
     * @param off the input used when the BooleanInput's value is false.
     * @param on the value used when the BooleanInput's value is true.
     * @return the FloatInput based on this BooleanInput.
     */
    public default FloatInput toFloat(FloatInput off, float on) {
        return new DerivedFloatInput(this, off) {
            @Override
            protected float apply() {
                return BooleanInput.this.get() ? on : off.get();
            }
        };
    }

    /**
     * Provides a FloatInput whose value is selected from <code>off</code> and
     * <code>on</code> based on the value of the BooleanInput.
     *
     * @param off the input used when the BooleanInput's value is false.
     * @param on the input used when the BooleanInput's value is true.
     * @return the FloatInput based on this BooleanInput.
     */
    public default FloatInput toFloat(FloatInput off, FloatInput on) {
        return new DerivedFloatInput(this, off, on) {
            @Override
            protected float apply() {
                return BooleanInput.this.get() ? on.get() : off.get();
            }
        };
    }

    /**
     * Provides a version of this BooleanInput that only changes if
     * <code>allow</code> is currently true.
     *
     * When <code>allow</code> changes to false, the value is locked, and when
     * <code>allow</code> changes to true, the value is unlocked and set to the
     * current value of this BooleanInput.
     *
     * @param allow when to allow changing of the result.
     * @return the lockable version of this BooleanInput.
     */
    public default BooleanInput filterUpdates(BooleanInput allow) {
        final BooleanInput original = this;
        return new DerivedBooleanInput(this, allow) {
            private boolean lastValue = original.get();

            @Override
            public boolean apply() {
                if (allow.get()) {
                    lastValue = original.get();
                }
                return lastValue;
            }
        };
    }

    /**
     * Provides a version of this BooleanInput that only changes if
     * <code>deny</code> is currently false.
     *
     * When <code>deny</code> changes to true, the value is locked, and when
     * <code>deny</code> changes to false, the value is unlocked and set to the
     * current value of this BooleanInput.
     *
     * @param deny when to deny changing of the result.
     * @return the lockable version of this BooleanInput.
     */
    public default BooleanInput filterUpdatesNot(BooleanInput deny) {
        final BooleanInput original = this;
        return new DerivedBooleanInput(this, deny) {
            private boolean lastValue = original.get();

            @Override
            public boolean apply() {
                if (!deny.get()) {
                    lastValue = original.get();
                }
                return lastValue;
            }
        };
    }
}

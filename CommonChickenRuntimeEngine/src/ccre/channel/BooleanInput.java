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

import ccre.ctrl.EventMixing;

/**
 * A BooleanInput is a way to get the current state of a boolean input, and to
 * subscribe to notifications of changes in the boolean input's value.
 *
 * A BooleanInput also acts as an UpdatingInput that updates when the
 * BooleanInput's value changes.
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
        public EventOutput onUpdateR(EventOutput notify) {
            return EventMixing.ignored;
        }

        @Override
        public BooleanInput not() {
            return alwaysTrue;
        };
    };
    /**
     * A BooleanInput that is always true.
     */
    public static BooleanInput alwaysTrue = new BooleanInput() {
        public boolean get() {
            return true;
        }

        @Override
        public EventOutput onUpdateR(EventOutput notify) {
            return EventMixing.ignored;
        }

        @Override
        public BooleanInput not() {
            return alwaysFalse;
        };
    };

    /**
     * Get the current state of this boolean input.
     *
     * @return The current value.
     */
    public boolean get();

    /**
     * Subscribe to changes in this boolean input's value. The boolean output
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
     * @see BooleanOutput#set(boolean)
     */
    public default void send(BooleanOutput output) {
        output.set(get());
        onUpdate(() -> output.set(get()));
    }

    public default EventOutput sendR(BooleanOutput output) {
        output.set(get());
        return onUpdateR(() -> output.set(get()));
    }

    public default BooleanInput not() {
        BooleanInput original = this;
        return new BooleanInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                original.onUpdate(notify);
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                return original.onUpdateR(notify);
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

    public default BooleanInput and(final BooleanInput b) {
        final BooleanInput a = this;
        return new DerivedBooleanInput(a, b) {
            @Override
            protected boolean apply() {
                return a.get() && b.get();
            }
        };
    }

    public default BooleanInput and(final BooleanInput... vals) {
        final BooleanInput a = this;
        return new DerivedBooleanInput(vals, a) {
            @Override
            protected boolean apply() {
                if (!a.get()) {
                    return false;
                }
                for (BooleanInput b : vals) {
                    if (!b.get()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}

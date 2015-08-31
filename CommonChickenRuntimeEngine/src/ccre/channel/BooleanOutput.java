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
    };

    /**
     * Set the boolean value of this output. In other words, turn it on or off.
     *
     * @param value The new value to send to this output.
     */
    public void set(boolean value);

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

    public default BooleanOutput combine(BooleanOutput other) {
        Utils.checkNull(other);
        BooleanOutput self = this;
        return new BooleanOutput() {
            @Override
            public void set(boolean value) {
                self.set(value);
                other.set(value);
            }
        };
    }

    public default BooleanOutput limitUpdatesTo(EventInput update) {
        Utils.checkNull(update);
        return new BooleanOutput() {
            private boolean lastValue, anyValue;

            {
                update.send(new EventOutput() {
                    @Override
                    public void event() {
                        if (anyValue) {
                            set(lastValue);
                        }
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

    public default EventOutput getSetEvent(boolean value) {
        return value ? getSetTrueEvent() : getSetFalseEvent();
    }

    public default EventOutput getSetEvent(BooleanInput value) {
        return () -> set(value.get());
    }

    public default EventOutput getSetTrueEvent() {
        return () -> set(true);
    }

    public default EventOutput getSetFalseEvent() {
        return () -> set(false);
    }

    public default void setWhen(boolean value, EventInput when) {
        when.send(this.getSetEvent(value));
    }

    public default void setWhen(BooleanInput value, EventInput when) {
        when.send(this.getSetEvent(value));
    }

    public default void setTrueWhen(EventInput when) {
        setWhen(true, when);
    }

    public default void setFalseWhen(EventInput when) {
        setWhen(false, when);
    }

    /**
     * Returns a BooleanOutput, and when the value written to it changes, it
     * fires the associated event. This will only fire when the value changes,
     * and is false by default.
     *
     * @param toFalse if the output becomes false.
     * @param toTrue if the output becomes true.
     * @return the output that can trigger the events.
     */
    public static BooleanOutput onChange(final EventOutput toFalse, final EventOutput toTrue) {
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

    public default BooleanOutput filter(BooleanInput allow) {
        BooleanOutput original = this;
        return (value) -> {
            if (allow.get()) {
                original.set(value);
            }
        };
    }

    public default BooleanOutput filterNot(BooleanInput deny) {
        BooleanOutput original = this;
        return (value) -> {
            if (!deny.get()) {
                original.set(value);
            }
        };
    }
}

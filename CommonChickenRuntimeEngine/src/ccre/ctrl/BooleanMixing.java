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
package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.DerivedEventInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;

/**
 * BooleanMixing is a class that provides a wide variety of useful static
 * methods to accomplish various common actions primarily relating to boolean
 * channels.
 *
 * @author skeggsc
 * @see FloatMixing
 * @see EventMixing
 * @see Mixing
 */
public class BooleanMixing {

    /**
     * Returns a BooleanOutput, and when the value written to it changes, it
     * fires the associated event. This will only fire when the value changes,
     * and is false by default.
     *
     * @param toFalse if the output becomes false.
     * @param toTrue if the output becomes true.
     * @return the output that can trigger the events.
     */
    public static BooleanOutput triggerWhenBooleanChanges(final EventOutput toFalse, final EventOutput toTrue) {
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
     * Combine two BooleanOutputs so that any write to the returned output will
     * go to both of the specified outputs.
     *
     * @param a the first output
     * @param b the second output
     * @return the output that will write to both specified outputs.
     */
    public static BooleanOutput combine(final BooleanOutput a, final BooleanOutput b) {
        Mixing.checkNull(a, b);
        return new BooleanOutput() {
            public void set(boolean value) {
                a.set(value);
                b.set(value);
            }
        };
    }

    /**
     * Combine three BooleanOutputs so that any write to the returned output
     * will go to all of the specified outputs.
     *
     * @param a the first output
     * @param b the second output
     * @param c the third output
     * @return the output that will write to all specified outputs.
     */
    public static BooleanOutput combine(final BooleanOutput a, final BooleanOutput b, final BooleanOutput c) {
        Mixing.checkNull(a, b, c);
        return new BooleanOutput() {
            public void set(boolean value) {
                a.set(value);
                b.set(value);
                c.set(value);
            }
        };
    }

    /**
     * Return a BooleanInput that is true when either specified input is true,
     * but not both.
     *
     * @param a the first input.
     * @param b the second input.
     * @return the input representing if either of the given inputs is true, but
     * not both.
     */
    public static BooleanInput xorBooleans(BooleanInput a, BooleanInput b) {
        return new DerivedBooleanInput(a, b) {
            public boolean apply() {
                return a.get() ^ b.get();
            }
        };
    }

    /**
     * Return a BooleanInput that is true when either specified input is true.
     *
     * @param a the first input.
     * @param b the second input.
     * @return the input representing if either of the given inputs is true.
     */
    public static BooleanInput orBooleans(final BooleanInput a, final BooleanInput b) {
        return new DerivedBooleanInput(a, b) {
            @Override
            protected boolean apply() {
                return a.get() || b.get();
            }
        };
    }

    /**
     * Return a BooleanInput that is true when any specified input is true.
     *
     * @param vals the inputs to check.
     * @return the input representing if any given input is true.
     */
    public static BooleanInput orBooleans(final BooleanInput... vals) {
        return new DerivedBooleanInput(vals) {
            @Override
            protected boolean apply() {
                for (BooleanInput inp : vals) {
                    if (inp.get()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns an EventInput that fires when the input changes to the target
     * value.
     *
     * @param input the value to check.
     * @param target the target value to trigger the event.
     * @return the EventInput that is fired when the input becomes the target.
     */
    public static EventInput whenBooleanBecomes(final BooleanInput input, final boolean target) {
        return new DerivedEventInput(input) {
            @Override
            protected boolean shouldProduce() {
                return input.get() == target;
            }
        };
    }

    /**
     * Returns an EventInput that fires when the input becomes true.
     *
     * @param input the value to monitor.
     * @return the EventInput that is fired when the input changes to true.
     */
    public static EventInput onPress(BooleanInput input) {
        return new DerivedEventInput(input) {
            @Override
            protected boolean shouldProduce() {
                return input.get();
            }
        };
    }

    /**
     * Returns an EventInput that fires when the input becomes false.
     *
     * @param input the value to monitor.
     * @return the EventInput that is fired when the input changes to false.
     */
    public static EventInput onRelease(BooleanInput input) {
        return new DerivedEventInput(input) {
            @Override
            protected boolean shouldProduce() {
                return !input.get();
            }
        };
    }

    /**
     * Returns an EventOutput that, when fired, writes the specified value to
     * the specified output.
     *
     * @param output the output to write to.
     * @param value the value to write.
     * @return the event to write the value.
     */
    public static EventOutput getSetEvent(final BooleanOutput output, final boolean value) {
        Mixing.checkNull(output);
        return new EventOutput() {
            public void event() {
                output.set(value);
            }
        };
    }

    /**
     * When the specified EventInput is fired, write the specified value to the
     * specified output
     *
     * @param when when to write the value.
     * @param out the output to write to.
     * @param value the value to write.
     */
    public static void setWhen(EventInput when, BooleanOutput out, boolean value) {
        Mixing.checkNull(when, out);
        when.send(getSetEvent(out, value));
    }

    /**
     * Each time the returned output is fired, the output will be set to an
     * alternating value.
     *
     * NOTE: This in no way reads the current state of the output! It just keeps
     * track internally. Using it in conjunction with anything else to modify
     * the output is likely to not work properly.
     *
     * The first time the event is fired, the output will be set to true. The
     * second time, false. The third time, true. Etcetera.
     *
     * @param out the output to modify.
     * @return when to toggle the output.
     */
    public static EventOutput toggleEvent(final BooleanOutput out) {
        Mixing.checkNull(out);
        return new EventOutput() {
            private boolean value = false;

            public void event() {
                value = !value;
                out.set(value);
            }
        };
    }
    
    public static BooleanOutput onToggle(EventOutput out) {
        Mixing.checkNull(out);
        return new BooleanOutput() {
            private boolean last, wasLast;
            @Override
            public void set(boolean value) {
                if (wasLast) {
                    if (last != value) {
                        out.event();
                    }
                } else {
                    last = value;
                    wasLast = true;
                }
            }
        };
    }

    private BooleanMixing() {
    }

    public static BooleanOutput limitUpdatesTo(BooleanOutput value, EventInput update) {
        BooleanStatus bstat = new BooleanStatus();
        update.send(() -> {
            value.set(bstat.get());
        });
        return bstat;
    }
}

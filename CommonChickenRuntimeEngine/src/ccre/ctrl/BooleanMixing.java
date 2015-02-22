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

import ccre.channel.BooleanFilter;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;

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
     * A BooleanOutput that goes nowhere. All data sent here is ignored.
     */
    public static final BooleanOutput ignoredBooleanOutput = new BooleanOutput() {
        public void set(boolean newValue) {
        }
    };
    /**
     * A BooleanInput that is always false.
     */
    public static final BooleanInput alwaysFalse = new BooleanInput() {
        public boolean get() {
            return false;
        }

        public void send(BooleanOutput consum) {
            consum.set(false);
        }

        public void unsend(BooleanOutput consum) {
        }
    };
    /**
     * A BooleanFilter that inverts a value. (True-&gt;False, False-&gt;True).
     */
    public static final BooleanFilter invert = new BooleanFilter() {
        @Override
        public boolean filter(boolean input) {
            return !input;
        }
    };
    /**
     * A BooleanInput that is always true.
     */
    public static final BooleanInput alwaysTrue = new BooleanInput() {
        public boolean get() {
            return true;
        }

        public void send(BooleanOutput consum) {
            consum.set(true);
        }

        public void unsend(BooleanOutput consum) {
        }
    };

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
     * Returns a BooleanInputPoll that represents the logical inversion of the
     * value of the specified input.
     *
     * @param value the value to invert.
     * @return the inverted value.
     */
    public static BooleanInputPoll invert(final BooleanInputPoll value) {
        return invert.wrap(value);
    }

    /**
     * Returns a BooleanInput that represents the logical inversion of the value
     * of the specified input.
     *
     * @param value the value to invert.
     * @return the inverted value.
     */
    public static BooleanInput invert(BooleanInput value) {
        return invert.wrap(value);
    }

    /**
     * Returns a BooleanOutput that, when written to, writes the logical
     * inversion of the value through to the specified output.
     *
     * @param output the output to write inverted values to.
     * @return the output to write pre-inverted values to.
     */
    public static BooleanOutput invert(final BooleanOutput output) {
        return invert.wrap(output);
    }

    /**
     * Return a BooleanInputPoll that is true when either specified input is
     * true, but not both.
     *
     * @param a the first input.
     * @param b the second input.
     * @return the input representing if either of the given inputs is true, but
     * not both.
     */
    public static BooleanInputPoll xorBooleans(final BooleanInputPoll a, final BooleanInputPoll b) {
        Mixing.checkNull(a, b);
        return new BooleanInputPoll() {
            public boolean get() {
                return a.get() ^ b.get();
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
        Mixing.checkNull(a, b);
        return createDispatch(xorBooleans((BooleanInputPoll) a, (BooleanInputPoll) b), EventMixing.combine(BooleanMixing.whenBooleanChanges(a), BooleanMixing.whenBooleanChanges(b)));
    }

    /**
     * Return a BooleanInputPoll that is true when either specified input is
     * true.
     *
     * @param a the first input.
     * @param b the second input.
     * @return the input representing if either of the given inputs is true.
     */
    public static BooleanInputPoll orBooleans(final BooleanInputPoll a, final BooleanInputPoll b) {
        Mixing.checkNull(a, b);
        return new BooleanInputPoll() {
            public boolean get() {
                return a.get() || b.get();
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
        Mixing.checkNull(a, b);
        MultipleSourceBooleanController out = new MultipleSourceBooleanController(MultipleSourceBooleanController.OR);
        out.addInput(a);
        out.addInput(b);
        return out;
    }

    /**
     * Return a BooleanInputPoll that is true when any specified input is true.
     *
     * @param vals the inputs to check.
     * @return the input representing if any given input is true.
     */
    public static BooleanInputPoll orBooleans(final BooleanInputPoll... vals) {
        Mixing.checkNull((Object[]) vals);
        return new BooleanInputPoll() {
            public boolean get() {
                for (BooleanInputPoll val : vals) {
                    if (val.get()) {
                        return true;
                    }
                }
                return false;
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
        Mixing.checkNull((Object) vals);
        MultipleSourceBooleanController out = new MultipleSourceBooleanController(MultipleSourceBooleanController.OR);
        for (BooleanInput i : vals) {
            out.addInput(i);
        }
        return out;
    }

    /**
     * When the checkTrigger event is fired, check if the boolean has changed.
     * If it has, the returned EventInput is fired.
     *
     * @param input the value to monitor.
     * @param checkTrigger when to check for changes.
     * @return the EventInput that fires when the input changes.
     */
    public static EventInput whenBooleanChanges(BooleanInputPoll input, EventInput checkTrigger) {
        Mixing.checkNull(input, checkTrigger);
        return whenBooleanChanges(createDispatch(input, checkTrigger));
    }

    /**
     * When the BooleanInput changes, the returned EventInput is fired.
     *
     * @param input the value to monitor.
     * @return the EventInput that fires when the input changes.
     */
    public static EventInput whenBooleanChanges(final BooleanInput input) {
        Mixing.checkNull(input);
        final EventStatus out = new EventStatus();
        input.send(new BooleanOutput() {
            private boolean last = input.get();

            public void set(boolean value) {
                if (value == last) {
                    return;
                }
                last = value;
                out.produce();
            }
        });
        return out;
    }

    /**
     * When the checkTrigger event is fired, check if the specified input has
     * changed to the target value since the last check. If it has, then the
     * returned EventInput is fired.
     *
     * @param input the value to check.
     * @param target the target value to trigger the event.
     * @param checkTrigger when to check for changes.
     * @return the EventInput that is fired when the input becomes the target.
     */
    public static EventInput whenBooleanBecomes(BooleanInputPoll input, boolean target, EventInput checkTrigger) {
        Mixing.checkNull(input, checkTrigger);
        return whenBooleanBecomes(createDispatch(input, checkTrigger), target);
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
        Mixing.checkNull(input);
        final EventStatus out = new EventStatus();
        input.send(new BooleanOutput() {
            private boolean last = input.get();

            public void set(boolean value) {
                if (value == last) {
                    return;
                }
                last = value;
                if (value == target) {
                    out.produce();
                }
            }
        });
        return out;
    }

    /**
     * Returns an EventInput that fires when the input becomes true.
     *
     * @param input the value to monitor.
     * @return the EventInput that is fired when the input changes to true.
     */
    public static EventInput onPress(BooleanInput input) {
        Mixing.checkNull(input);
        return whenBooleanBecomes(input, true);
    }

    /**
     * Returns an EventInput that fires when the input becomes false.
     *
     * @param input the value to monitor.
     * @return the EventInput that is fired when the input changes to false.
     */
    public static EventInput onRelease(BooleanInput input) {
        Mixing.checkNull(input);
        return whenBooleanBecomes(input, false);
    }

    /**
     * When the specified event is fired, pump the value from the specified
     * input to the specified output.
     *
     * @param trigger when to pump the value
     * @param in the input
     * @param out the output
     */
    public static void pumpWhen(EventInput trigger, final BooleanInputPoll in, final BooleanOutput out) {
        Mixing.checkNull(trigger, in, out);
        trigger.send(pumpEvent(in, out));
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
     * Returns an EventOutput that, when called, pumps the value from the
     * specified input to the specified output
     *
     * @param in the input
     * @param out the output
     * @return the EventOutput that pumps the value
     */
    public static EventOutput pumpEvent(final BooleanInputPoll in, final BooleanOutput out) {
        Mixing.checkNull(in, out);
        return new EventOutput() {
            public void event() {
                out.set(in.get());
            }
        };
    }

    /**
     * Return a BooleanInput that is the same as the specified BooleanInputPoll,
     * except that it is also a producer that will update whenever the specified
     * event is triggered.
     *
     * @param input the original input.
     * @param trigger the event to dispatch at.
     * @return the dispatchable input.
     */
    public static BooleanInput createDispatch(BooleanInputPoll input, EventInput trigger) {
        Mixing.checkNull(input, trigger);
        BooleanStatus bstat = new BooleanStatus(input.get());
        BooleanMixing.pumpWhen(trigger, input, bstat);
        return bstat;
    }

    /**
     * Return a BooleanInputPoll that is true when both specified inputs are
     * true.
     *
     * @param a the first input.
     * @param b the second input.
     * @return the input representing if both given inputs are true.
     */
    public static BooleanInputPoll andBooleans(final BooleanInputPoll a, final BooleanInputPoll b) {
        Mixing.checkNull(a, b);
        return new BooleanInputPoll() {
            public boolean get() {
                return a.get() && b.get();
            }
        };
    }

    /**
     * Return a BooleanInput that is true when both specified inputs are true.
     *
     * @param a the first input.
     * @param b the second input.
     * @return the input representing if both given inputs are true.
     */
    public static BooleanInput andBooleans(final BooleanInput a, final BooleanInput b) {
        Mixing.checkNull(a, b);
        MultipleSourceBooleanController out = new MultipleSourceBooleanController(MultipleSourceBooleanController.AND);
        out.addInput(a);
        out.addInput(b);
        return out;
    }

    /**
     * Return a BooleanInputPoll that is true when all specified inputs are
     * true.
     *
     * @param vals the inputs to check.
     * @return the input representing if all given inputs are true.
     */
    public static BooleanInputPoll andBooleans(final BooleanInputPoll... vals) {
        Mixing.checkNull((Object[]) vals);
        return new BooleanInputPoll() {
            public boolean get() {
                for (BooleanInputPoll val : vals) {
                    if (!val.get()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Return a BooleanInput that is true when all specified inputs are true.
     *
     * @param vals the inputs to check.
     * @return the input representing if all given inputs are true.
     */
    public static BooleanInput andBooleans(final BooleanInput... vals) {
        Mixing.checkNull((Object[]) vals);
        MultipleSourceBooleanController out = new MultipleSourceBooleanController(MultipleSourceBooleanController.AND);
        for (BooleanInput i : vals) {
            out.addInput(i);
        }
        return out;
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
     * When check is fired, and shouldSet is true, set the output to value.
     *
     * This is intended to be used with a frequent event as check, to
     * effectively hold the output at a specific value.
     *
     * @param check when to update the value.
     * @param shouldSet whether or not the output should be held.
     * @param output the output to hold.
     * @param value the value to hold it at.
     */
    public static void setWhile(EventInput check, BooleanInputPoll shouldSet, BooleanOutput output, boolean value) {
        Mixing.checkNull(check, shouldSet, output);
        setWhen(EventMixing.filterEvent(shouldSet, true, check), output, value);
    }

    /**
     * When check is fired, and shouldSet is false, set the output to value.
     *
     * This is intended to be used with a frequent event as check, to
     * effectively hold the output at a specific value.
     *
     * @param check when to update the value.
     * @param shouldSet whether or not the output should NOT be held.
     * @param output the output to hold.
     * @param value the value to hold it at.
     */
    public static void setWhileNot(EventInput check, BooleanInputPoll shouldSet, BooleanOutput output, boolean value) {
        Mixing.checkNull(check, shouldSet, output);
        setWhen(EventMixing.filterEvent(shouldSet, false, check), output, value);
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

    private BooleanMixing() {
    }
}

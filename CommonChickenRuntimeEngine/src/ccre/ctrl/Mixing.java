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
package ccre.ctrl;

import ccre.chan.*;
import ccre.event.*;
import ccre.log.Logger;
import ccre.ctrl.MixingImpls.*;

/**
 * Mixing is a class that provides a wide variety of useful static methods to
 * accomplish various common actions using channels.
 *
 * Common actions involving teleoperating the robot can be found in DriverImpls.
 *
 * @see DriverImpls
 * @author skeggsc
 */
public class Mixing {

    private Mixing() {
    }
    /**
     * A FloatOutput that goes nowhere. All data sent here is ignored.
     */
    public static final FloatOutput ignoredFloatOutput = new FloatOutput() {
        public void set(float newValue) {
        }
    };
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
     * A FloatFilter that negates a value.
     */
    public static final FloatFilter negate = new FloatFilter() {
        @Override
        public float filter(float input) {
            return -input;
        }
    };
    /**
     * A BooleanFilter that inverts a value. (True->False, False->True).
     */
    public static final BooleanFilter invert = new BooleanFilter() {
        @Override
        public boolean filter(boolean input) {
            return !input;
        }
    };

    /**
     * Creates a FloatInput that is always the specified value.
     *
     * @param value the value to always have.
     * @return the FloatInput representing that value.
     */
    public static FloatInput always(float value) {
        return new Always(value);
    }

    /**
     * Combine two FloatOutputs so that any write to the returned output will go
     * to both of the specified outputs.
     *
     * @param a the first output
     * @param b the second output
     * @return the output that will write to both specified outputs.
     */
    public static FloatOutput combine(final FloatOutput a, final FloatOutput b) {
        return new FloatOutput() {
            public void set(float value) {
                a.set(value);
                b.set(value);
            }
        };
    }

    /**
     * Combine three FloatOutputs so that any write to the returned output will
     * go to all of the specified outputs.
     *
     * @param a the first output
     * @param b the second output
     * @param c the third output
     * @return the output that will write to all specified outputs.
     */
    public static FloatOutput combine(final FloatOutput a, final FloatOutput b, final FloatOutput c) {
        return new FloatOutput() {
            public void set(float value) {
                a.set(value);
                b.set(value);
                c.set(value);
            }
        };
    }

    /**
     * Combine two EventSources so that either event firing will fire the result
     * event.
     *
     * @param a the first event source
     * @param b the second event source
     * @return the source that is fired by either of the original sources.
     */
    public static EventSource combine(EventSource a, EventSource b) {
        Event e = new Event();
        a.addListener(e);
        b.addListener(e);
        return e;
    }

    /**
     * Combine multiple EventSources so that any event firing will fire the
     * result event.
     *
     * @param sources the event sources
     * @return the source that is fired by any of the original sources.
     */
    public static EventSource combine(EventSource... sources) {
        Event e = new Event();
        for (EventSource es : sources) {
            es.addListener(e);
        }
        return e;
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
        return new BooleanOutput() {
            public void set(boolean value) {
                a.set(value);
                b.set(value);
                c.set(value);
            }
        };
    }

    /**
     * Sets the given FloatOutput to one of two values depending on what was
     * written to the returned BooleanOutput.
     *
     * This would be useful if a motor could only be 75% power or 0% power and
     * you wanted to control it using a boolean.
     *
     * @param controlled the FloatOutput to controll.
     * @param off the value to send if the boolean is false.
     * @param on the value to send if the boolean is true.
     * @return the BooleanOutput that will now control the provided FloatOutput.
     */
    public static BooleanOutput select(final FloatOutput controlled, final float off, final float on) {
        return new BSF(controlled, off, on);
    }

    /**
     * Provides a FloatInput that contains a value selected from the two float
     * arguments based on the state of the specified BooleanInput.
     *
     * @param selector the value to select the float value based on.
     * @param off the value to use when false
     * @param on the value to use when true
     * @return the FloatInput calculated from the selector's value and the two
     * floats.
     */
    public static FloatInput select(BooleanInput selector, float off, float on) {
        BCF out = new BCF(selector.get(), off, on);
        selector.send(out);
        return out;
    }

    /**
     * Provides a FloatInput that contains a value selected from the two float
     * arguments based on the state of the specified BooleanInputProducer.
     *
     * @param selector the value to select the float value based on.
     * @param default_ the value to assume for the BooleanInputProducer before
     * any changes are detected.
     * @param off the value to use when false
     * @param on the value to use when true
     * @return the FloatInput calculated from the selector's value and the two
     * floats.
     */
    public static FloatInput select(BooleanInput selector, boolean default_, float off, float on) {
        BCF out = new BCF(default_, off, on);
        selector.send(out);
        return out;
    }

    /**
     * Provides a FloatInputPoll that contains a value selected from the two
     * float arguments based on the state of the specified BooleanInputPool.
     *
     * @param selector the value to select the float value based on.
     * @param off the value to use when false
     * @param on the value to use when true
     * @return the FloatInputPoll calculated from the selector's value and the
     * two floats.
     */
    public static FloatInputPoll select(BooleanInputPoll selector, float off, float on) {
        return new BSF2(selector, off, on);
    }

    /**
     * The returned BooleanOutput is a way to modify the specified target. When
     * the BooleanOutput is changed, the target is set to the current value of
     * the associated parameter (the on parameter if true, the off parameter if
     * false).
     *
     * Warning: changes to the FloatInputPoll parameters will not modify the
     * output until the BooleanOutput is written to!
     *
     * @param target the FloatOutput to write to.
     * @param off the value to write if the written boolean is false.
     * @param on the value to write if the written boolean is true.
     * @return the BooleanOutput that will modify the specified target.
     */
    public static BooleanOutput select(final FloatOutput target, final FloatInputPoll off, final FloatInputPoll on) {
        return new BooleanOutput() {
            public void set(boolean value) {
                target.set(value ? on.get() : off.get());
            }
        };
    }

    /**
     * Returns a FloatInput with a value selected from two FloatInputPolls based
     * on the BooleanInput's value.
     *
     * Warning: changes to the FloatInputPoll parameters will not modify the
     * output until the BooleanInput changes!
     *
     * @param selector the selector to choose an input using.
     * @param off if the selector is false.
     * @param on if the selector is true.
     * @return the value selected based on the selector's value and the statuses
     * of the two arguments.
     */
    public static FloatInput select(BooleanInput selector, FloatInputPoll off, FloatInputPoll on) {
        BCF2 out = new BCF2(selector.get(), off, on);
        selector.send(out);
        return out;
    }

    /**
     * Returns a FloatInput with a value selected from two FloatInputPolls based
     * on the BooleanInputProducer's value.
     *
     * Warning: changes to the FloatInputPoll parameters will not modify the
     * output until the BooleanInputProducer changes!
     *
     * @param selector the selector to choose an input using.
     * @param default_ the value to default the selector to before it changes.
     * @param off if the selector is false.
     * @param on if the selector is true.
     * @return the value selected based on the selector's value and the statuses
     * of the two arguments.
     */
    public static FloatInput select(BooleanInput selector, boolean default_, FloatInputPoll off, FloatInputPoll on) {
        BCF2 out = new BCF2(default_, off, on);
        selector.send(out);
        return out;
    }

    /**
     * Returns a FloatInputPoll with a value selected from two specified
     * FloatInputPolls based on the BooleanInputPoll's value.
     *
     * @param selector the selector to choose an input using.
     * @param off if the selector is false.
     * @param on if the selector is true.
     * @return the value selected based on the selector's value and the statuses
     * of the two arguments.
     */
    public static FloatInputPoll select(final BooleanInputPoll selector, final FloatInputPoll off, final FloatInputPoll on) {
        return new BooleanSelectFloatImpl(selector, on, off);
    }

    /**
     * Return a Filter that applies the specified-size deadzone as defined in
     * Utils.deadzone.
     *
     * @param deadzone The deadzone size to apply.
     * @return The filter representing this deadzone size.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatFilter deadzone(final float deadzone) {
        return new DeadzoneImpl(deadzone);
    }

    /**
     * Return a Filter that applies the specified limitation to the value.
     *
     * @param minimum The minimum value to limit to. Use Float.NEGATIVE_INFINITY
     * if you want no lower bound.
     * @param maximum The maximum value to limit to. Use Float.POSITIVE_INFINITY
     * if you want no upper bound.
     * @return The filter representing the specified limit.
     */
    public static FloatFilter limit(float minimum, float maximum) {
        return new LimitImpl(minimum, maximum);
    }

    /**
     * Returns a FloatInputPoll with a deadzone applied as defined in
     * Utils.deadzone
     *
     * @param inp the input representing the current value.
     * @param range the deadzone to apply.
     * @return the input representing the deadzone applied to the specified
     * value.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatInputPoll deadzone(FloatInputPoll inp, float range) {
        return deadzone(range).wrap(inp);
    }

    /**
     * Returns a FloatInput with a deadzone applied as specified in
     * Utils.deadzone.
     *
     * @param inp the input representing the current value.
     * @param range the deadzone to apply.
     * @return the input representing the deadzone applied to the specified
     * value.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatInput deadzone(FloatInput inp, float range) {
        return deadzone(range).wrap(inp);
    }

    /**
     * Returns a FloatOutput that writes through a deadzoned version of any
     * values written to it. Deadzones values as specified in Utils.deadzone.
     *
     * @param out the output to write deadzoned values to.
     * @param range the deadzone to apply.
     * @return the output that writes deadzoned values through to the specified
     * output.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatOutput deadzone(final FloatOutput out, final float range) {
        return deadzone(range).wrap(out);
    }

    /**
     * Returns a FloatInputPoll representing the negated version of the
     * specified input.
     *
     * @param value the input to negate.
     * @return the negated input.
     */
    public static FloatInputPoll negate(final FloatInputPoll value) {
        return negate.wrap(value);
    }

    /**
     * Returns a FloatInput representing the negated version of the specified
     * input.
     *
     * @param value the input to negate.
     * @return the negated input.
     */
    public static FloatInput negate(FloatInput value) {
        return negate.wrap(value);
    }

    /**
     * Returns a FloatOutput that, when written to, writes the negation of the
     * value through to the specified output.
     *
     * @param output the output to write negated values to.
     * @return the output to write pre-negated values to.
     */
    public static FloatOutput negate(final FloatOutput output) {
        return negate.wrap(output);
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
     * Returns an EventConsumer that, when fired, writes the specified value to
     * the specified output.
     *
     * @param output the output to write to.
     * @param value the value to write.
     * @return the event to write the value.
     */
    public static EventConsumer getSetEvent(FloatOutput output, float value) {
        return new GSEF(output, value);
    }

    /**
     * Returns an EventConsumer that, when fired, writes the specified value to
     * the specified output.
     *
     * @param output the output to write to.
     * @param value the value to write.
     * @return the event to write the value.
     */
    public static EventConsumer getSetEvent(BooleanOutput output, boolean value) {
        return new GSEB(output, value);
    }

    /**
     * When the specified EventSource is fired, write the specified value to the
     * specified output
     *
     * @param when when to write the value.
     * @param out the output to write to.
     * @param value the value to write.
     */
    public static void setWhen(EventSource when, FloatOutput out, float value) {
        when.addListener(getSetEvent(out, value));
    }

    /**
     * When the specified EventSource is fired, write the specified value to the
     * specified output
     *
     * @param when when to write the value.
     * @param out the output to write to.
     * @param value the value to write.
     */
    public static void setWhen(EventSource when, BooleanOutput out, boolean value) {
        when.addListener(getSetEvent(out, value));
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
    public static BooleanOutput triggerWhenBooleanChanges(final EventConsumer toFalse, final EventConsumer toTrue) {
        return new BooleanOutput() {
            private boolean last;

            public void set(boolean value) {
                if (value == last) {
                    return;
                }
                if (value) {
                    last = true;
                    if (toTrue != null) {
                        toTrue.eventFired();
                    }
                } else {
                    last = false;
                    if (toFalse != null) {
                        toFalse.eventFired();
                    }
                }
            }
        };
    }

    /**
     * When the checkTrigger event is fired, check if the specified input has
     * changed to the target value since the last check. If it has, then the
     * returned EventSource is fired.
     *
     * @param input the value to check.
     * @param target the target value to trigger the event.
     * @param checkTrigger when to check for changes.
     * @return the EventSource that is fired when the input becomes the target.
     */
    public static EventSource whenBooleanBecomes(BooleanInputPoll input, boolean target, EventSource checkTrigger) {
        return whenBooleanBecomes(createDispatch(input, checkTrigger), target);
    }

    /**
     * Returns an EventSource that fires when the input changes to the target
     * value.
     *
     * @param input the value to check.
     * @param target the target value to trigger the event.
     * @return the EventSource that is fired when the input becomes the target.
     */
    public static EventSource whenBooleanBecomes(BooleanInput input, boolean target) {
        final Event out = new Event();
        input.send(new WBBI(target, out));
        return out;
    }

    /**
     * When the returned EventConsumer is fired and the specified
     * BooleanInputPoll is the specified requirement, fire the passed
     * EventConsumer.
     *
     * @param input the input to test.
     * @param requirement the value to require.
     * @param target the target to fire.
     * @return when to check if the target should be fired.
     */
    public static EventConsumer filterEvent(BooleanInputPoll input, boolean requirement, EventConsumer target) {
        return new FEC(input, requirement, target);
    }

    /**
     * Return an EventSource that is fired when the specified EventSource is
     * fired and the specified BooleanInputPoll is the specified requirement.
     *
     * @param input the input to test.
     * @param requirement the value to require.
     * @param when when to check if the target should be fired.
     * @return the target to fire.
     */
    public static EventSource filterEvent(BooleanInputPoll input, boolean requirement, EventSource when) {
        final Event out = new Event();
        when.addListener(new FES(input, requirement, out));
        return out;
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
        return new AndBooleansImpl2(a, b);
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
        return new OrBooleansImpl2(a, b);
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
        return new XorBooleansImpl(a, b);
    }

    /**
     * Return a BooleanInputPoll that is true when all specified inputs are
     * true.
     *
     * @param vals the inputs to check.
     * @return the input representing if all given inputs are true.
     */
    public static BooleanInputPoll andBooleans(final BooleanInputPoll... vals) {
        return new AndBooleansImpl(vals);
    }

    /**
     * Return a BooleanInputPoll that is true when any specified input is true.
     *
     * @param vals the inputs to check.
     * @return the input representing if any given input is true.
     */
    public static BooleanInputPoll orBooleans(final BooleanInputPoll... vals) {
        return new OrBooleansImpl(vals);
    }

    /**
     * Returns an EventConsumer that, when called, pumps the value from the
     * specified input to the specified output
     *
     * @param in the input
     * @param out the output
     * @return the EventConsumer that pumps the value
     */
    public static EventConsumer pumpEvent(final BooleanInputPoll in, final BooleanOutput out) {
        return new PumpEventImplB(out, in);
    }

    /**
     * Returns an EventConsumer that, when called, pumps the value from the
     * specified input to the specified output
     *
     * @param in the input
     * @param out the output
     * @return the EventConsumer that pumps the value
     */
    public static EventConsumer pumpEvent(final FloatInputPoll in, final FloatOutput out) {
        return new PumpEventImplF(out, in);
    }

    /**
     * When the specified event is fired, pump the value from the specified
     * input to the specified output.
     *
     * @param trigger when to pump the value
     * @param in the input
     * @param out the output
     */
    public static void pumpWhen(EventSource trigger, final BooleanInputPoll in, final BooleanOutput out) {
        trigger.addListener(pumpEvent(in, out));
    }

    /**
     * When the specified event is fired, pump the value from the specified
     * input to the specified output.
     *
     * @param trigger when to pump the value
     * @param in the input
     * @param out the output
     */
    public static void pumpWhen(EventSource trigger, final FloatInputPoll in, final FloatOutput out) {
        trigger.addListener(pumpEvent(in, out));
    }

    /**
     * Returns a combination of the specified events such that the returned
     * EventConsumer will fire all arguments when fired.
     *
     * @param events the events to fire
     * @return the trigger for firing the arguments.
     */
    public static EventConsumer combine(final EventConsumer... events) {
        return new EventConsumer() {
            public void eventFired() {
                for (EventConsumer cnsm : events) {
                    cnsm.eventFired();
                }
            }
        };
    }

    /**
     * Returns a combination of the specified events such that the returned
     * EventConsumer will fire both arguments when fired.
     *
     * @param a the first event
     * @param b the second event
     * @return the trigger for firing the arguments.
     */
    public static EventConsumer combine(final EventConsumer a, final EventConsumer b) {
        return new EventConsumer() {
            public void eventFired() {
                a.eventFired();
                b.eventFired();
            }
        };
    }

    /**
     * Returns a scaled version of the specified input, such that when the value
     * from the specified input is the value in the one parameter, the output is
     * 1.0, and when the value from the specified input is the value in the zero
     * parameter, the output is 0.0. The value is linearly scaled, for example:
     * a value of ((zero + one) / 2) will create an output of 0.5. There is no
     * capping - the output can be any number, including a number out of the
     * range of zero to one.
     *
     * The scaling is equivalent to:
     * <code>(base.readValue() - zero) / (one - zero)</code>
     *
     * @param base the value to scale.
     * @param zero the value of base that turns into 0.0.
     * @param one the value of base that turns into 1.0.
     * @return the scaled value.
     */
    public static FloatInputPoll normalizeFloat(FloatInputPoll base, float zero, float one) {
        float range = one - zero;
        return new NFI(base, zero, range);
    }

    /**
     * Return a BooleanInputPoll that is true when the specified float input is
     * at least the specified minimum value.
     *
     * @param base the value to test
     * @param minimum the minimum value
     * @return an input that represents the value being at least the minimum.
     */
    public static BooleanInputPoll floatIsAtLeast(final FloatInputPoll base, final float minimum) {
        return new FIAL(base, minimum);
    }

    /**
     * Return a BooleanInputPoll that is true when the specified float input is
     * at most the specified maximum value.
     *
     * @param base the value to test
     * @param maximum the maximum value
     * @return an input that represents the value being at most the maximum.
     */
    public static BooleanInputPoll floatIsAtMost(final FloatInputPoll base, final float maximum) {
        return new FIAM(base, maximum);
    }

    /**
     * Return a BooleanInputPoll that is true when the specified float input is
     * in the range of the specified minimum and maximum, inclusive.
     *
     * @param base the value to test
     * @param minimum the minimum value
     * @param maximum the maximum value
     * @return an input that represents the value being in range
     */
    public static BooleanInputPoll floatIsInRange(final FloatInputPoll base, final float minimum, final float maximum) {
        return new FIIR(base, minimum, maximum);
    }

    /**
     * Return a BooleanInputPoll that is true when the specified float input is
     * outside of the range of the specified minimum and maximum. It will be
     * false at the minimum or maximum.
     *
     * @param base the value to test
     * @param minimum the minimum value
     * @param maximum the maximum value
     * @return an input that represents the value being outside the range
     */
    public static BooleanInputPoll floatIsOutsideRange(final FloatInputPoll base, final float minimum, final float maximum) {
        return new FIOR(base, minimum, maximum);
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
    public static BooleanInput createDispatch(BooleanInputPoll input, EventSource trigger) {
        BooleanStatus bstat = new BooleanStatus();
        Mixing.pumpWhen(trigger, input, bstat);
        return bstat;
    }

    /**
     * Return a FloatInput that is the same as the specified FloatInputPoll,
     * except that it is also a producer that will update whenever the specified
     * event is triggered.
     *
     * @param input the original input.
     * @param trigger the event to dispatch at.
     * @return the dispatchable input.
     */
    public static FloatInput createDispatch(FloatInputPoll input, EventSource trigger) {
        FloatStatus fstat = new FloatStatus();
        Mixing.pumpWhen(trigger, input, fstat);
        return fstat;
    }

    /**
     * Add a ramping system between the specified input and output, with the
     * specified acceleration limit, and returns the EventConsumer to update the
     * ramping system.
     *
     * @param limit The maximum delta per update.
     * @param from The FloatInputPoll to control the expected value.
     * @param target The output to write the current value to.
     * @return The EventConsumer that updates the ramping system.
     */
    public static EventConsumer createRamper(final float limit, final FloatInputPoll from, final FloatOutput target) {
        return new RampingImpl(from, limit, target);
    }

    /**
     * Add a ramping system that will wrap the specified FloatOutput in a
     * ramping controller that will update when the specified event is produced,
     * with the specified limit.
     *
     * @param limit The maximum delta per update.
     * @param updateWhen When to update the ramping system.
     * @param target The target to wrap.
     * @return The wrapped output.
     */
    public static FloatOutput addRamping(final float limit, EventSource updateWhen, final FloatOutput target) {
        FloatStatus temp = new FloatStatus();
        updateWhen.addListener(createRamper(limit, temp, target));
        return temp;
    }

    /**
     * Add a ramping system that will wrap the specified FloatInputPoll in a
     * ramping controller that will update when the specified event is produced,
     * with the specified limit.
     *
     * @param limit The maximum delta per update.
     * @param updateWhen When to update the ramping system.
     * @param source The source to wrap
     * @return The wrapped input.
     */
    public static FloatInputPoll addRamping(final float limit, EventSource updateWhen, final FloatInputPoll source) {
        FloatStatus temp = new FloatStatus();
        updateWhen.addListener(createRamper(limit, source, temp));
        return temp;
    }

    /**
     * Returns a FloatInputPoll representing the delta between the current value
     * of input and the previous value. This _only_ works when you use the
     * result in one place! If you use it in multiple, then it may try to find
     * the deltas between each invocation!
     *
     * To get around this, use findRate with two arguments.
     *
     * @param input The input value to find the rate of.
     * @return The FloatInputPoll representing the rate.
     */
    public static FloatInputPoll findRate(final FloatInputPoll input) {
        return new FindRateImpl(input);
    }

    /**
     * Returns a FloatInputPoll representing the delta between the current value
     * of input and the value in the last cycle, denoted by the specified
     * EventSource.
     *
     * If you only need to use this in one place, then using findRate with one
     * argument might be a better choice.
     *
     * @param input The input value to find the rate of.
     * @param updateWhen When to update the current state, so that the delta is
     * from the last update of this.
     * @return The FloatInputPoll representing the rate.
     */
    public static FloatInputPoll findRate(final FloatInputPoll input, EventSource updateWhen) {
        return new FindRateCycledImpl(input).start(updateWhen);
    }

    /**
     * Returns a four-way select based on two BooleanInputPolls from four
     * floats.
     *
     * @param alpha The first boolean.
     * @param beta The second boolean.
     * @param ff The value to use when both inputs are false.
     * @param ft The value to use when the first is false and the second is
     * true.
     * @param tf The value to use when the first is true and the second is
     * false.
     * @param tt The value to use when both inputs are true.
     * @return The FloatInputPoll representing the current value.
     */
    public static FloatInputPoll quadSelect(final BooleanInputPoll alpha, final BooleanInputPoll beta, final float ff, final float ft, final float tf, final float tt) {
        return new QuadSelectImpl(alpha, beta, tt, tf, ft, ff);
    }

    /**
     * Returns a four-way select based on two BooleanInputPolls from four
     * FloatInputPolls.
     *
     * @param alpha The first boolean.
     * @param beta The second boolean.
     * @param ff The value to use when both inputs are false.
     * @param ft The value to use when the first is false and the second is
     * true.
     * @param tf The value to use when the first is true and the second is
     * false.
     * @param tt The value to use when both inputs are true.
     * @return The FloatInputPoll representing the current value.
     */
    public static FloatInputPoll quadSelect(final BooleanInputPoll alpha, final BooleanInputPoll beta, final FloatInputPoll ff, final FloatInputPoll ft, final FloatInputPoll tf, final FloatInputPoll tt) {
        return new QuadSelectImpl2(alpha, beta, tt, tf, ft, ff);
    }

    /**
     * Returns a debounced version of the specified EventConsumer, such that
     * there is a minimum delay of minMillis milliseconds between events.
     *
     * Any event sent before the timeout will be ignored.
     *
     * @param orig The EventConsumer to debounce.
     * @param minMillis The minimum event delay.
     * @return The debounced version of the event consumer.
     */
    public static EventConsumer debounce(EventConsumer orig, int minMillis) {
        return new DebounceImpl(orig, minMillis);
    }

    /**
     * Returns a debounced version of the specified EventSource, such that there
     * is a minimum delay of minMillis milliseconds between events.
     *
     * Any event sent before the timeout will be ignored.
     *
     * @param orig The EventSource to debounce.
     * @param minMillis The minimum event delay.
     * @return The debounced version of the event source.
     */
    public static EventSource debounce(EventSource orig, int minMillis) {
        Event e = new Event();
        orig.addListener(debounce((EventConsumer) e, minMillis));
        return e;
    }
}

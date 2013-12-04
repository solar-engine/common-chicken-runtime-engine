/*
 * Copyright 2013 Colby Skeggs
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

import ccre.chan.BooleanInput;
import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanInputProducer;
import ccre.chan.BooleanOutput;
import ccre.chan.BooleanStatus;
import ccre.chan.FloatInput;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;
import ccre.chan.FloatStatus;
import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.Utils;

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
        public void writeValue(float newValue) {
        }
    };
    /**
     * A BooleanOutput that goes nowhere. All data sent here is ignored.
     */
    public static final BooleanOutput ignoredBooleanOutput = new BooleanOutput() {
        public void writeValue(boolean newValue) {
        }
    };
    /**
     * A BooleanInput that is always false.
     */
    public static final BooleanInput alwaysFalse = new BooleanInput() {
        public boolean readValue() {
            return false;
        }

        public void addTarget(BooleanOutput consum) {
            consum.writeValue(false);
        }

        public boolean removeTarget(BooleanOutput consum) {
            Logger.warning("Faked removeTarget for Mixing.alwaysFalse");
            return true; // Faked!
        }
    };
    /**
     * A BooleanInput that is always true.
     */
    public static final BooleanInput alwaysTrue = new BooleanInput() {
        public boolean readValue() {
            return true;
        }

        public void addTarget(BooleanOutput consum) {
            consum.writeValue(true);
        }

        public boolean removeTarget(BooleanOutput consum) {
            Logger.warning("Faked removeTarget for Mixing.alwaysTrue");
            return true; // Faked!
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class Always implements FloatInput {

        private final float value;

        Always(float value) {
            this.value = value;
        }

        public float readValue() {
            return value;
        }

        public void addTarget(FloatOutput consum) {
            consum.writeValue(value);
        }

        public boolean removeTarget(FloatOutput consum) {
            Logger.warning("Faked removeTarget for Mixing.always(" + value + ")");
            return true; // Faked!
        }
    }

    /**
     * Combine two FloatOutputs so that any write to the returned output will go
     * to both of the specified outputs.
     *
     * @param a the first output
     * @param b the second output
     * @return the output that will write to both specified outputs.
     */
    public static FloatOutput combineFloats(final FloatOutput a, final FloatOutput b) {
        return new FloatOutput() {
            public void writeValue(float value) {
                a.writeValue(value);
                b.writeValue(value);
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
    public static FloatOutput combineFloats(final FloatOutput a, final FloatOutput b, final FloatOutput c) {
        return new FloatOutput() {
            public void writeValue(float value) {
                a.writeValue(value);
                b.writeValue(value);
                c.writeValue(value);
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
    public static BooleanOutput combineBooleans(final BooleanOutput a, final BooleanOutput b) {
        return new BooleanOutput() {
            public void writeValue(boolean value) {
                a.writeValue(value);
                b.writeValue(value);
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
    public static BooleanOutput combineBooleans(final BooleanOutput a, final BooleanOutput b, final BooleanOutput c) {
        return new BooleanOutput() {
            public void writeValue(boolean value) {
                a.writeValue(value);
                b.writeValue(value);
                c.writeValue(value);
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
    public static BooleanOutput booleanSelectFloat(final FloatOutput controlled, final float off, final float on) {
        return new BSF(controlled, off, on);
    }

    /**
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static final class BSF implements BooleanOutput {

        private final float off;
        private final float on;
        private final FloatOutput bout;

        private BSF(FloatOutput bout, float off, float on) {
            this.bout = bout;
            this.off = off;
            this.on = on;
        }

        public void writeValue(boolean value) {
            bout.writeValue(value ? on : off);
        }
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
    public static FloatInput booleanSelectFloat(BooleanInput selector, float off, float on) {
        return new BCF(selector, selector.readValue(), off, on);
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
    public static FloatInput booleanSelectFloat(BooleanInputProducer selector, boolean default_, float off, float on) {
        return new BCF(selector, default_, off, on);
    }

    /**
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static final class BCF implements FloatInput, BooleanOutput {

        private final BooleanInputProducer binp;
        private final float off, on;
        private float cur;
        private CArrayList<FloatOutput> consumers = null;

        BCF(BooleanInputProducer binp, boolean default_, float off, float on) {
            this.binp = binp;
            this.off = off;
            this.on = on;
            binp.addTarget(this);
            cur = default_ ? on : off;
        }

        public float readValue() {
            return cur;
        }

        public void addTarget(FloatOutput consum) {
            if (consumers == null) {
                consumers = new CArrayList<FloatOutput>();
            }
            consumers.add(consum);
            consum.writeValue(cur);
        }

        public boolean removeTarget(FloatOutput consum) {
            if (consumers != null) {
                consumers.remove(consum);
                return true;
            }
            return false;
        }

        public void writeValue(boolean value) {
            cur = value ? on : off;
            if (consumers != null) {
                for (FloatOutput out : consumers) {
                    out.writeValue(cur);
                }
            }
        }
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
    public static FloatInputPoll booleanSelectFloat(BooleanInputPoll selector, float off, float on) {
        return new BSF2(selector, off, on);
    }

    /**
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class BSF2 implements FloatInputPoll {

        private final BooleanInputPoll binp;
        private final float off;
        private final float on;

        private BSF2(BooleanInputPoll binp, float off, float on) {
            this.binp = binp;
            this.off = off;
            this.on = on;
        }

        public float readValue() {
            return binp.readValue() ? on : off;
        }
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
    public static BooleanOutput booleanSelectFloat(final FloatOutput target, final FloatInputPoll off, final FloatInputPoll on) {
        return new BooleanOutput() {
            public void writeValue(boolean value) {
                target.writeValue(value ? on.readValue() : off.readValue());
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
    public static FloatInput booleanSelectFloat(BooleanInput selector, FloatInputPoll off, FloatInputPoll on) {
        return new BCF2(selector, selector.readValue(), off, on);
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
    public static FloatInput booleanSelectFloat(BooleanInputProducer selector, boolean default_, FloatInputPoll off, FloatInputPoll on) {
        return new BCF2(selector, default_, off, on);
    }

    /**
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static final class BCF2 implements FloatInput, BooleanOutput {

        private final BooleanInputProducer binp;
        private final FloatInputPoll off, on;
        private float cur;
        private CArrayList<FloatOutput> consumers = null;

        BCF2(BooleanInputProducer binp, boolean default_, FloatInputPoll off, FloatInputPoll on) {
            this.binp = binp;
            this.off = off;
            this.on = on;
            binp.addTarget(this);
            cur = default_ ? on.readValue() : off.readValue();
        }

        public float readValue() {
            return cur;
        }

        public void addTarget(FloatOutput consum) {
            if (consumers == null) {
                consumers = new CArrayList<FloatOutput>();
            }
            consumers.add(consum);
            consum.writeValue(cur);
        }

        public boolean removeTarget(FloatOutput consum) {
            if (consumers != null) {
                consumers.remove(consum);
                return true;
            }
            return false;
        }

        public void writeValue(boolean value) {
            cur = value ? on.readValue() : off.readValue();
            if (consumers != null) {
                for (FloatOutput out : consumers) {
                    out.writeValue(cur);
                }
            }
        }
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
    public static FloatInputPoll booleanSelectFloat(final BooleanInputPoll selector, final FloatInputPoll off, final FloatInputPoll on) {
        return new FloatInputPoll() {
            public float readValue() {
                return selector.readValue() ? on.readValue() : off.readValue();
            }
        };
    }

    /**
     * Returns a FloatInputPoll with a deadzone applied as defined in
     * Utils.deadzone
     *
     * @param value the input representing the current value.
     * @param deadzone the deadzone to apply.
     * @return the input representing the deadzone applied to the specified
     * value.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatInputPoll deadzone(FloatInputPoll value, float deadzone) {
        return new DZI(value, deadzone);
    }

    /**
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class DZI implements FloatInputPoll {

        private final FloatInputPoll value;
        private final float deadzone;

        DZI(FloatInputPoll value, float deadzone) {
            this.value = value;
            this.deadzone = deadzone;
        }

        public float readValue() {
            return Utils.deadzone(value.readValue(), deadzone);
        }
    }

    /**
     * Returns a FloatInput with a deadzone applied as specified in
     * Utils.deadzone.
     *
     * @param value the input representing the current value.
     * @param deadzone the deadzone to apply.
     * @return the input representing the deadzone applied to the specified
     * value.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatInput deadzone(FloatInput value, float deadzone) {
        FloatStatus out = new FloatStatus();
        value.addTarget(deadzone((FloatOutput) out, deadzone));
        out.writeValue(value.readValue());
        return out;
    }

    /**
     * Returns a FloatInputProducer with a deadzone applied as specified in
     * Utils.deadzone
     *
     * @param value the input representing the current value.
     * @param deadzone the deadzone to apply.
     * @return the input representing the deadzone applied to the specified
     * value.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatInputProducer deadzone(FloatInputProducer value, float deadzone) {
        FloatStatus out = new FloatStatus();
        value.addTarget(deadzone((FloatOutput) out, deadzone));
        return out;
    }

    /**
     * Returns a FloatOutput that writes through a deadzoned version of any
     * values written to it. Deadzones values as specified in Utils.deadzone.
     *
     * @param output the output to write deadzoned values to.
     * @param deadzone the deadzone to apply.
     * @return the output that writes deadzoned values through to the specified
     * output.
     * @see ccre.util.Utils#deadzone(float, float)
     */
    public static FloatOutput deadzone(final FloatOutput output, final float deadzone) {
        return new DZO(output, deadzone);
    }

    /**
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class DZO implements FloatOutput {

        private final FloatOutput value;
        private final float deadzone;

        DZO(FloatOutput value, float deadzone) {
            this.value = value;
            this.deadzone = deadzone;
        }

        public void writeValue(float newValue) {
            value.writeValue(Utils.deadzone(newValue, deadzone));
        }
    }

    /**
     * Returns a FloatInputPoll representing the negated version of the
     * specified input.
     *
     * @param value the input to negate.
     * @return the negated input.
     */
    public static FloatInputPoll negate(final FloatInputPoll value) {
        return new FloatInputPoll() {
            public float readValue() {
                return -value.readValue();
            }
        };
    }

    /**
     * Returns a FloatInput representing the negated version of the specified
     * input.
     *
     * @param value the input to negate.
     * @return the negated input.
     */
    public static FloatInput negate(FloatInput value) {
        FloatStatus out = new FloatStatus();
        value.addTarget(negate((FloatOutput) out));
        out.writeValue(value.readValue());
        return out;
    }

    /**
     * Returns a FloatInputProducer representing the negated version of the
     * specified input.
     *
     * @param value the input to negate.
     * @return the negated input.
     */
    public static FloatInputProducer negate(FloatInputProducer value) {
        FloatStatus out = new FloatStatus();
        value.addTarget(negate((FloatOutput) out));
        return out;
    }

    /**
     * Returns a FloatOutput that, when written to, writes the negation of the
     * value through to the specified output.
     *
     * @param output the output to write negated values to.
     * @return the output to write pre-negated values to.
     */
    public static FloatOutput negate(final FloatOutput output) {
        return new FloatOutput() {
            public void writeValue(float newValue) {
                output.writeValue(-newValue);
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
        return new BooleanInputPoll() {
            public boolean readValue() {
                return !value.readValue();
            }
        };
    }

    /**
     * Returns a BooleanInput that represents the logical inversion of the value
     * of the specified input.
     *
     * @param value the value to invert.
     * @return the inverted value.
     */
    public static BooleanInput invert(BooleanInput value) {
        BooleanStatus out = new BooleanStatus();
        value.addTarget(invert((BooleanOutput) out));
        out.writeValue(value.readValue());
        return out;
    }

    /**
     * Returns a BooleanInputProducer that represents the logical inversion of
     * the value of the specified input.
     *
     * @param value the value to invert.
     * @return the inverted value.
     */
    public static BooleanInputProducer invert(BooleanInputProducer value) {
        BooleanStatus out = new BooleanStatus();
        value.addTarget(invert((BooleanOutput) out));
        return out;
    }

    /**
     * Returns a BooleanOutput that, when written to, writes the logical
     * inversion of the value through to the specified output.
     *
     * @param output the output to write inverted values to.
     * @return the output to write pre-inverted values to.
     */
    public static BooleanOutput invert(final BooleanOutput output) {
        return new BooleanOutput() {
            public void writeValue(boolean newValue) {
                output.writeValue(!newValue);
            }
        };
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class GSEF implements EventConsumer {

        private final FloatOutput out;
        private final float value;

        GSEF(FloatOutput out, float value) {
            this.out = out;
            this.value = value;
        }

        public void eventFired() {
            out.writeValue(value);
        }
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class GSEB implements EventConsumer {

        private final BooleanOutput out;
        private final boolean value;

        GSEB(BooleanOutput out, boolean value) {
            this.out = out;
            this.value = value;
        }

        public void eventFired() {
            out.writeValue(value);
        }
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
            protected boolean last;

            public void writeValue(boolean value) {
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
    public static EventSource whenBooleanBecomes(BooleanInputProducer input, boolean target) {
        final Event out = new Event();
        input.addTarget(new WBBI(target, out));
        return out;
    }

    /**
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class WBBI implements BooleanOutput {

        private final boolean target;
        private final Event out;

        WBBI(boolean target, Event out) {
            this.target = target;
            this.out = out;
        }
        protected boolean last;

        public void writeValue(boolean value) {
            if (value == last) {
                return;
            }
            last = value;
            if (value == target) {
                out.produce();
            }
        }
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class FEC implements EventConsumer {

        private final BooleanInputPoll shouldAllow;
        private final boolean requirement;
        private final EventConsumer cnsm;

        FEC(BooleanInputPoll shouldAllow, boolean requirement, EventConsumer cnsm) {
            this.shouldAllow = shouldAllow;
            this.requirement = requirement;
            this.cnsm = cnsm;
        }

        public void eventFired() {
            if (shouldAllow.readValue() == requirement) {
                cnsm.eventFired();
            }
        }
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class FES implements EventConsumer {

        private final BooleanInputPoll shouldAllow;
        private final boolean requirement;
        private final Event out;

        FES(BooleanInputPoll shouldAllow, boolean requirement, Event out) {
            this.shouldAllow = shouldAllow;
            this.requirement = requirement;
            this.out = out;
        }

        public void eventFired() {
            if (shouldAllow.readValue() == requirement) {
                out.produce();
            }
        }
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
        return new BooleanInputPoll() {
            public boolean readValue() {
                return a.readValue() && b.readValue();
            }
        };
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
        return new BooleanInputPoll() {
            public boolean readValue() {
                return a.readValue() || b.readValue();
            }
        };
    }

    /**
     * Return a BooleanInputPoll that is true when all specified inputs are
     * true.
     *
     * @param vals the inputs to check.
     * @return the input representing if all given inputs are true.
     */
    public static BooleanInputPoll andBooleans(final BooleanInputPoll... vals) {
        return new BooleanInputPoll() {
            public boolean readValue() {
                for (BooleanInputPoll val : vals) {
                    if (!val.readValue()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Return a BooleanInputPoll that is true when any specified input is true.
     *
     * @param vals the inputs to check.
     * @return the input representing if any given input is true.
     */
    public static BooleanInputPoll orBooleans(final BooleanInputPoll... vals) {
        return new BooleanInputPoll() {
            public boolean readValue() {
                for (BooleanInputPoll val : vals) {
                    if (val.readValue()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Return a BooleanInputPoll that is true when the specified float inputs
     * are equal.
     *
     * @param a the first input.
     * @param b the second input.
     * @return an input that represents the two floats being equal.
     */
    public static BooleanInputPoll floatsEqual(final FloatInputPoll a, final FloatInputPoll b) {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return a.readValue() == b.readValue();
            }
        };
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
        return new EventConsumer() {
            public void eventFired() {
                out.writeValue(in.readValue());
            }
        };
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
        return new EventConsumer() {
            public void eventFired() {
                out.writeValue(in.readValue());
            }
        };
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
    public static EventConsumer combineEvents(final EventConsumer... events) {
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
    public static EventConsumer combineEvents(final EventConsumer a, final EventConsumer b) {
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class NFI implements FloatInputPoll {

        private final FloatInputPoll base;
        private final float zero;
        private final float range;

        NFI(FloatInputPoll base, float zero, float range) {
            this.base = base;
            this.zero = zero;
            this.range = range;
        }

        public float readValue() {
            return (base.readValue() - zero) / range;
        }
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class FIAL implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float minimum;

        FIAL(FloatInputPoll base, float minimum) {
            this.base = base;
            this.minimum = minimum;
        }

        public boolean readValue() {
            return base.readValue() >= minimum;
        }
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class FIAM implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float maximum;

        FIAM(FloatInputPoll base, float maximum) {
            this.base = base;
            this.maximum = maximum;
        }

        public boolean readValue() {
            return base.readValue() <= maximum;
        }
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class FIIR implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float minimum;
        private final float maximum;

        FIIR(FloatInputPoll base, float minimum, float maximum) {
            this.base = base;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public boolean readValue() {
            float val = base.readValue();
            return val >= minimum && val <= maximum;
        }
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
     * Implementation detail. This is not an anonymous class in order to play
     * nicer with Squawk.
     */
    private static class FIOR implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float minimum;
        private final float maximum;

        FIOR(FloatInputPoll base, float minimum, float maximum) {
            this.base = base;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public boolean readValue() {
            float val = base.readValue();
            return val < minimum || val > maximum;
        }
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
        return new EventConsumer() {
            public float last = from.readValue();

            public void eventFired() {
                last = Utils.updateRamping(last, from.readValue(), limit);
                target.writeValue(last);
            }
        };
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
}

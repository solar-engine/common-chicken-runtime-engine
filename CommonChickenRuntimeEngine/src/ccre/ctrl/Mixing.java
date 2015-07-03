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
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.concurrency.ConcurrentDispatchArray;

/**
 * Mixing is a class that provides a wide variety of useful static methods to
 * accomplish various common actions using channels, that don't fall into the
 * existing categories of EventMixing, FloatMixing, or BooleanMixing.
 *
 * Common actions involving teleoperating the robot can be found in DriverImpls.
 *
 * @see DriverImpls
 * @see BooleanMixing
 * @see FloatMixing
 * @see EventMixing
 * @author skeggsc
 */
public class Mixing {

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
        checkNull(controlled);
        return new BooleanOutput() {
            public void set(boolean value) {
                controlled.set(value ? on : off);
            }
        };
    }

    static void checkNull(Object... objs) {
        for (Object obj : objs) {
            if (obj == null) {
                throw new NullPointerException();
            }
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
    public static FloatInput select(final BooleanInput selector, final float off, final float on) {
        checkNull(selector);
        return select(selector, selector.get(), off, on);
    }

    /**
     * Provides a FloatInput that contains a value selected from the two float
     * arguments based on the state of the specified BooleanInput.
     *
     * @param selector the value to select the float value based on.
     * @param default_ the value to assume for the BooleanInput before any
     * changes are detected.
     * @param off the value to use when false
     * @param on the value to use when true
     * @return the FloatInput calculated from the selector's value and the two
     * floats.
     */
    public static FloatInput select(final BooleanInput selector, final boolean default_, final float off, final float on) {
        checkNull(selector);
        final FloatStatus stat = new FloatStatus(default_ ? on : off);
        selector.send(select(stat, off, on));
        return stat;
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
    public static FloatInputPoll select(final BooleanInputPoll selector, final float off, final float on) {
        checkNull(selector);
        return new FloatInputPoll() {
            public float get() {
                return selector.get() ? on : off;
            }
        };
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
        checkNull(target, off, on);
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
        checkNull(selector, off, on);
        return select(selector, selector.get(), off, on);
    }

    /**
     * Returns a FloatInput with a value selected from two FloatInputPolls based
     * on the BooleanInputProducer's value.
     *
     * Warning: changes to the FloatInputPoll parameters will not modify the
     * output until the BooleanInput changes! However, this is likely to be
     * fixed in the future.
     *
     * @param selector the selector to choose an input using.
     * @param default_ the value to default the selector to before it changes.
     * @param off if the selector is false.
     * @param on if the selector is true.
     * @return the value selected based on the selector's value and the statuses
     * of the two arguments.
     */
    public static FloatInput select(final BooleanInput selector, final boolean default_, final FloatInputPoll off, final FloatInputPoll on) {
        checkNull(selector, off, on);
        return new FloatInput() {
            private FloatInputPoll cur = default_ ? on : off;
            private final ConcurrentDispatchArray<FloatOutput> consumers = new ConcurrentDispatchArray<FloatOutput>();

            {
                selector.send(new BooleanOutput() {
                    public void set(boolean value) {
                        cur = value ? on : off;
                        float val = cur.get();
                        for (FloatOutput out : consumers) {
                            out.set(val);
                        }
                    }
                });
            }

            public float get() {
                return cur.get();
            }

            public void send(FloatOutput consum) {
                consumers.addIfNotFound(consum);
                consum.set(get());
            }

            public void unsend(FloatOutput consum) {
                consumers.remove(consum);
            }
        };
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
        checkNull(selector, off, on);
        return new FloatInputPoll() {
            public float get() {
                return selector.get() ? on.get() : off.get();
            }
        };
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
        checkNull(alpha, beta);
        return new FloatInputPoll() {
            public float get() {
                return alpha.get() ? (beta.get() ? tt : tf) : (beta.get() ? ft : ff);
            }
        };
    }

    /**
     * Returns a four-way select based on two BooleanInputs from four floats.
     *
     * @param alpha The first boolean.
     * @param beta The second boolean.
     * @param ff The value to use when both inputs are false.
     * @param ft The value to use when the first is false and the second is
     * true.
     * @param tf The value to use when the first is true and the second is
     * false.
     * @param tt The value to use when both inputs are true.
     * @return The FloatInput representing the current value.
     */
    public static FloatInput quadSelect(final BooleanInput alpha, final BooleanInput beta, final float ff, final float ft, final float tf, final float tt) {
        checkNull(alpha, beta);
        return FloatMixing.createDispatch(quadSelect((BooleanInputPoll) alpha, (BooleanInputPoll) beta, ff, ft, tf, tt),
                EventMixing.combine(BooleanMixing.whenBooleanChanges(alpha), BooleanMixing.whenBooleanChanges(alpha)));
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
        checkNull(alpha, beta, ff, ft, tf, tt);
        return new FloatInputPoll() {
            public float get() {
                return (alpha.get() ? (beta.get() ? tt : tf) : (beta.get() ? ft : ff)).get();
            }
        };
    }

    /**
     * Returns a four-way select based on two BooleanInputs from four
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
     * @return The FloatInput representing the current value.
     */
    public static FloatInput quadSelect(final BooleanInput alpha, final BooleanInput beta, final FloatInputPoll ff, final FloatInputPoll ft, final FloatInputPoll tf, final FloatInputPoll tt) {
        checkNull(alpha, beta, ff, ft, tf, tt);
        return FloatMixing.createDispatch(quadSelect((BooleanInputPoll) alpha, (BooleanInputPoll) beta, ff, ft, tf, tt),
                EventMixing.combine(BooleanMixing.whenBooleanChanges(alpha), BooleanMixing.whenBooleanChanges(beta)));
    }

    private Mixing() {
    }
}

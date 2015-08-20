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
import ccre.channel.DerivedFloatInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.util.Utils;

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
        Utils.checkNull(controlled);
        return new BooleanOutput() {
            public void set(boolean value) {
                controlled.set(value ? on : off);
            }
        };
    }

    /**
     * The returned BooleanOutput is a way to modify the specified target. When
     * the BooleanOutput is changed, the target is set to the current value of
     * the associated parameter (the on parameter if true, the off parameter if
     * false).
     *
     * @param target the FloatOutput to write to.
     * @param off the value to write if the written boolean is false.
     * @param on the value to write if the written boolean is true.
     * @return the BooleanOutput that will modify the specified target.
     */
    public static BooleanOutput select(final FloatOutput target, final FloatInput off, final FloatInput on) {
        Utils.checkNull(target, off, on);
        return new BooleanOutput() {
            private boolean lastValue = false, anyValue = false;
            
            {
                off.onUpdate(() -> {
                    if (anyValue && !lastValue) {
                        set(lastValue); // resend as necessary
                    }
                });
                on.onUpdate(() -> {
                    if (anyValue && lastValue) {
                        set(lastValue); // resend as necessary
                    }
                });
            }
            
            public void set(boolean value) {
                lastValue = value;
                anyValue = true;
                target.set(value ? on.get() : off.get());
            }
        };
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
        return new DerivedFloatInput(selector) {
            @Override
            protected float apply() {
                return selector.get() ? on : off;
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
    public static FloatInput select(BooleanInput selector, FloatInput off, FloatInput on) {
        return new DerivedFloatInput(selector, off, on) {
            @Override
            protected float apply() {
                return selector.get() ? on.get() : off.get();
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
        return new DerivedFloatInput(alpha, beta) {
            @Override
            protected float apply() {
                return alpha.get() ? beta.get() ? tt : tf : beta.get() ? ft : ff;
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
    public static FloatInput quadSelect(final BooleanInput alpha, final BooleanInput beta, final FloatInput ff, final FloatInput ft, final FloatInput tf, final FloatInput tt) {
        return new DerivedFloatInput(alpha, beta, tt, tf, ft, ff) {
            @Override
            protected float apply() {
                return alpha.get() ? beta.get() ? tt.get() : tf.get() : beta.get() ? ft.get() : ff.get();
            }
        };
    }

    private Mixing() {
    }
}

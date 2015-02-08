/*
 * Copyright 2014-2015 Colby Skeggs.
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

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;

/**
 * A generic PID Controller for use in CCRE applications. Supports online tuning
 * of various parameters.
 * 
 * This will attempt to move the input close to the setpoint by way of varying
 * the output according to the P, I, and D terms.
 * 
 * It also supports limiting of the output range and the current integral sum.
 * 
 * This is an EventOutput - when this is fired, it will update the current
 * value. This is also a FloatInput, representing the current output from the
 * PID controller.
 *
 * @author skeggsc
 */
public class PIDControl implements FloatInput, EventOutput {

    private final FloatInputPoll input, setpoint;
    private final FloatInputPoll P, I, D;
    private FloatInputPoll maxOutput = null, minOutput = null;
    private FloatInputPoll maxIntegral = null, minIntegral = null;
    private float previousInput = 0.0f;
    private long previousTime = 0;
    /**
     * The running total from the integral term.
     */
    public final FloatStatus integralTotal = new FloatStatus();
    private final FloatStatus output = new FloatStatus();
    /**
     * If two execution of the PIDControl differ by more than this much, the
     * controller will pretend it only differed by this much.
     */
    private FloatInputPoll maximumTimeDelta = FloatMixing.always(0.1f); // 100ms.

    /**
     * Create a simple fixed PID controller. It's very much possible to have
     * more control - just instantiate the class directly.
     * 
     * @param trigger when to update.
     * @param input the input to track.
     * @param setpoint the setpoint to attempt to move the input to.
     * @param p the proportional constant.
     * @param i the integral constant.
     * @param d the derivative constant.
     * @return the PID controller, which is also an input representing the
     * current value.
     */
    public static PIDControl createFixedPID(EventInput trigger, FloatInputPoll input, FloatInputPoll setpoint, float p, float i, float d) {
        PIDControl ctrl = new PIDControl(input, setpoint,
                FloatMixing.always(p), FloatMixing.always(i), FloatMixing.always(d));
        trigger.send(ctrl);
        return ctrl;
    }

    /**
     * Create a new PIDControl with the specified sources for its tuning, and no
     * setpoint.
     * 
     * This is equivalent to using the error parameter as the input parameter
     * and a fixed setpoint of zero.
     * 
     * @param error the error source for the PID controller.
     * @param P a source for the proportional term.
     * @param I a source for the integral term.
     * @param D a source for the derivative term.
     */
    public PIDControl(FloatInputPoll error, FloatInputPoll P, FloatInputPoll I, FloatInputPoll D) {
        this.input = error;
        this.setpoint = FloatMixing.always(0);
        this.P = P;
        this.I = I;
        this.D = D;
    }

    /**
     * Create a new PIDControl with the specified sources for its tuning.
     * 
     * @param input the input source for the PID controller.
     * @param setpoint the setpoint source for the PID controller.
     * @param P a source for the proportional term.
     * @param I a source for the integral term.
     * @param D a source for the derivative term.
     */
    public PIDControl(FloatInputPoll input, FloatInputPoll setpoint, FloatInputPoll P, FloatInputPoll I, FloatInputPoll D) {
        this.input = input;
        this.setpoint = setpoint;
        this.P = P;
        this.I = I;
        this.D = D;
    }

    /**
     * Restrict the PID output to the specified magnitude.
     * 
     * @param maximumAbsolute the maximum absolute value.
     */
    public void setOutputBounds(float maximumAbsolute) {
        setOutputBounds(-maximumAbsolute, maximumAbsolute);
    }

    /**
     * Restrict the PID output to the specified magnitude.
     * 
     * @param maximumAbsolute the maximum absolute value.
     */
    public void setOutputBounds(FloatInputPoll maximumAbsolute) {
        setOutputBounds(FloatMixing.negate(maximumAbsolute), maximumAbsolute);
    }

    /**
     * Restrict the PID output to the specified lower and upper bounds.
     * 
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setOutputBounds(float minimum, float maximum) {
        setOutputBounds(FloatMixing.always(minimum), FloatMixing.always(maximum));
    }

    /**
     * Restrict the PID output to the specified lower and upper bounds.
     * 
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setOutputBounds(FloatInputPoll minimum, FloatInputPoll maximum) {
        this.maxOutput = maximum;
        this.minOutput = minimum;
    }

    /**
     * Restrict the current integral sum to the specified magnitude.
     * 
     * @param maximumAbsolute the maximum absolute value.
     */
    public void setIntegralBounds(float maximumAbsolute) {
        setIntegralBounds(-maximumAbsolute, maximumAbsolute);
    }

    /**
     * Restrict the current integral sum to the specified magnitude.
     * 
     * @param maximumAbsolute the maximum absolute value.
     */
    public void setIntegralBounds(FloatInputPoll maximumAbsolute) {
        setIntegralBounds(FloatMixing.negate(maximumAbsolute), maximumAbsolute);
    }

    /**
     * Restrict the current integral sum to the specified lower and upper
     * bounds.
     * 
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setIntegralBounds(float minimum, float maximum) {
        setIntegralBounds(FloatMixing.always(minimum), FloatMixing.always(maximum));
    }

    /**
     * Restrict the current integral sum to the specified lower and upper
     * bounds.
     * 
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setIntegralBounds(FloatInputPoll minimum, FloatInputPoll maximum) {
        this.maxIntegral = maximum;
        this.minIntegral = minimum;
    }

    /**
     * Set the maximum time delta: if two execution of the PIDControl differ by
     * more than the maximum time delta, the controller will pretend it only
     * differed by the maximum time delta.
     * 
     * @param delta the new maximum time delta.
     */
    public void setMaximumTimeDelta(float delta) {
        setMaximumTimeDelta(FloatMixing.always(delta));
    }

    /**
     * Set the maximum time delta: if two execution of the PIDControl differ by
     * more than the maximum time delta, the controller will pretend it only
     * differed by the maximum time delta.
     * 
     * @param delta the new maximum time delta.
     */
    public void setMaximumTimeDelta(FloatInputPoll delta) {
        this.maximumTimeDelta = delta;
    }

    /**
     * Update the PID controller on the specified event's occurrence.
     * 
     * @param when the event to trigger the controller with.
     */
    public void updateWhen(EventInput when) {
        when.send(this);
    }

    /**
     * Update the PID controller.
     */
    public void event() {
        float error = input.get() - setpoint.get();
        if (Float.isNaN(error) || Float.isInfinite(error)) {
            output.set(Float.NaN);
        } else {
            long time = System.currentTimeMillis();
            long timeDelta = time - previousTime;
            if (timeDelta < 0) {
                throw new RuntimeException("Time just ran backwards!");
            } else if (timeDelta == 0) { // Updating too fast. Ignore it.
                return;
            } else if (timeDelta / 1000f > maximumTimeDelta.get()) {
                timeDelta = (long) (maximumTimeDelta.get() * 1000);
            }
            float newTotal = integralTotal.get() + error * timeDelta / 1000f;
            if (minIntegral != null && newTotal < minIntegral.get()) {
                newTotal = minIntegral.get();
            }
            if (maxIntegral != null && newTotal > maxIntegral.get()) {
                newTotal = maxIntegral.get();
            }
            integralTotal.set(newTotal);
            float slope = 1000 /* milliseconds per second */* (error - previousInput) / timeDelta;
            float valueOut = error * P.get() + integralTotal.get() * I.get() + slope * D.get();
            previousInput = error;
            previousTime = time;
            if (minOutput != null && valueOut < minOutput.get()) {
                valueOut = minOutput.get();
            }
            if (maxOutput != null && valueOut > maxOutput.get()) {
                valueOut = maxOutput.get();
            }
            output.set(valueOut);
        }
    }

    public void send(FloatOutput to) {
        output.send(to);
    }

    public void unsend(FloatOutput to) {
        output.unsend(to);
    }

    public float get() {
        return output.get();
    }
}

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
 * Note: PIDControl is a variant of PIDController with the output sign switched.
 * It exists only for backwards compatibility and will be removed soon.
 *
 * @author skeggsc
 */
public class PIDController implements FloatInput, EventOutput {

    private final FloatInput input, setpoint;
    private final FloatInput P, I, D;
    private FloatInput maxOutput = null, minOutput = null;
    private FloatInput maxIntegral = null, minIntegral = null;
    private float previousError = 0.0f;
    private long previousTime = 0;
    /**
     * The running total from the integral term.
     */
    public final FloatStatus integralTotal = new FloatStatus();
    private final FloatStatus output = new FloatStatus();
    /**
     * If two executions of the PIDController differ by more than this much, the
     * controller will pretend it only differed by this much.
     */
    private FloatInput maximumTimeDelta = FloatInput.always(0.1f); // 100ms.

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
    public static PIDController createFixed(EventInput trigger, FloatInput input, FloatInput setpoint, float p, float i, float d) {
        PIDController ctrl = new PIDController(input, setpoint,
                FloatInput.always(p), FloatInput.always(i), FloatInput.always(d));
        ctrl.updateWhen(trigger);
        return ctrl;
    }

    /**
     * Create a new PIDController with the specified sources for its tuning, and
     * no setpoint.
     *
     * This is equivalent to using the error parameter as the input parameter
     * and a fixed setpoint of zero.
     *
     * @param error the error source for the PID controller.
     * @param P a source for the proportional term.
     * @param I a source for the integral term.
     * @param D a source for the derivative term.
     */
    public PIDController(FloatInput error, FloatInput P, FloatInput I, FloatInput D) {
        this.input = FloatInput.always(0);
        this.setpoint = error;
        this.P = P;
        this.I = I;
        this.D = D;
    }

    /**
     * Create a new PIDController with the specified sources for its tuning.
     *
     * @param input the input source for the PID controller.
     * @param setpoint the setpoint source for the PID controller.
     * @param P a source for the proportional term.
     * @param I a source for the integral term.
     * @param D a source for the derivative term.
     */
    public PIDController(FloatInput input, FloatInput setpoint, FloatInput P, FloatInput I, FloatInput D) {
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
    public void setOutputBounds(FloatInput maximumAbsolute) {
        setOutputBounds(maximumAbsolute.negated(), maximumAbsolute);
    }

    /**
     * Restrict the PID output to the specified lower and upper bounds.
     *
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setOutputBounds(float minimum, float maximum) {
        setOutputBounds(FloatInput.always(minimum), FloatInput.always(maximum));
    }

    /**
     * Restrict the PID output to the specified lower and upper bounds.
     *
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setOutputBounds(FloatInput minimum, FloatInput maximum) {
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
    public void setIntegralBounds(FloatInput maximumAbsolute) {
        setIntegralBounds(maximumAbsolute.negated(), maximumAbsolute);
    }

    /**
     * Restrict the current integral sum to the specified lower and upper
     * bounds.
     *
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setIntegralBounds(float minimum, float maximum) {
        setIntegralBounds(FloatInput.always(minimum), FloatInput.always(maximum));
    }

    /**
     * Restrict the current integral sum to the specified lower and upper
     * bounds.
     *
     * @param minimum the minimum value.
     * @param maximum the maximum value.
     */
    public void setIntegralBounds(FloatInput minimum, FloatInput maximum) {
        this.maxIntegral = maximum;
        this.minIntegral = minimum;
    }

    /**
     * Set the maximum time delta: if two execution of the PIDController differ
     * by more than the maximum time delta, the controller will pretend it only
     * differed by the maximum time delta.
     *
     * @param delta the new maximum time delta, in seconds.
     */
    public void setMaximumTimeDelta(float delta) {
        setMaximumTimeDelta(FloatInput.always(delta));
    }

    /**
     * Set the maximum time delta: if two execution of the PIDController differ
     * by more than the maximum time delta, the controller will pretend it only
     * differed by the maximum time delta.
     *
     * @param delta the new maximum time delta, in seconds.
     */
    public void setMaximumTimeDelta(FloatInput delta) {
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
        long time = System.currentTimeMillis();
        long timeDelta = time - previousTime;
        previousTime = time;
        update(timeDelta);
    }

    /**
     * Update the PID controller, giving it a custom time delta in milliseconds
     * instead of letting it measure the delta itself.
     *
     * @param timeDelta the time delta
     * @throws IllegalArgumentException if timeDelta is negative
     */
    public void update(long timeDelta) throws IllegalArgumentException {
        float error = setpoint.get() - input.get();
        if (Float.isNaN(error) || Float.isInfinite(error)) {
            output.set(Float.NaN);
        } else {
            if (timeDelta < 0) {
                throw new IllegalArgumentException("Time just ran backwards!");
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
            float slope = 1000 /* milliseconds per second */* (error - previousError) / timeDelta;
            float valueOut = error * P.get() + integralTotal.get() * I.get() + slope * D.get();
            previousError = error;
            if (minOutput != null && valueOut < minOutput.get()) {
                valueOut = minOutput.get();
            }
            if (maxOutput != null && valueOut > maxOutput.get()) {
                valueOut = maxOutput.get();
            }
            output.set(valueOut);
        }
    }

    public float get() {
        return output.get();
    }

    @Override
    public void onUpdate(EventOutput notify) {
        output.onUpdate(notify);
    }

    @Override
    public EventOutput onUpdateR(EventOutput notify) {
        return output.onUpdateR(notify);
    }

    /**
     * Get the error that was received on the last call, which is used to
     * calculate the D component.
     *
     * @return the previous error
     */
    public float getPreviousError() {
        return previousError;
    }
}

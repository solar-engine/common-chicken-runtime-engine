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
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.time.Time;

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
public class PIDController implements FloatInput, EventOutput {

    private final FloatInput input, setpoint;
    private final FloatInput P, I, D;
    private FloatInput maxAbsOutput = null;
    private FloatInput maxAbsIntegral = null;
    private float previousError = 0.0f;
    private long previousTime = 0;
    /**
     * The running total from the integral term.
     */
    public final FloatCell integralTotal = new FloatCell();
    private final FloatCell output = new FloatCell();
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
        PIDController ctrl = new PIDController(input, setpoint, FloatInput.always(p), FloatInput.always(i), FloatInput.always(d));
        ctrl.updateWhen(trigger);
        return ctrl;
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
        if (input == null || setpoint == null || P == null || I == null || D == null) {
            throw new NullPointerException();
        }
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
        setOutputBounds(FloatInput.always(maximumAbsolute));
    }

    /**
     * Restrict the PID output to the specified magnitude.
     *
     * @param maximumAbsolute the maximum absolute value.
     */
    public void setOutputBounds(FloatInput maximumAbsolute) {
        maxAbsOutput = maximumAbsolute;
    }

    /**
     * Restrict the current integral sum to the specified magnitude.
     *
     * @param maximumAbsolute the maximum absolute value.
     */
    public void setIntegralBounds(float maximumAbsolute) {
        setIntegralBounds(FloatInput.always(maximumAbsolute));
    }

    /**
     * Restrict the current integral sum to the specified magnitude.
     *
     * @param maximumAbsolute the maximum absolute value.
     */
    public void setIntegralBounds(FloatInput maximumAbsolute) {
        maxAbsIntegral = maximumAbsolute;
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
        long time = Time.currentTimeMillis();
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
            if (maxAbsIntegral != null && Math.abs(newTotal) > maxAbsIntegral.get()) {
                newTotal = newTotal < 0 ? -maxAbsIntegral.get() : maxAbsIntegral.get();
            }
            try {
                integralTotal.set(newTotal);
            } finally {
                float slope = Time.MILLISECONDS_PER_SECOND * (error - previousError) / timeDelta;
                float valueOut = error * P.get() + integralTotal.get() * I.get() + slope * D.get();
                previousError = error;
                if (maxAbsOutput != null && Math.abs(valueOut) > maxAbsOutput.get()) {
                    valueOut = valueOut < 0 ? -maxAbsOutput.get() : maxAbsOutput.get();
                }
                output.set(valueOut);
            }
        }
    }

    public float get() {
        return output.get();
    }

    @Override
    public EventOutput onUpdate(EventOutput notify) {
        return output.onUpdate(notify);
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

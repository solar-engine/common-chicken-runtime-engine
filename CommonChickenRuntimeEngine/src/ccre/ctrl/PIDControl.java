/*
 * Copyright 2015 Colby Skeggs.
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
import ccre.channel.FloatInputPoll;

/**
 * A backwards-compatibility version of PIDController, with the sign flipped on
 * the error calculation. I recommend that you update to PIDController and fix
 * your signs.
 *
 * The reason for the change is that the old (PIDControl) signs are
 * counter-intuitive: if the setPoint is greater than the input, then the
 * movement makes more sense to be positive, not negative.
 *
 * @author skeggsc
 */
@Deprecated
public class PIDControl extends PIDController {

    /**
     * Create a simple fixed PID controller. It's very much possible to have
     * more control - just instantiate the class directly.
     *
     * WARNING: the PIDControl class is deprecated. Switch to PIDController!
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
     * WARNING: the PIDControl class is deprecated. Switch to PIDController!
     *
     * @param error the error source for the PID controller.
     * @param P a source for the proportional term.
     * @param I a source for the integral term.
     * @param D a source for the derivative term.
     */
    public PIDControl(FloatInputPoll error, FloatInputPoll P, FloatInputPoll I, FloatInputPoll D) {
        super(error, P, I, D);
    }

    /**
     * Create a new PIDController with the specified sources for its tuning.
     *
     * WARNING: the PIDControl class is deprecated. Switch to PIDController!
     *
     * @param input the input source for the PID controller.
     * @param setpoint the setpoint source for the PID controller.
     * @param P a source for the proportional term.
     * @param I a source for the integral term.
     * @param D a source for the derivative term.
     */
    public PIDControl(FloatInputPoll input, FloatInputPoll setpoint, FloatInputPoll P, FloatInputPoll I, FloatInputPoll D) {
        super(setpoint, input, P, I, D); // purposeful switching
    }
}

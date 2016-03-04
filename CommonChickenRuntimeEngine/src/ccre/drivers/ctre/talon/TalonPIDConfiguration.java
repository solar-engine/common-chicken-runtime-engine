/*
 * Copyright 2016 Cel Skeggs
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
package ccre.drivers.ctre.talon;

import ccre.channel.BooleanIO;
import ccre.channel.FloatIO;

/**
 * The PID configuration of a Talon. Many of these properties are synchronous
 * rather than asynchronous, and might not appear to update instantly when they
 * are modified.
 *
 * Note that this PID controlling includes a feedforward term.
 *
 * @author skeggsc
 */
public interface TalonPIDConfiguration {
    /**
     * Provides the proportional coefficient of the PID controller.
     *
     * @return a FloatIO representing the coefficient.
     */
    public FloatIO getP();

    /**
     * Provides the integral coefficient of the PID controller.
     *
     * @return a FloatIO representing the coefficient.
     */
    public FloatIO getI();

    /**
     * Provides the derivative coefficient of the PID controller.
     *
     * @return a FloatIO representing the coefficient.
     */
    public FloatIO getD();

    /**
     * Provides the feedforward coefficient of the PID controller.
     *
     * @return a FloatIO representing the coefficient.
     */
    public FloatIO getF();

    /**
     * Provides the integral accumulator limit of the PID controller.
     *
     * @return a FloatIO representing the limit.
     */
    public FloatIO getIntegralBounds();

    /**
     * Provides whether or not the secondary profile is active, which allows for
     * switching between two PID setups.
     *
     * @return a BooleanIO representing whether the secondary profile is active
     * instead of the primary.
     */
    public BooleanIO getIsSecondaryProfileActive();

    /**
     * Provides the ramp rate of the closed loop PID controller.
     *
     * @return a FloatIO representing the ramp rate.
     */
    public FloatIO getCloseLoopRampRate();

    /**
     * Provides the current integral accumulator of the closed loop PID
     * controller.
     *
     * @return a FloatIO representing the integral accumulator.
     */
    public FloatIO getIAccum();

    /**
     * Configures the ramping rates for the throttle and the voltage
     * compensation. See the Talon SRX manual for more info.
     *
     * These are independent of the closed-loop ramping rate.
     *
     * @param throttle the throttle ramping rate.
     * @param voltageCompensation the voltage compensation ramping rate.
     */
    public void configureRampRates(float throttle, float voltageCompensation);
}

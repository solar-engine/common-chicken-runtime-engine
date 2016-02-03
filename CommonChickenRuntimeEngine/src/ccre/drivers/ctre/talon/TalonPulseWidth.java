/*
 * Copyright 2016 Colby Skeggs
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

import ccre.channel.BooleanInput;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;

/**
 * The Pulse Width input on a Talon.
 *
 * @author skeggsc
 */
public interface TalonPulseWidth {

    /**
     * Configures the rates at which the pulse width frame will be updated. Read
     * the Talon SRX documentation for details.
     *
     * @param millis the millisecond period of the "pulse width" frame.
     */
    public void configurePulseWidthUpdateRate(int millis);

    /**
     * Sets the Talon to read from a pulse width input for its sensor position.
     */
    public void usePulseWidth();

    /**
     * Sets the Talon to read from a CTRE magnetic encoder in relative mode for
     * its sensor position.
     */
    public void useRelativeCtreMagEncoder();

    /**
     * Sets the Talon to read from a CTRE magnetic encoder in absolute mode for
     * its sensor position.
     */
    public void useAbsoluteCtreMagEncoder();

    /**
     * Provides the pulse width position, regardless of the active sensor.
     *
     * @return a FloatIO representing the pulse width position.
     */
    public FloatIO getPulseWidthPosition();

    /**
     * Provides the pulse width velocity, regardless of the active sensor.
     *
     * @return a FloatInput representing the pulse width position.
     */
    public FloatInput getPulseWidthVelocity();

    /**
     * Provides the rise-to-fall microsecond count for the pulse width sensor.
     *
     * @return a FloatInput representing the rise-to-fall microsecond count.
     */
    public FloatInput getPulseWidthRiseToFallMicroseconds();

    /**
     * Provides the rise-to-rise microsecond count for the pulse width sensor.
     *
     * @return a FloatInput representing the rise-to-rise microsecond count.
     */
    public FloatInput getPulseWidthRiseToRiseMicroseconds();

    /**
     * Provides the connection status of the pulse width sensor/CTRE magnetic
     * encoder.
     *
     * @return a BooleanInput representing the sensor presence.
     */
    public BooleanInput getPulseWidthOrCtreMagEncoderPresent();
}

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

import ccre.channel.FloatIO;
import ccre.channel.FloatInput;

/**
 * The analog input of a Talon.
 *
 * @author skeggsc
 */
public interface TalonAnalog {

    /**
     * Configures the rates at which the analog frame will be updated. Read the
     * Talon SRX documentation for details.
     *
     * @param millis the millisecond period of the "analog" frame.
     */
    public void configureAnalogUpdateRate(int millis);

    /**
     * Sets the Talon to read from the analog input used as an encoder for its
     * sensor position.
     */
    public void useAnalogEncoder();

    /**
     * Sets the Talon to read from the analog input used as a potentiometer for
     * its sensor position.
     */
    public void useAnalogPotentiometer();

    /**
     * Provides the analog sensor position, regardless of the active sensor, in potentiometer mode.
     *
     * @return a FloatIO representing the analog sensor position.
     */
    public FloatIO getAnalogPosition(); // 0.0 to 1.0

    /**
     * Provides the analog sensor position, regardless of the active sensor, in encoder mode.
     *
     * @return a FloatIO representing the analog sensor position.
     */
    public FloatIO getAnalogPositionEncoder(); // one rotation is 0.0 to 1.0

    /**
     * Provides the analog sensor velocity, regardless of the active sensor.
     *
     * @return a FloatInput representing the analog sensor velocity.
     */
    public FloatInput getAnalogVelocity();

    /**
     * Configures the number of rotations that the potentiometer goes through
     * per full sweep of the analog range.
     *
     * @param rotations the number of rotations.
     */
    public void configurePotentiometerTurns(float rotations);
}

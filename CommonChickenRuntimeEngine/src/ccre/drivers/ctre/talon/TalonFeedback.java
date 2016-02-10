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
 * The feedback info of a Talon.
 *
 * @author skeggsc
 */
public interface TalonFeedback {
    /**
     * Provides the bus voltage of the Talon, in volts.
     *
     * The bus voltage is the input voltage of the Talon, generally the same as
     * the battery voltage.
     *
     * @return a FloatInput representing the bus voltage of the Talon.
     */
    public FloatInput getBusVoltage();

    /**
     * Provides the output voltage of the Talon, in volts.
     *
     * The output voltage is the actual voltage across the terminals of the
     * driven motor.
     *
     * @return a FloatInput representing the output voltage of the Talon.
     */
    public FloatInput getOutputVoltage();

    /**
     * Provides the output current of the Talon, in amps.
     *
     * The output current is the actual current passing through the Talon.
     *
     * @return a FloatInput representing the current going through the Talon.
     */
    public FloatInput getOutputCurrent();

    /**
     * Provides the current position of the Talon's active sensor, in the
     * engineering units for the specific sensor.
     *
     * @return a FloatIO representing the sensor position of the currently
     * active sensor.
     */
    public FloatIO getSensorPosition();

    /**
     * Provides the current velocity of the Talon's active sensor, in the
     * engineering units for the specific sensor.
     *
     * @return a FloatInput representing the sensor velocity of the currently
     * active sensor.
     */
    public FloatInput getSensorVelocity();

    /**
     * Provides the current throttle fraction of the Talon, from -1.0 to 1.0.
     * This factor scales the active output voltage of the Talon.
     *
     * @return a FloatInput representing the Talon throttle.
     */
    public FloatInput getThrottle();

    /**
     * Provides the closed loop error reported by the Talon, in units based on
     * the current sensor.
     *
     * @return a FloatInput representing the closed loop error.
     */
    public FloatInput getClosedLoopError();

    /**
     * Provides the current temperature of the Talon, in degrees Celsius.
     *
     * @return a FloatInput representing the temperature of the Talon.
     */
    public FloatInput getTemperature();

    /**
     * Queries the firmware version of the Talon.
     *
     * @return the firmware version of the Talon, if it has been received.
     */
    public long GetFirmwareVersion();
}

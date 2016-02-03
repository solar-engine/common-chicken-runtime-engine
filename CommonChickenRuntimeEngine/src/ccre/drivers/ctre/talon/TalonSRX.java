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

import ccre.channel.BooleanIO;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.Faultable;

/**
 * A connection to a CTRE Talon SRX over CAN. It is organized into a list of
 * "modules" that represent different components of the Talon SRX's interface.
 * It also has some non-componentized methods.
 *
 * It is recommended that you read the official Talon SRX documentation for
 * details on what each concept means.
 *
 * @see #modAnalog()
 * @see #modEncoder()
 * @see #modFaults()
 * @see #modHardLimits()
 * @see #modMotor()
 * @see #modPID()
 * @see #modPulseWidth()
 * @see #modSoftLimits()
 * @author skeggsc
 */
public interface TalonSRX {

    /**
     * All of the possible faults that a Talon SRX can experience.
     *
     * @author skeggsc
     * @see TalonSRX#modFaults()
     */
    public enum Faults {
        /**
         * A fault caused by the Talon being over temperature.
         */
        OVER_TEMPERATURE,
        /**
         * A fault caused by the bus voltage being too low.
         */
        UNDER_VOLTAGE,
        /**
         * A fault caused by an unknown hardware failure.
         */
        HARDWARE_FAILURE,
        /**
         * A "fault" representing the forward hard limit being pressed.
         */
        FORWARD_HARD_LIMIT,
        /**
         * A "fault" representing the reverse hard limit being pressed.
         */
        REVERSE_HARD_LIMIT,
        /**
         * A "fault" representing the forward soft limit being pressed.
         */
        FORWARD_SOFT_LIMIT,
        /**
         * A "fault" representing the reverse soft limit being pressed.
         */
        REVERSE_SOFT_LIMIT
    }

    /**
     * Accesses a representation of the current fault conditions of the Talon.
     *
     * @return the Faults module.
     */
    public Faultable<Faults> modFaults();

    /**
     * Accesses a representation of the analog input of the Talon.
     *
     * @return the Analog module.
     */
    public TalonAnalog modAnalog();

    /**
     * Accesses a representation of the encoder input of the Talon.
     *
     * @return the Encoder module.
     */
    public TalonEncoder modEncoder();

    /**
     * Accesses a representation of the hard limits of the Talon.
     *
     * @return the Hard Limit module.
     */
    public TalonHardLimits modHardLimits();

    /**
     * Accesses a representation of the PID configuration of the Talon.
     *
     * @return the PID module.
     */
    public TalonPIDConfiguration modPID();

    /**
     * Accesses a representation of the Pulse Width input of the Talon.
     *
     * @return the Pulse Width module.
     */
    public TalonPulseWidth modPulseWidth();

    /**
     * Accesses a representation of the soft limits of the Talon.
     *
     * @return the Soft Limit module.
     */
    public TalonSoftLimits modSoftLimits();

    /**
     * Accesses the ExtendedMotor representation of the Talon.
     *
     * @return the ExtendedMotor for this Talon.
     */
    public ExtendedMotor modMotor();

    // other stuff

    /**
     * Provides the enablement state of this ExtendedMotor.
     *
     * @return a BooleanIO representing the enable state of the Talon.
     */
    public BooleanIO asEnable();

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

    // modes

    /**
     * Configures the Talon to follow the specified other Talon rather than
     * follow a specific control mode of its own.
     *
     * The throttle of this Talon will replicate the other Talon's throttle as
     * closely as possible.
     *
     * @param talonID the ID of the Talon to follow.
     */
    public void activateFollowerMode(int talonID);

    /**
     * Configures the Talon to follow the specified other Talon rather than
     * follow a specific control mode of its own.
     *
     * The throttle of this Talon will replicate the other Talon's throttle as
     * closely as possible.
     *
     * @param talon the Talon to follow.
     */
    public default void activateFollowerMode(TalonSRX talon) {
        activateFollowerMode(talon.getDeviceID());
    }

    // public void activateMotionProfileMode(); TODO

    // configuration

    /**
     * Configures whether or not the sensor reading and/or the output reading
     * are negated.
     *
     * @param flipSensor whether or not the sensor's direction should be
     * reversed.
     * @param flipOutput whether or not the output's direction should be
     * reversed.
     */
    public void configureReversed(boolean flipSensor, boolean flipOutput);

    /**
     * Configures the allowable closed loop error on the Talon.
     *
     * @param allowableCloseLoopError the maximum allowable error.
     */
    public void configureAllowableClosedLoopError(float allowableCloseLoopError);

    /**
     * Configures the rates at which the two common frames will be updated. Read
     * the Talon SRX documentation for details.
     *
     * @param millisGeneral the millisecond period of the "general" frame.
     * @param millisFeedback the millisecond period of the "feedback" frame.
     */
    public void configureGeneralFeedbackUpdateRate(int millisGeneral, int millisFeedback);

    /**
     * Configures the maximum output voltage of the Talon.
     * <code>forwardVoltage</code> should be in the range 0 to 12, and
     * <code>reverseVoltage</code> should be in the range 0 to -12.
     *
     * @param forwardVoltage the maximum forward voltage.
     * @param reverseVoltage the maximum reverse voltage.
     */
    public void configureMaximumOutputVoltage(float forwardVoltage, float reverseVoltage);

    /**
     * Configures the nominal output voltage of the Talon.
     * <code>forwardVoltage</code> should be in the range 0 to 12, and
     * <code>reverseVoltage</code> should be in the range 0 to -12.
     *
     * @param forwardVoltage the nominal forward voltage.
     * @param reverseVoltage the nominal reverse voltage.
     */
    public void configureNominalOutputVoltage(float forwardVoltage, float reverseVoltage);

    // other

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

    /**
     * Fetches the device ID of the Talon. This was the ID used to create the
     * Talon SRX reference.
     *
     * @return the device ID, in the range 0 - 62.
     */
    public int getDeviceID();

    /**
     * Provides control over whether the Talon SRX is set to Brake or Coast. It
     * will be true when the Talon is set to Brake and false when the Talon is
     * set to Coast.
     *
     * Once this is changed, it will override anything configured on the roboRIO
     * inspection website.
     *
     * @return a BooleanIO representing whether or not the Talon is set to Brake
     * mode.
     */
    public BooleanIO getBrakeNotCoast();
}

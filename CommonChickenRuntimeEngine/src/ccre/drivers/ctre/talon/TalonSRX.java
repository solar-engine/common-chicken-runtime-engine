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
    public TalonExtendedMotor modMotor();

    /**
     * Accesses a representation of the feedback data of the Talon.
     *
     * @return the Feedback module.
     */
    public TalonFeedback modFeedback();

    /**
     * Accesses a representation of the general configuration of the Talon.
     *
     * @return the General Configuration module.
     */
    public TalonGeneralConfig modGeneralConfig();

    /**
     * Fetches the device ID of the Talon. This was the ID used to create the
     * Talon SRX reference.
     *
     * @return the device ID, in the range 0 - 62.
     */
    public int getDeviceID();
}

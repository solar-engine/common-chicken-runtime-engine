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

/**
 * The generic configuration of a Talon.
 *
 * @author skeggsc
 */
public interface TalonGeneralConfig {
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

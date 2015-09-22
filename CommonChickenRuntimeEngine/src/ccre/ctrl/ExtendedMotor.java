/*
 * Copyright 2014-2015 Colby Skeggs
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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;

/**
 * Sometimes there's more to control about a motor than just a power level, and
 * so FloatOutput is insufficient. That's where ExtendedMotor comes in. An
 * example of an ExtendedMotor would be a CAN Jaguar or CAN Talon.
 *
 * An extended motor may have one or more Diagnostics (error reports, such as
 * faults), Statuses (state reports, such as voltage), and Control Modes
 * (methods of controlling an output, such as voltage-based or current-based.)
 *
 * It may also be disabled and enabled.
 *
 * It may also have an internal PID state.
 *
 * @author skeggsc
 */
public abstract class ExtendedMotor {
    /**
     * The different possible diagnostics for an ExtendedMotor to provide.
     *
     * Some of these are bitsets of fault conditions, and some are
     * boolean-reportable faults (on or off.)
     *
     * @author skeggsc
     */
    public static enum DiagnosticType {
        // Mask-based: (long)
        /**
         * A bitset of CAN Jaguar faults, reported raw.
         *
         * The CAN Jaguar supports CURRENT_FAULT, TEMPERATURE_FAULT,
         * BUS_VOLTAGE_FAULT, and GATE_DRIVER_FAULT.
         */
        CAN_JAGUAR_FAULTS(false),
        /**
         * A bitset of faults, reported raw. What this means depends on the
         * specific device.
         */
        GENERIC_FAULT_MASK(false),

        // Boolean-based:
        /**
         * If any fault is occurring.
         */
        ANY_FAULT(true),
        /**
         * A boolean fault based on current.
         */
        CURRENT_FAULT(true),
        /**
         * A boolean fault based on temperature.
         */
        TEMPERATURE_FAULT(true),
        /**
         * A boolean fault based on bus voltage.
         */
        BUS_VOLTAGE_FAULT(true),
        /**
         * A boolean fault based on the gate driver.
         */
        GATE_DRIVER_FAULT(true),
        /**
         * A boolean fault based on a communications failure
         */
        COMMS_FAULT(true);

        /**
         * Specifies if this type of fault is reported as a boolean.
         */
        public final boolean isBooleanDiagnostic;

        private DiagnosticType(boolean isBoolean) {
            this.isBooleanDiagnostic = isBoolean;
        }
    }

    /**
     * The different possible output control modes for an ExtendedMotor to
     * provide.
     *
     * @author skeggsc
     */
    public static enum OutputControlMode {
        /**
         * A generic fractional output. This takes a range of -1.0 (full
         * reverse) to 1.0 (full forward). This will always map to something,
         * but it may not always mean anything useful for a device.
         */
        GENERIC_FRACTIONAL,
        /**
         * A voltage-based fractional output. This takes a range of -1.0 (full
         * reverse) to 1.0 (full forward). This is calculated based on voltage.
         */
        VOLTAGE_FRACTIONAL,
        /**
         * A voltage-based fixed output. This takes a voltage to attempt to
         * produce.
         */
        VOLTAGE_FIXED,
        /**
         * A current-based fixed output. This takes a current to attempt to
         * produce.
         */
        CURRENT_FIXED
    }

    /**
     * The different possible status types for an ExtendedMotor to provide.
     *
     * @author skeggsc
     */
    public static enum StatusType {
        /**
         * Provides the present bus voltage.
         */
        BUS_VOLTAGE,
        /**
         * Provides the present output voltage.
         */
        OUTPUT_VOLTAGE,
        /**
         * Provides the present output current.
         */
        OUTPUT_CURRENT,
        /**
         * Provides the present controller temperature, in degrees Celsius.
         */
        TEMPERATURE
    }

    /**
     * Enables the output, if possible. A control mode should be specified
     * first.
     *
     * @throws ExtendedMotorFailureException if the output cannot be enabled.
     */
    public abstract void enable() throws ExtendedMotorFailureException;

    /**
     * Disables the output, if possible.
     *
     * @throws ExtendedMotorFailureException if the output cannot be disabled.
     */
    public abstract void disable() throws ExtendedMotorFailureException;

    /**
     * Constructs a BooleanOutput to control the enablement status. A control
     * mode should be specified before enabling.
     *
     * @return a BooleanOutput controlling whether this controller is enabled.
     */
    public abstract BooleanOutput asEnable();

    /**
     * Opens the controller in the specified output mode. This may override any
     * previous output modes, so don't use any previous results from this method
     * once it has been called.
     *
     * @param mode the mode to put the controller into.
     * @return the output representing the controller. The meaning is based on
     * the mode, or null if this mode is not allowed for this device.
     * @throws ExtendedMotorFailureException if an error occurs while opening
     * the output with this mode.
     */
    public abstract FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException;

    /**
     * Gets access to one of the status readouts from the ExtendedMotor.
     *
     * @param type the type of status to get access to.
     * @return the FloatInput representing this status readout, or null if it
     * cannot be acquired.
     */
    public FloatInput asStatus(StatusType type) {
        return asStatus(type, FRC.sensorPeriodic);
    }

    /**
     * Gets access to one of the status readouts from the ExtendedMotor.
     *
     * @param type the type of status to get access to.
     * @param updateOn when to update the sensor.
     * @return the FloatInput representing this status readout, or null if it
     * cannot be acquired.
     */
    public abstract FloatInput asStatus(StatusType type, EventInput updateOn);

    /**
     * Gets the current diagnostic value from the ExtendedMotor. This is usually
     * either an Integer or a Boolean based on the DiagnosticType.
     *
     * @param type the type of diagnostic to read.
     * @return the current diagnostic value.
     */
    public abstract Object getDiagnostics(DiagnosticType type);

    /**
     * Gets a channel representing a boolean diagnostic channel.
     *
     * @param type the type of diagnostic to monitor.
     * @return a channel representing the diagnostic state, or null if it cannot
     * be acquired.
     */
    public BooleanInput getDiagnosticChannel(final DiagnosticType type) {
        return getDiagnosticChannel(type, FRC.sensorPeriodic);
    }

    /**
     * Gets a channel representing a boolean diagnostic channel.
     *
     * @param type the type of diagnostic to monitor.
     * @param updateOn when to update the sensor.
     * @return a channel representing the diagnostic state, or null if it cannot
     * be acquired.
     */
    public BooleanInput getDiagnosticChannel(final DiagnosticType type, EventInput updateOn) {
        if (!type.isBooleanDiagnostic || !(getDiagnostics(type) instanceof Boolean)) {
            return null;
        }
        return new DerivedBooleanInput(updateOn) { // TODO: fix this
            @Override
            protected boolean apply() {
                Object out = getDiagnostics(type);
                if (out instanceof Boolean) {
                    return (Boolean) out;
                } else {
                    throw new RuntimeException("Diagnostic type changed to not be a boolean anymore!");
                }
            }
        };
    }

    /**
     * Checks if internal PID is included in this ExtendedMotor. This does not
     * necessarily mean that it is in use - the output mode must support PID.
     *
     * This may return either true or false if the current output mode does not
     * support PID but the ExtendedMotor does in other cases.
     *
     * @return if internal PID is included.
     */
    public abstract boolean hasInternalPID();

    /**
     * Sets the internal PID tuning in this ExtendedMotor. Don't call this
     * unless hasInternalPID() returns true.
     *
     * @param P the proportional factor in the tuning.
     * @param I the integral factor in the tuning.
     * @param D the derivative factor in the tuning.
     * @throws ExtendedMotorFailureException if the PID cannot be set.
     */
    public abstract void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException;
}

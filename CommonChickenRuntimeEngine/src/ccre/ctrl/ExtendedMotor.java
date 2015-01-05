package ccre.ctrl;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;

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
        GATE_DRIVER_FAULT(true);

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
         * A current-based fixed output, with PID settings required. This takes
         * a current to attempt to produce. This will require tuning PID values.
         */
        CURRENT_FIXED_PID
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
         * Provides the present controller temperature.
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
     * @throws ExtendedMotorFailureException if the output cannot be acquired.
     */
    public abstract BooleanOutput asEnable() throws ExtendedMotorFailureException;

    /**
     * Opens the controller in the specified output mode. This may override any
     * previous output modes, so don't use any previous results from this method
     * once it has been called.
     * 
     * @param mode the mode to put the controller into.
     * @return the output representing the controller. The meaning is based on
     * the mode.
     * @throws ExtendedMotorFailureException if the output cannot be opened with
     * this mode.
     */
    public abstract FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException;

    /**
     * Gets access to one of the status readouts from the ExtendedMotor.
     * 
     * @param type the type of status to get access to.
     * @return the FloatInputPoll representing this status readout.
     * @throws ExtendedMotorFailureException if the status readout cannot be
     * acquired.
     */
    public abstract FloatInputPoll asStatus(StatusType type) throws ExtendedMotorFailureException;

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
    public BooleanInputPoll getDiagnosticChannel(final DiagnosticType type) {
        if (!type.isBooleanDiagnostic || !(getDiagnostics(type) instanceof Boolean)) {
            return null;
        }
        return new BooleanInputPoll() {
            public boolean get() {
                Object out = getDiagnostics(type);
                if (out instanceof Boolean) {
                    return (Boolean) out;
                } else {
                    throw new ExtendedMotorFailureException("Diagnostic type changed to not be a boolean anymore!");
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

package ccre.ctrl;

import ccre.channel.BooleanOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;

public interface ExtendedMotor {
    public static enum DiagnosticType {
        // Mask-based: (long)
        CAN_JAGUAR_FAULTS,
        GENERIC_FAULT_MASK,
        
        // Boolean-based:
        CURRENT_FAULT,
        TEMPERATURE_FAULT,
        BUS_VOLTAGE_FAULT,
        GATE_DRIVER_FAULT
    }
    
    public static enum OutputControlMode {
        GENERIC_FRACTIONAL,
        VOLTAGE_FRACTIONAL,
        VOLTAGE_FIXED,
        CURRENT_FIXED_PID
    }
    
    public static enum StatusType {
        BUS_VOLTAGE,
        OUTPUT_VOLTAGE,
        OUTPUT_CURRENT,
        TEMPERATURE
    }

    public abstract void enable() throws ExtendedMotorFailureException;

    public abstract void disable() throws ExtendedMotorFailureException;

    public abstract BooleanOutput asEnable() throws ExtendedMotorFailureException;

    public abstract FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException;

    public abstract FloatInputPoll asStatus(StatusType type) throws ExtendedMotorFailureException;

    public abstract Object getDiagnostics(DiagnosticType type);

    public abstract boolean hasInternalPID();

    public abstract void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException;
}

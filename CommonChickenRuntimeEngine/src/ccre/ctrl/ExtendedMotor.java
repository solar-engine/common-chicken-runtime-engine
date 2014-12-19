package ccre.ctrl;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;

public abstract class ExtendedMotor {
    public static enum DiagnosticType {
        // Mask-based: (long)
        CAN_JAGUAR_FAULTS(false),
        GENERIC_FAULT_MASK(false),
        
        // Boolean-based:
        CURRENT_FAULT(true),
        TEMPERATURE_FAULT(true),
        BUS_VOLTAGE_FAULT(true),
        GATE_DRIVER_FAULT(true);
        
        public final boolean isBooleanDiagnostic;

        private DiagnosticType(boolean isBoolean) {
            this.isBooleanDiagnostic = isBoolean;
        }
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

    public abstract boolean hasInternalPID();

    public abstract void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException;
}

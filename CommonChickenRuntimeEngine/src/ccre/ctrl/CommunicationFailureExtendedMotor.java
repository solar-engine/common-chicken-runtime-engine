package ccre.ctrl;

import ccre.channel.BooleanOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.log.Logger;

/**
 * A fake implementation of ExtendedMotor that is suitable for use when
 * communications could not be established initially with the device.
 * 
 * This will provide lots of stubbed-out functionality and will log warning
 * messages whenever it is interacted with.
 * 
 * @author skeggsc
 */
public class CommunicationFailureExtendedMotor extends ExtendedMotor implements FloatOutput {

    private final String message;

    /**
     * Create a new CommunicationFailureExtendedMotor with a message to include
     * whenever the issue is reported.
     * 
     * @param message the message to include.
     */
    public CommunicationFailureExtendedMotor(String message) {
        this.message = message;
    }

    @Override
    public void enable() throws ExtendedMotorFailureException {
        throw new ExtendedMotorFailureException("Communications failed: " + message);
    }

    @Override
    public void disable() throws ExtendedMotorFailureException {
        throw new ExtendedMotorFailureException("Communications failed: " + message);
    }

    @Override
    public BooleanOutput asEnable() {
        return new BooleanOutput() {
            public void set(boolean value) {
                Logger.warning("Motor control (enable/disable) failed: " + message);
            }
        };
    }

    @Override
    public FloatOutput asMode(OutputControlMode mode) {
        Logger.severe("Could not access mode of Extended Motor: " + message);
        return this;
    }

    @Override
    public FloatInputPoll asStatus(StatusType type) {
        Logger.severe("Could not access status of Extended Motor: " + message);
        return new FloatInputPoll() {
            public float get() {
                return 0f;
            }
        };
    }

    @Override
    public Object getDiagnostics(DiagnosticType type) {
        if (type == DiagnosticType.COMMS_FAULT || type == DiagnosticType.ANY_FAULT) {
            return true;
        } else if (type.isBooleanDiagnostic) {
            return false;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasInternalPID() {
        return false;
    }

    @Override
    public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
        Logger.warning("Tried to set PID on Extended Motor with failed comms: " + message);
    }

    private long nextWarning = 0;

    public void set(float value) {
        long now = System.currentTimeMillis();
        if (now > nextWarning) {
            Logger.warning("Could not modify Extended Motor value - failed comms: " + message);
            nextWarning = now + 3000;
        }
    }
}

package ccre.ctrl;

/**
 * An exception thrown when an ExtendedMotor cannot complete the requested
 * operation.
 * 
 * @author skeggsc
 */
public class ExtendedMotorFailureException extends RuntimeException {

    private static final long serialVersionUID = 8628319463301729456L;
    private final Throwable cause;

    /**
     * Creates a new ExtendedMotorFailureException, with no message or cause.
     */
    public ExtendedMotorFailureException() {
        super();
        this.cause = null;
    }

    /**
     * Creates a new ExtendedMotorFailureException, with a message and no cause.
     * 
     * @param message the message of the exception.
     */
    public ExtendedMotorFailureException(String message) {
        super(message);
        this.cause = null;
    }

    /**
     * Creates a new ExtendedMotorFailureException, with a cause and no message.
     * 
     * @param cause the cause of the exception.
     */
    public ExtendedMotorFailureException(Throwable cause) {
        super();
        this.cause = cause;
        this.initCause(cause);
    }

    /**
     * Creates a new ExtendedMotorFailureException, with both a message and a
     * cause.
     * 
     * @param message the message of the exception.
     * @param cause the cause of the exception.
     */
    public ExtendedMotorFailureException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
        this.initCause(cause);
    }

    /**
     * Gets the cause of the Throwable in a way that works even on the cRIO,
     * which doesn't allow for normally-associated causes.
     * 
     * @return the cause, or null if none exists.
     */
    public Throwable getEMCause() {
        return cause;
    }
}

package ccre.ctrl;

public class ExtendedMotorFailureException extends RuntimeException {

    private static final long serialVersionUID = 8628319463301729456L;

    public ExtendedMotorFailureException() {
        super();
    }

    public ExtendedMotorFailureException(String message) {
        super(message);
    }

    public ExtendedMotorFailureException(Throwable thr) {
        super(thr);
    }

    public ExtendedMotorFailureException(String message, Throwable thr) {
        super(message, thr);
    }
}

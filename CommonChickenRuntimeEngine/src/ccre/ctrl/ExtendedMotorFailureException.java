package ccre.ctrl;

public class ExtendedMotorFailureException extends RuntimeException {

    private static final long serialVersionUID = 8628319463301729456L;
    private final Throwable base;

    public ExtendedMotorFailureException() {
        super();
        this.base = null;
    }

    public ExtendedMotorFailureException(String message) {
        super(message);
        this.base = null;
    }

    public ExtendedMotorFailureException(Throwable thr) {
        super();
        this.base = thr;
    }

    public ExtendedMotorFailureException(String message, Throwable thr) {
        super(message);
        this.base = thr;
    }
}

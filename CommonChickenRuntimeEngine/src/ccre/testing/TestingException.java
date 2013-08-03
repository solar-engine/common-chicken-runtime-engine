package ccre.testing;

/**
 * An exception thrown when a test fails. This is thrown by the various
 * assert... methods, you probably shouldn't throw one yourself.
 *
 * @author skeggsc
 */
public class TestingException extends Exception {

    public TestingException() {
        super();
    }

    public TestingException(String message) {
        super(message);
    }
}

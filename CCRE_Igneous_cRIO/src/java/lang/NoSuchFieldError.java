package java.lang;

/**
 * This a substitute for java.lang.NoSuchFieldError for Squawk. Does nothing
 * useful whatsoever except stuff can compile.
 *
 * @see java.lang.NoSuchFieldError
 * @author skeggsc
 */
public class NoSuchFieldError extends Error { // Should really be extending IncompatibleClassChangeError

    /**
     * Creates a NoSuchFieldError with no message.
     */
    public NoSuchFieldError() {
    }

    /**
     * Creates an NoSuchFieldError with a specified message.
     *
     * @param message The specified message.
     */
    public NoSuchFieldError(String message) {
        super(message);
    }
}

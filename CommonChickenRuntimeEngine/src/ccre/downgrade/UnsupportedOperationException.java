package ccre.downgrade;

/**
 * This is the same as java.lang.UnsupportedOperationException. Don't use
 * this. It is used when Retrotranslator downgrades the code to 1.3, because 1.3
 * doesn't have UnsupportedOperationException.
 *
 * @see java.lang.UnsupportedOperationException
 * @author skeggsc
 */
public class UnsupportedOperationException extends RuntimeException {

    public UnsupportedOperationException() {
    }

    public UnsupportedOperationException(String message) {
        super(message);
    }
}

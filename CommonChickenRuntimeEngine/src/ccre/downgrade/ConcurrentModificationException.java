package ccre.downgrade;

/**
 * This is the same as java.util.ConcurrentModificationException. Don't use
 * this. It is used when Retrotranslator downgrades the code to 1.3, because 1.3
 * doesn't have ConcurrentModificationException.
 *
 * @see java.util.ConcurrentModificationException
 * @author skeggsc
 */
public class ConcurrentModificationException extends RuntimeException {

    public ConcurrentModificationException() {
    }

    public ConcurrentModificationException(String message) {
        super(message);
    }
}

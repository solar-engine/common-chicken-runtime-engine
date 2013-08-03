package ccre.downgrade;

/**
 * This is the same as java.lang.Iterable. Don't use this. It is used when
 * Retrotranslator downgrades the code to 1.3, because 1.3 doesn't have
 * Iterable.
 *
 * @param <T> The type returned by the iterator.
 * @see java.lang.Iterable
 * @author skeggsc
 */
public interface Iterable<T> {

    Iterator<T> iterator();
}

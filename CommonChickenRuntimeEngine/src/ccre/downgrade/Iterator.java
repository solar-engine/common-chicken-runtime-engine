package ccre.downgrade;

/**
 * This is the same as java.util.Iterator. Don't use
 * this. It is used when Retrotranslator downgrades the code to 1.3, because 1.3
 * doesn't have Iterator.
 *
 * @param <E> The type returned by the iterator.
 * @see java.util.Iterator
 * @author skeggsc
 */
public interface Iterator<E> {

    boolean hasNext();

    E next();

    void remove();
}

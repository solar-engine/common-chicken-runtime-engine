package java.lang;

/**
 * A reimplementation of java.lang.Comparable, used for cRIO downgrading. Should
 * work exactly the same as the equivalent.
 * 
 * @author skeggsc
 * @param <T> the type to be comparable to.
 */
public interface Comparable<T> {
    /**
     * Compare this object to the other object. Should return negative numbers
     * for less than, zero for equal, and positive for greater than.
     * 
     * @param o the object to compare with.
     * @return a comparison integer.
     */
    int compareTo(T o);
}

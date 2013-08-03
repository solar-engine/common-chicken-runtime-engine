package ccre.util;

/**
 * A set of useful array-related utilites.
 *
 * @author skeggsc
 */
public class CArrayUtils {

    /**
     * An empty list. Immutable.
     */
    public static CList EMPTY_LIST = new CAbstractList() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public Object get(int index) {
            throw new IndexOutOfBoundsException("Out of bounds: " + index);
        }
    };

    /**
     * Return an empty list of the given element type.
     *
     * @param <T> the element type.
     * @return the new empty list.
     */
    @SuppressWarnings("unchecked")
    public static <T> CList<T> getEmptyList() {
        return EMPTY_LIST;
    }

    /**
     * Cast the given array to a generic array. This should not be done unless
     * you know what you are doing! This method only exists to suppress
     * warnings.
     *
     * @param <T> the element type.
     * @param arr the original array.
     * @return the casted array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] castToGeneric(Object[] arr) {
        return (T[]) arr;
    }

    /**
     * Create a copy of the specified Object[] array, with the specified new
     * length. Added indexes will be filled with null.
     *
     * @param values the old array.
     * @param nlen the length of the new array.
     * @return the new array.
     */
    public static Object[] copyOf(Object[] values, int nlen) {
        Object[] out = new Object[nlen];
        if (nlen < values.length) {
            System.arraycopy(values, 0, out, 0, nlen);
        } else {
            System.arraycopy(values, 0, out, 0, values.length);
        }
        return out;
    }

    /**
     * Create a fixed-size list of from the specified array. Modifications to
     * one will modify the other.
     *
     * @param <T> the element type.
     * @param arr the data to convert to a list.
     * @return the list version of the specified array.
     */
    public static <T> CList<T> asList(final T... arr) {
        if (arr == null) {
            throw new NullPointerException();
        }
        if (arr.length == 0) {
            return getEmptyList();
        }
        return new CAbstractList<T>() {
            private final T[] t = arr;
            private final int len = arr.length;

            @Override
            public int size() {
                return len;
            }

            @Override
            public T get(int index) {
                return t[index];
            }

            @Override
            public T set(int index, T val) {
                T old = t[index];
                t[index] = val;
                return old;
            }

            @Override
            public int indexOf(Object o) {
                T[] locT = this.t;
                if (o == null) {
                    for (int i = 0; i < locT.length; i++) {
                        if (locT[i] == null) {
                            return i;
                        }
                    }
                    return -1;
                } else {
                    for (int i = 0; i < locT.length; i++) {
                        if (o.equals(locT[i])) {
                            return i;
                        }
                    }
                    return -1;
                }
            }

            @Override
            public int lastIndexOf(Object o) {
                T[] locT = this.t;
                if (o == null) {
                    for (int i = locT.length - 1; i >= 0; i--) {
                        if (locT[i] == null) {
                            return i;
                        }
                    }
                    return -1;
                } else {
                    for (int i = locT.length - 1; i >= 0; i--) {
                        if (o.equals(locT[i])) {
                            return i;
                        }
                    }
                    return -1;
                }
            }
        };
    }
}

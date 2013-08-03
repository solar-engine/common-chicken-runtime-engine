package ccre.util;

/**
 * An array list. This will perform in O(1) for lookups and sets, and O(N) for
 * inserts and deletions that are not to the end of the list.
 *
 * @author skeggsc
 * @param <T> the element type.
 */
public class CArrayList<T> extends CAbstractList<T> {

    /**
     * The list of contained values.
     */
    protected T[] values;
    /**
     * The number of contained elements.
     */
    protected int size;

    /**
     * Create a new CArrayList with a default length of 10.
     */
    public CArrayList() {
        values = CArrayUtils.castToGeneric(new Object[10]);
    }

    /**
     * Create a new CArrayList with a specified default length.
     *
     * @param cap the default capacity.
     */
    public CArrayList(int cap) {
        values = CArrayUtils.castToGeneric(new Object[cap]);
    }

    /**
     * Create a new CArrayList from an existing collection. The default length
     * will be the collection's size + 10.
     *
     * @param coll the collection to copy elements from.
     */
    public CArrayList(CCollection<? extends T> coll) {
        values = CArrayUtils.castToGeneric(new Object[coll.size() + 10]);
        addAll(coll);
    }

    public int size() {
        return size;
    }

    @Override
    public void clear() {
        notifyModified();
        size = 0;
    }

    @Override
    public T get(int index) {
        return values[index];
    }

    @Override
    public T set(int index, T element) {
        notifyModified();
        T prev = values[index];
        values[index] = element;
        return prev;
    }

    @Override
    public void add(int index, T element) {
        notifyModified();
        if (size >= values.length) {
            values = CArrayUtils.castToGeneric(CArrayUtils.copyOf(values, values.length + (values.length >> 1) + 1));
        }
        for (int i = size; i > index; i--) {
            values[i] = values[i - 1];
        }
        size++;
        values[index] = element;
    }

    @Override
    public T remove(int index) {
        notifyModified();
        T old = values[index];
        size--;
        for (int i = index; i < size; i++) {
            values[i] = values[i + 1];
        }
        return old;
    }
}

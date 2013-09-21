package ccre.util;

import java.util.Iterator;

/**
 * A list of elements.
 *
 * Unless otherwise specified, this implements the same interface as
 * java.util.List, except that this works across all CCRE platforms, meaning
 * that FRC robot code can use it.
 *
 * @param <E> the element type.
 */
public interface CList<E> extends CCollection<E> {

    /**
     * Returns an iterator that iterates over the elements in order.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @return the iterator
     */
    Iterator<E> iterator();

    /**
     * Adds the element to the end of the sequence.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param e the element to add.
     * @return true
     */
    boolean add(E e);

    /**
     * Removes the first occurance of the specified object.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @return true if the list contained the object.
     */
    boolean remove(Object o);

    /**
     * Append all the elements from the specified collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     */
    boolean addAll(CCollection<? extends E> c);

    /**
     * Inserts all the elements from the specified collection starting at the
     * specified position.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param index where to insert the elements.
     * @param c the collection to add elements from.
     * @return if the list has been modified by this operation.
     */
    boolean addAll(int index, CCollection<? extends E> c);

    /**
     * Get the element at the specified index.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param index the index to get from.
     * @return the current value at that index.
     */
    E get(int index);

    /**
     * Replace the element at the specified index.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param index the index to modify.
     * @param element the new value for the index.
     * @return the element previously at the specified index.
     */
    E set(int index, E element);

    /**
     * Insert the specified element at the specified index.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param index the index to insert at.
     * @param element the element to insert.
     */
    void add(int index, E element);

    /**
     * Remove and return the element from the specified index.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param index the index to remove.
     * @return the value from the index.
     */
    E remove(int index);

    /**
     * Return the index of the first occurance of the specified object, or -1 if
     * it cannot be found.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param o the object to look for.
     * @return the first index.
     */
    int indexOf(Object o);

    /**
     * Return the index of the last occurance of the specified object, or -1 if
     * it cannot be found.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.List, except that this works across all CCRE platforms, meaning
     * that FRC robot code can use it.
     *
     * @param o the object to look for.
     * @return the last index.
     */
    int lastIndexOf(Object o);
}

/*
 * Copyright 2013 Colby Skeggs
 *
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 *
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    /**
     * Convert this list to a string.
     *
     * The result will be square brackets surrounding a comma-delimited list of
     * the toStrings of the elements of the list.
     *
     * Examples:
     *
     * A list containing 1, 2, and 3 will render as: [1, 2, 3]
     *
     * An empty list will render as: []
     *
     * @return The string expression of the list.
     */
    @Override
    String toString();
}

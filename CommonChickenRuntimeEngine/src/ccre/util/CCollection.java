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
 * This is a collection. Unless otherwise specified, this implements the same
 * interface as java.util.Collection, except that this works across all CCRE
 * platforms, meaning that FRC robot code can use it.
 *
 * @author skeggsc
 * @param <E> the element type of the collection.
 */
public interface CCollection<E> extends Iterable<E> {

    /**
     * Get the number of elements in the collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @return the number of elements.
     */
    int size();

    /**
     * Return true if there are no elements in the collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @return if the collection is empty
     */
    boolean isEmpty();

    /**
     * Return true if the given Object is in this collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @param o the Object to test.
     * @return true if the Object is in this collection.
     */
    boolean contains(Object o);

    /**
     * Return an iterator over the elements of this collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @return the iterator.
     */
    Iterator<E> iterator();

    /**
     * Add the specified element to this collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @param e the element to add.
     * @return true if the element could be added to this collection.
     */
    boolean add(E e);

    /**
     * Removes one copy of the specified element from this collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @param o the object to remove.
     * @return true if the object was originally in the collection.
     */
    boolean remove(Object o);

    /**
     * Return true if all the specified elements are in this collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @param c the collection to check all the elements of.
     * @return true if this collection contains all elements of the other
     * collection.
     */
    boolean containsAll(CCollection<?> c);

    /**
     * Add all the elements from the specified collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @param c the collection to add the elements from.
     * @return true if any changes were made in this collection.
     */
    boolean addAll(CCollection<? extends E> c);

    /**
     * Remove all the elements from the specified collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @param c the collection to remove the elements of.
     * @return true if any changes were made in this collection.
     */
    boolean removeAll(CCollection<?> c);

    /**
     * Remove all elements that are not in the specified collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     *
     * @param c the collection to retain the elements of.
     * @return true if any changes were made in this collection.
     */
    boolean retainAll(CCollection<?> c);

    /**
     * Remove all elements from this collection.
     *
     * Unless otherwise specified, this implements the same interface as
     * java.util.Collection, except that this works across all CCRE platforms,
     * meaning that FRC robot code can use it.
     */
    void clear();
}

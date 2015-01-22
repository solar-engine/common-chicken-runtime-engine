/*
 * Copyright 2013-2014 Colby Skeggs
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
package java.util;

/**
 * This a substitute for java.util.Iterator for Squawk. Does nothing useful
 * whatsoever except stuff can work.
 *
 * @param <E> The type returned by the iterator.
 * @see java.util.Iterator
 * @author skeggsc
 */
public interface Iterator<E> {

    /**
     * Checks if there are any more elements to return from the iterator.
     *
     * @return If there are more elements.
     */
    public boolean hasNext();

    /**
     * If there are any more elements to return, returns the next. Otherwise
     * throws NoSuchElementException.
     *
     * @return The next element.
     * @throws NoSuchElementException If there are no more elements.
     */
    public E next() throws NoSuchElementException;

    /**
     * Removes the element previously returned from next(). This is an optional
     * operation.
     *
     * @throws IllegalStateException If no previous element has been returned
     * from next(), or if remove() has already been called.
     * @throws UnsupportedOperationException If this operation is not supported
     * by the iterator.
     */
    public void remove() throws UnsupportedOperationException, IllegalStateException;
}

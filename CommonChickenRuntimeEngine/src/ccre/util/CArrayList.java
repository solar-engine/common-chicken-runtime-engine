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
package ccre.util;

/**
 * An array list. This will perform in O(1) for lookups and sets, and O(N) for
 * inserts and deletions that are not to the end of the list.
 *
 * @author skeggsc
 * @param <T> the element type.
 */
public final class CArrayList<T> extends CAbstractList<T> {

    /**
     * The list of contained values.
     */
    private T[] values;
    /**
     * The number of contained elements.
     */
    private int size;

    /**
     * Create a new CArrayList with a default length of 10.
     */
    public CArrayList() {
        values = CArrayUtils.castToGeneric(new Object[10]);
    }

    /**
     * Create a new CArrayList with a specified default capacity.
     *
     * @param cap the default capacity.
     */
    public CArrayList(int cap) {
        values = CArrayUtils.castToGeneric(new Object[cap]);
    }

    /**
     * Create a new CArrayList from an existing array. The default capacity will
     * be the array's length + 10.
     *
     * @param coll the array to copy elements from.
     */
    public CArrayList(T[] coll) {
        values = CArrayUtils.castToGeneric(CArrayUtils.copyOf(coll, coll.length + 10));
        size = coll.length;
    }

    /**
     * Create a new CArrayList from an existing collection. The default capacity
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

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return values[index];
    }

    @Override
    public T set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        notifyModified();
        T prev = values[index];
        values[index] = element;
        return prev;
    }

    @Override
    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
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
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        notifyModified();
        T old = values[index];
        size--;
        for (int i = index; i < size; i++) {
            values[i] = values[i + 1];
        }
        return old;
    }
}

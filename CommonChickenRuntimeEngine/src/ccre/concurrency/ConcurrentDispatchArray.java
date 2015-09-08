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
package ccre.concurrency;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ccre.channel.EventOutput;

/**
 * A concurrent collection that allows concurrent iteration and removal without
 * concurrency errors. The values returned by an iterator are the values that
 * were in the iterator when the iterator was started.
 *
 * This is implemented by copying the entire array when a modification operation
 * is completed.
 *
 * @author skeggsc
 * @param <E> The type of the collection's elements
 */
public final class ConcurrentDispatchArray<E> implements Collection<E>, Serializable {

    private static final long serialVersionUID = -7949492774411494179L;
    /**
     * The array that contains the current data. Do not modify this field
     * directly - use compareAndSetArray.
     *
     * @see #compareAndSetArray(java.lang.Object[], java.lang.Object[])
     */
    private volatile Object[] data;

    /**
     * Create a new empty ConcurrentDispatchArray.
     */
    public ConcurrentDispatchArray() {
        data = new Object[0];
    }

    /**
     * Create a new prefilled ConcurrentDispatchArray with the specified
     * elements.
     *
     * @param base The elements to add to the list.
     */
    public ConcurrentDispatchArray(Collection<? extends E> base) {
        data = base.toArray();
    }

    public Iterator<E> iterator() {
        final Object[] dat = data;
        return new Iterator<E>() {
            private int i = 0;

            public boolean hasNext() {
                return i < dat.length;
            }

            @SuppressWarnings("unchecked")
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (E) dat[i++];
            }

            public void remove() {
                removeSpecificElement(dat[i - 1]);
            }
        };
    }

    private boolean removeSpecificElement(Object tgt) {
        Object[] previous, active;
        do {
            previous = data;
            active = new Object[previous.length - 1];
            int j;
            for (j = 0; j < previous.length; j++) {
                Object cur = previous[j];
                if (cur == tgt) {
                    break;
                }
                active[j] = cur;
            }
            if (j == previous.length) {
                // already removed. do nothing!
                return false;
            }
            for (j++; j < previous.length; j++) {
                active[j - 1] = previous[j];
            }
        } while (!compareAndSetArray(previous, active));
        return true;
    }

    /**
     * If the current array is the expected array, set the current array to the
     * updated array.
     *
     * As long as all modifications occur through this method, there will be
     * race conditions or deadlocks.
     *
     * @param expect the array that is expected to be the current value.
     * @param update the array that should replace the current array.
     * @return if the replacement completed successfully.
     */
    private synchronized boolean compareAndSetArray(Object[] expect, Object[] update) {
        if (expect == data) {
            data = update;
            return true;
        } else {
            return false;
        }
    }

    public boolean add(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        while (true) {
            Object[] old = data;
            Object[] dout = Arrays.copyOf(old, old.length + 1);
            dout[dout.length - 1] = e;
            if (compareAndSetArray(old, dout)) {
                return true;
            }
        }
    }

    public EventOutput addR(E e) {
        add(e);
        return () -> remove(e);
    }

    /**
     * Like add, but also returns false (without adding) if the element is
     * already found.
     *
     * @param e The element to add.
     * @return if e is added to the array, not already in it.
     */
    public boolean addIfNotFound(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        while (true) {
            Object[] old = data;
            for (Object o : old) {
                if (e.equals(o)) {
                    return false;
                }
            }
            Object[] dout = Arrays.copyOf(old, old.length + 1);
            dout[dout.length - 1] = e;
            if (compareAndSetArray(old, dout)) {
                return true;
            }
        }
    }

    public int size() {
        return data.length;
    }

    public boolean remove(Object o) {
        for (Iterator<E> it = this.iterator(); it.hasNext();) {
            E e = it.next();
            if (e.equals(o)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void clear() {
        data = new Object[0];
    }

    public boolean addAll(Collection<? extends E> c) { // TODO: Make this more efficient.
        boolean out = false;
        for (E e : c) {
            out |= add(e);
        }
        return out;
    }

    /**
     * Like addAll, but as if addIfNotFound were used instead of add.
     *
     * @param c The elements to add.
     * @return if anything in c is added to the array.
     */
    public boolean addAllIfNotFound(Collection<? extends E> c) { // TODO: Make this more efficient.
        boolean out = false;
        for (E e : c) {
            out |= addIfNotFound(e);
        }
        return out;
    }

    public boolean isEmpty() {
        return size() <= 0;
    }

    public boolean contains(Object o) {
        for (E cur : this) {
            if (cur.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean removeAll(Collection<?> c) {
        boolean mod = false;
        for (Iterator<E> it = this.iterator(); it.hasNext();) {
            E e = it.next();
            if (c.contains(e)) {
                it.remove();
                mod = true;
            }
        }
        return mod;
    }

    public boolean retainAll(Collection<?> c) {
        boolean mod = false;
        for (Iterator<E> it = this.iterator(); it.hasNext();) {
            E e = it.next();
            if (!c.contains(e)) {
                it.remove();
                mod = true;
            }
        }
        return mod;
    }

    public Object[] toArray() {
        Object[] d = data;
        return Arrays.copyOf(d, d.length);
    }

    public int fillArray(Object[] target) {
        Object[] d = data;
        int out = d.length - target.length;
        System.arraycopy(d, 0, target, 0, out >= 0 ? target.length : d.length);
        return out;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < data.length) a = (T[]) new Object[data.length];
        fillArray(a);
        return a;
    }
}

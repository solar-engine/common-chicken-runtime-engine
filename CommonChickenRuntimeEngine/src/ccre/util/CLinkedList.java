/*
 * Copyright 2013-2015 Colby Skeggs
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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A linked list. This will perform in O(N) for lookups and sets not at
 * endpoints, and O(N) for inserts and deletions that are not to the ends of the
 * list.
 *
 * This implementation is a circular linked list using a sentinel node for the
 * start and end of the list.
 *
 * @author skeggsc
 * @param <T> the element type.
 */
public class CLinkedList<T> extends CAbstractList<T> {

    private final Node<T> sentinel;
    private int size = 0;

    /**
     * Create a new CLinkedList.
     */
    public CLinkedList() {
        sentinel = new Node<T>(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
    }

    /**
     * Create a new CLinkedList from an existing array.
     *
     * @param coll the array to copy elements from.
     */
    public CLinkedList(T[] coll) {
        this();
        addAllFromArray(coll);
    }

    /**
     * Create a new CLinkedList from an existing collection.
     *
     * @param coll the collection to copy elements from.
     */
    public CLinkedList(CCollection<? extends T> coll) {
        this();
        addAll(coll);
    }

    private void addAllFromArray(T[] array) {
        for (T t : array) {
            addLast(t);
        }
    }

    @Override
    public Iterator<T> iterator() {
        final int modCount = getModCount();
        return new LinkedListIterator(modCount);
    }

    void decrementSize() {
        size--;
    }

    public int size() {
        return size;
    }

    @Override
    public void clear() {
        notifyModified();
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    @Override
    public boolean add(final T thing) {
        addLast(thing);
        return true;
    }

    private void requireIndexIn(final int start, final int index, final int end) {
        if (start > index || index >= end) {
            throw new IndexOutOfBoundsException("Invalid index " + index);
        }
    }

    private Node<T> getNodeAt(int index) {
        Node<T> n = sentinel;
        if (index <= size / 2) {
            for (int i = 0; i <= index; i++) {
                n = n.next;
            }
        } else {
            for (int i = 0; i < size - index; i++) {
                n = n.prev;
            }
        }
        return n;
    }

    @Override
    public void add(final int index, final T thing) {
        requireIndexIn(0, index, size + 1);
        notifyModified();
        Node<T> n = getNodeAt(index);
        Node<T> created = new Node<T>(n.prev, thing, n);
        created.prev.next = created;
        created.next.prev = created;
        size++;
    }

    public T get(final int index) {
        requireIndexIn(0, index, size);
        Node<T> n = getNodeAt(index);
        return n.o;
    }

    @Override
    public T set(final int index, final T thing) {
        requireIndexIn(0, index, size);
        notifyModified();
        Node<T> n = getNodeAt(index);
        T prev = n.o;
        n.o = thing;
        return prev;
    }

    /**
     * Sets the elements of this linked list to be the given array. The array
     * must be the same size as this list, and the elements must all be
     * instances of T.
     *
     * @param inputs the data to load.
     */
    @SuppressWarnings("unchecked")
    public void setAll(Object[] inputs) {
        if (inputs.length != size) {
            throw new IllegalArgumentException("Wrong number of arguments to CLinkedList setAll!");
        }
        Node<T> n = sentinel;
        for (Object input : inputs) {
            n = n.next;
            if (n == sentinel) {
                throw new ConcurrentModificationException();
            }
            n.o = (T) input;
        }
        if (n.next != sentinel) {
            throw new ConcurrentModificationException();
        }
        notifyModified();
    }

    @Override
    public T remove(final int index) {
        requireIndexIn(0, index, size);
        notifyModified();
        Node<T> n = getNodeAt(index);
        size--;
        n.prev.next = n.next;
        n.next.prev = n.prev;
        return n.o;
    }

    @Override
    public int indexOf(final Object thing) {
        int i = 0;
        Node<?> n = sentinel;
        while (true) {
            n = n.next;
            if (n == sentinel) {
                return -1;
            }
            if (thing.equals(n.o)) {
                return i;
            }
            i++;
        }
    }

    @Override
    public int lastIndexOf(final Object thing) {
        int i = size - 1;
        Node<?> n = sentinel;
        while (true) {
            n = n.prev;
            if (n == sentinel) {
                return -1;
            }
            if (thing.equals(n.o)) {
                return i;
            }
            i--;
        }
    }

    /**
     * Return the first element of the list. This works in O(1) time.
     *
     * @return the first element.
     */
    public T getFirst() {
        if (size == 0) {
            throw new NoSuchElementException("List is empty!");
        }
        return sentinel.next.o;
    }

    /**
     * Return the last element of the list. This works in O(1) time.
     *
     * @return the last element.
     */
    public T getLast() {
        if (size == 0) {
            throw new NoSuchElementException("List is empty!");
        }
        return sentinel.prev.o;
    }

    /**
     * Insert an element at the start of the list. This works in O(1) time.
     *
     * @param thing the element to add.
     */
    public void addFirst(final T thing) {
        notifyModified();
        Node<T> cr = new Node<T>(sentinel, thing, sentinel.next);
        cr.next.prev = cr;
        cr.prev.next = cr;
        size++;
    }

    /**
     * Insert an element at the end of the list. This works in O(1) time.
     *
     * @param thing the element to add.
     */
    public void addLast(final T thing) {
        notifyModified();
        Node<T> cr = new Node<T>(sentinel.prev, thing, sentinel);
        cr.next.prev = cr;
        cr.prev.next = cr;
        size++;
    }

    /**
     * Remove and return the first element of the list. This works in O(1) time.
     *
     * @return the first element.
     */
    public T removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("List is empty!");
        }
        notifyModified();
        size--;
        Node<T> toremove = sentinel.next;
        toremove.next.prev = sentinel;
        sentinel.next = toremove.next;
        return toremove.o;
    }

    /**
     * Remove and return the last element of the list. This works in O(1) time.
     *
     * @return the last element.
     */
    public T removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("List is empty!");
        }
        notifyModified();
        size--;
        Node<T> toremove = sentinel.prev;
        toremove.prev.next = sentinel;
        sentinel.prev = toremove.prev;
        return toremove.o;
    }

    private static final class Node<T> {

        public Node<T> next;
        public Node<T> prev;
        public T o;

        Node(Node<T> prev, T o, Node<T> next) {
            this.next = next;
            this.prev = prev;
            this.o = o;
        }
    }

    private class LinkedListIterator implements Iterator<T> {

        private Node<T> current;
        private int locmod;
        private boolean canRemove = false;

        LinkedListIterator(int lastmod) {
            current = sentinel.next;
            this.locmod = lastmod;
        }

        public boolean hasNext() {
            if (locmod != getModCount()) {
                throw new ConcurrentModificationException();
            }
            return current != sentinel;
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T out = current.o;
            current = current.next;
            canRemove = true;
            return out;
        }

        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            canRemove = false;
            notifyModified();
            decrementSize();
            current = current.prev;
            if (current == sentinel) {
                throw new IllegalStateException();
            }
            current.prev.next = current.next;
            current.next.prev = current.prev;
            current = current.next;
            locmod++;
        }
    }
}

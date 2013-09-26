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

    private final static class Node<T> {

        public Node<T> next, prev;
        public T o;

        Node(Node<T> prev, T o, Node<T> next) {
            this.next = next;
            this.prev = prev;
            this.o = o;
        }
    }
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

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> current = sentinel.next;
            int locmod = modCount;

            public boolean hasNext() {
                if (locmod != modCount) {
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
                return out;
            }

            public void remove() {
                notifyModified();
                size--;
                current.prev.next = current.next;
                current.next.prev = current.prev;
                locmod++;
            }
        };
    }

    @Override
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
    public boolean isEmpty() {
        return size == 0;
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

    @Override
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
    public boolean contains(final Object thing) {
        Node<T> n = sentinel;
        while (true) {
            n = n.next;
            if (n == sentinel) {
                return false;
            }
            if (thing.equals(n.o)) {
                return true;
            }
        }
    }

    @Override
    public int indexOf(final Object thing) {
        int i = 0;
        Node n = sentinel;
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
        Node n = sentinel;
        while (true) {
            n = n.next;
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
}

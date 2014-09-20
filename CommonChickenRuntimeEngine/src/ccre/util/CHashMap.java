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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A basic hash map. Iterate over this to iterate over all the keys. Does not
 * support null keys, but does support null values.
 *
 * @author skeggsc
 * @param <K> the key type.
 * @param <V> the value type.
 */
public final class CHashMap<K, V> implements Iterable<K> {

    /**
     * The current hash nodes of the map.
     */
    private Node<K, V>[] map;
    /**
     * The number of elements in the map.
     */
    private int size;
    /**
     * The number of structural modifications made to the CHashMap.
     */
    private transient volatile int modcount = 0;

    /**
     * Create a new CHashMap with a given initial array size.
     *
     * @param initial initial size.
     */
    public CHashMap(int initial) {
        map = CArrayUtils.castToGeneric(new Node<?, ?>[initial]);
        size = 0;
    }

    /**
     * Create a new CHashMap with a default size of 16.
     */
    public CHashMap() {
        map = CArrayUtils.castToGeneric(new Node<?, ?>[16]);
        size = 0;
    }

    /**
     * Iterate over all the keys of this CHashMap, but handle concurrent
     * modifications graciously by not having guarantees about which version of
     * the map will be returned.
     *
     * @return the iterator
     */
    public Iterator<K> looseIterator() {
        return new CHashMapIterator(map, false);
    }

    /**
     * Iterate over all the keys of this CHashMap.
     */
    public Iterator<K> iterator() {
        return new CHashMapIterator(map, true);
    }

    /**
     * Calculate a hash between 0 (inclusive) and the length of the hashmap's
     * array (exclusive), of the specified object.
     *
     * @param tgt the object.
     * @return the hash.
     */
    private int hash(Object tgt) {
        int x = tgt.hashCode();
        if (x < 0) {
            return (-x) % map.length;
        } else {
            return x % map.length;
        }
    }

    /**
     * Set the specified key in the map to the specified value.
     *
     * @param key the key.
     * @param value the value.
     * @return the previous value at that key, or null if no such key existed.
     */
    public V put(K key, V value) {
        Node<K, V> cur = map[hash(key)];
        while (cur != null) {
            if (cur.key.equals(key)) {
                V out = cur.value;
                cur.value = value;
                return out;
            }
            cur = cur.next;
        }
        if (size >= map.length * 0.75) {
            Node<K, V>[] nmap = CArrayUtils.castToGeneric(new Node<?, ?>[map.length * 2 + 1]);
            for (Node<K, V> oldnode : map) {
                Node<K, V> active = oldnode;
                while (active != null) {
                    int h = active.key.hashCode();
                    if (h < 0) {
                        h = -h;
                    }
                    h %= nmap.length;
                    nmap[h] = new Node<K, V>(active.key, active.value, nmap[h]);
                    active = active.next;
                }
            }
            map = nmap;
        }
        int h = hash(key);
        map[h] = new Node<K, V>(key, value, map[h]);
        size++;
        modcount++;
        return null;
    }

    /**
     * Get the value from the map at the specified key.
     *
     * @param key the key to get from.
     * @return the value at that key, or null if the key doesn't exist.
     */
    public V get(K key) {
        Node<K, V> n = map[hash(key)];
        while (n != null) {
            if (n.key.equals(key)) {
                return n.value;
            }
            n = n.next;
        }
        return null;
    }

    /**
     * Decide if the CHashMap contains the specified key.
     *
     * @param key the key to look for.
     * @return if the key exists.
     */
    public boolean containsKey(K key) {
        Node<K, V> n = map[hash(key)];
        while (n != null) {
            if (n.key.equals(key)) {
                return true;
            }
            n = n.next;
        }
        return false;
    }

    /**
     * Clear the CHashMap. It will no longer contain any elements.
     */
    public void clear() {
        for (int i = 0; i < map.length; i++) {
            map[i] = null;
        }
        size = 0;
        modcount++;
    }

    /**
     * Returns whether or not any key/value pairs exist in this CHashMap.
     *
     * @return true if this CHashMap contains no key/value pairs.
     */
    public boolean isEmpty() {
        return size <= 0;
    }

    /**
     * Returns the number of key/value pairs in this CHashMap.
     *
     * @return the number of key/value pairs.
     */
    public int size() {
        return size;
    }

    /**
     * Removes a key from this CHashMap.
     *
     * @param key the key to remove from this map
     * @return the removed key's value
     */
    public V remove(K key) {
        Node<K, V> n = map[hash(key)];
        Node<K, V> previous = null;
        while (n != null) {
            if (n.key.equals(key)) {
                if (previous != null) {
                    previous.next = n.next;
                } else {
                    map[hash(key)] = n.next;
                }
                size--;
                modcount++;
                return n.value;
            }
            previous = n;
            n = n.next;
        }
        return null;
    }

    private static class Node<K, V> {

        public final K key;
        public V value;
        public Node<K, V> next;

        Node(K key, V val, Node<K, V> next) {
            this.key = key;
            this.value = val;
            this.next = next;
        }
    }

    private class CHashMapIterator implements Iterator<K> {

        private final boolean checkConcurrentModification;
        private final Node<K, V>[] localMap;
        private int index = 0;
        private Node<K, V> next = null;
        private int expectedModcount = modcount;
        private K keyToRemove = null;

        CHashMapIterator(Node<K, V>[] localMap, boolean checkConcurrentModification) {
            this.localMap = localMap;
            this.checkConcurrentModification = checkConcurrentModification;
        }

        public boolean hasNext() {
            if (checkConcurrentModification && modcount != expectedModcount) {
                throw new ConcurrentModificationException();
            }
            while (next == null) {
                if (index >= localMap.length) {
                    return false;
                }
                next = localMap[index++];
            }
            return true;
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            K out = next.key;
            next = next.next;
            keyToRemove = out;
            return out;
        }

        @Override
        public void remove() {
            if (keyToRemove == null) {
                throw new IllegalStateException("Cannot remove nothing!");
            }
            if (checkConcurrentModification && modcount != expectedModcount) {
                throw new ConcurrentModificationException();
            }
            CHashMap.this.remove(keyToRemove);
            expectedModcount++;
        }
    }
}

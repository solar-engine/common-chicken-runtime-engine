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

// This class is only saved in case I need to use it later. It's not actually used or finished.

/*public class Heap<T extends Comparable<T>> {

    protected Object[] values;
    protected int size;

    public Heap() {
        values = new Object[32];
    }

    public Heap(int cnt) {
        values = new Object[cnt];
    }

    public synchronized void insert(T val) {
        if (size >= values.length) {
            // If array is full, double the size.
            values = CArrayUtils.copyOf(values, values.length << 1);
        }
        // Add to the bottom level of the heap
        int cur = size++;
        // Swap up the chain until the current value is less than the parent
        while (cur != 0) {
            int chi = cur;
            cur = (chi - 1) >> 1; // Get parent
            T vcr = (T) values[cur];
            if (val.compareTo(vcr) < 0) {
                values[chi] = val;
                return;
            }
            values[chi] = vcr;
        }
        values[cur] = val;
    }
    
    public boolean hasNext() {
        return size > 0;
    }

    public synchronized T peek() {
        if (size <= 0) {
            return null;
        } else {
            return (T) values[0];
        }
    }

    public synchronized T remove() {
        if (size <= 0) {
            return null;
        }
        T oldroot = (T) values[0];
        // Remove last element and put it in the root
        values[0] = values[--size];
        int i = 0;
        while (true) {
            int left = (i << 1) + 1; // calculate left child
            int right = (i << 1) + 2; // calculate right child
            int largest = i;
            // find largest out of I and its children
            if (left <= size && ((T) values[left]).compareTo((T) values[largest]) > 0) {
                largest = left;
            }
            if (right <= size && ((T) values[right]).compareTo((T) values[largest]) > 0) {
                largest = right;
            }
            // if it's largest, we're done. otherwise swap them and repeat on the target.
            if (largest != i) {
                Object o = values[i];
                values[i] = values[largest];
                values[largest] = o;
                i = largest;
            } else {
                break;
            }
        }
        return oldroot;
    }
}*/

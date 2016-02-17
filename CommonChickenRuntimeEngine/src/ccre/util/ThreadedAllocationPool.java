/*
 * Copyright 2016 Cel Skeggs
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

public class ThreadedAllocationPool<E> {
    private final ArrayBlockingQueue<E> queue;
    private final Supplier<E> constructor;

    public ThreadedAllocationPool(int size, Supplier<E> constructor) {
        this.queue = new ArrayBlockingQueue<>(size);
        this.constructor = constructor;
    }

    public boolean free(E e) {
        return this.queue.offer(e);
    }

    public E allocate() {
        E ent = this.queue.poll();
        if (ent == null) {
            ent = this.constructor.get();
        }
        return ent;
    }
}

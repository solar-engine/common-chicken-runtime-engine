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

import ccre.verifier.FlowPhase;

/**
 * A thread-safe allocation pool with a maximum size that allocates additional
 * objects on demand.
 *
 * @author skeggsc
 * @param <E> the type of the allocated objects.
 */
public final class ThreadedAllocationPool<E> {
    private final ArrayBlockingQueue<E> queue;
    private final Supplier<E> constructor;

    /**
     * Creates a ThreadedAllocationPool. {@link Supplier#get()} will be called
     * on <code>constructor</code> whenever a new instance is needed.
     *
     * The easy way to use this is to write
     * <code>new ThreadedAllocationPool(1024, SomeClass::new)</code>, which will
     * dispatch to the zero-argument constructor of <code>SomeClass</code>.
     *
     * @param size the maximum number of cached instances, not including
     * instances currently handed out.
     * @param constructor the constructor for the pool.
     */
    public ThreadedAllocationPool(int size, Supplier<E> constructor) {
        this.queue = new ArrayBlockingQueue<>(size);
        this.constructor = constructor;
    }

    /**
     * Returns an instance back to the pool. Be certain that you do not use this
     * instance afterward, because another thread could have taken it out.
     *
     * If the pool is full, the instance will be discarded.
     *
     * @param e the instance to return.
     * @return true if the instance was actually used, false if it was
     * discarded.
     */
    @FlowPhase
    public boolean free(E e) {
        return this.queue.offer(e);
    }

    /**
     * Acquires an instance. If an instance is available in the pool, it will be
     * removed and returned. Otherwise, a new instance will be allocated and
     * returned.
     *
     * @return the instance.
     */
    @FlowPhase
    public E allocate() {
        E ent = this.queue.poll();
        if (ent == null) {
            ent = this.constructor.get();
        }
        return ent;
    }
}

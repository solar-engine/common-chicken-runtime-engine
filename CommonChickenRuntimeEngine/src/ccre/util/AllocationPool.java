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
 * A superclass of a basic allocate/release allocation pool. Call alloc() to get
 * a value of type T, call release(T) once finished with it. This will minimize
 * actual allocations.
 *
 * @author skeggsc
 * @param <T> the type that gets allocated.
 */
public abstract class AllocationPool<T> {

    /**
     * The list of available instances.
     */
    private final CArrayList<T> available = new CArrayList<T>(10);

    /**
     * Create a new instance, because there are no instances remaining.
     *
     * @return the new instance.
     */
    protected abstract T allocateNew();

    /**
     * Get an instance. This might be a reused instance or a new instance.
     *
     * @return an instance.
     */
    public synchronized T alloc() {
        if (available.isEmpty()) {
            return allocateNew();
        } else {
            return available.remove(available.size() - 1);
        }
    }

    /**
     * Release an instance. After this point, it may be allocated by a
     * subsequent call to alloc(). Do not use the instance after this point.
     *
     * @param value the instance to release.
     */
    public synchronized void release(T value) {
        available.add(value);
    }
}

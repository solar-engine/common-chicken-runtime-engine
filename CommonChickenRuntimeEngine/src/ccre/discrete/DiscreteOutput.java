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
package ccre.discrete;

import ccre.log.Logger;

/**
 * A DiscreteOutput is an interface for anything that can be set to a value
 * selected from a limited list of values.
 *
 * @author skeggsc
 *
 * @param <E> the type of the discrete data
 */
public interface DiscreteOutput<E> {
    /**
     * Gets the type of this DiscreteOutput.
     *
     * @return the type, as a DiscreteType instance.
     */
    public DiscreteType<E> getType();

    /**
     * Sets the discrete value of this output.
     *
     * If any exception occurs during the propagation of the changes, it will be
     * passed on by <code>set</code>.
     *
     * @param value the new value to send to this output.
     * @see #safeSet(Object) for a version that catches any errors that occur.
     */
    public void set(E value);

    /**
     * Sets the discrete value of this output.
     *
     * If any exception occurs during the propagation of the changes,
     * <code>safeSet</code> will catch and log it as a
     * {@link ccre.log.LogLevel#SEVERE} error.
     *
     * @param value the new value to send to this output.
     * @see #set(Object) for a version that throws any errors that occur.
     */
    public default void safeSet(E value) {
        try {
            set(value);
        } catch (Throwable ex) {
            Logger.severe("Error during channel propagation", ex);
        }
    }
}

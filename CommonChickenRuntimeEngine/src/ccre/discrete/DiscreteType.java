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

import ccre.verifier.IgnoredPhase;

/**
 * A representation of the type of a discrete input or output.
 *
 * @author skeggsc
 * @param <E> the type represented by this discrete type
 */
public interface DiscreteType<E> {
    /**
     * Gets the class from which all instances of this discrete class are
     * derived, directly or indirectly.
     *
     * @return the class for <code>E</code>
     */
    @IgnoredPhase
    public Class<E> getType();

    /**
     * Enumerates the possible values of this discrete type.
     *
     * @return an array of all of the possibilities.
     */
    @IgnoredPhase
    public E[] getOptions();

    /**
     * Checks whether or not <code>value</code> is one of the possible values of
     * this discrete type.
     *
     * @param value the instance to check.
     * @return true if <code>value</code> is a valid instance, and false
     * otherwise
     */
    @IgnoredPhase
    public boolean isOption(E value);

    /**
     * Converts one of the options to a meaningful string in the context of this
     * discrete type.
     *
     * @param value the instance to check.
     * @return the string expression, which may be different from
     * <code>value.toString()</code>
     */
    @IgnoredPhase
    public String toString(E value);

    /**
     * Gets the default value of this discrete type, similar to zero for floats
     * or false for booleans.
     *
     * @return the default value.
     */
    @IgnoredPhase
    public E getDefaultValue();
}

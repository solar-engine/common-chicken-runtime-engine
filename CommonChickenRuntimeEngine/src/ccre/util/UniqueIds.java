/*
 * Copyright 2014 Colby Skeggs
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
 * A generator that generates Unique Identifiers for use in other CCRE
 * subsystems.
 *
 * @author skeggsc
 */
public class UniqueIds {

    /**
     * The global Unique Id generator.
     */
    public static final UniqueIds global = new UniqueIds();

    /**
     * The next unique identifier to be generated from this instance.
     */
    private int nextId = 0;

    /**
     * Generate a unique identifier in the form of an integer.
     *
     * @return The unique identifier.
     */
    public synchronized int nextId() {
        return nextId++;
    }

    /**
     * Generate a unique identifier in the form of a hexadecimal string.
     *
     * @return The unique identifier.
     */
    public String nextHexId() {
        return Integer.toHexString(nextId());
    }

    /**
     * Generate a unique identifier in the form of a specified prefix, a dash
     * ('-'), and then a unique hexadecimal string.
     *
     * @param prefix A prefix to go before the number.
     * @return The unique identifier.
     */
    public String nextHexId(String prefix) {
        return prefix + "-" + Integer.toHexString(nextId());
    }
}

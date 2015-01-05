/*
 * Copyright 2015 Colby Skeggs
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
package java.lang;

/**
 * A reimplementation of java.lang.Comparable, used for cRIO downgrading. Should
 * work exactly the same as the equivalent.
 * 
 * @author skeggsc
 * @param <T> the type to be comparable to.
 */
public interface Comparable<T> {
    /**
     * Compare this object to the other object. Should return negative numbers
     * for less than, zero for equal, and positive for greater than.
     * 
     * @param o the object to compare with.
     * @return a comparison integer.
     */
    int compareTo(T o);
}

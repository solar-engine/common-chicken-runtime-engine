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
package com.sun.squawk;

/**
 * Provides access for the CCRE to some of the package-private Squawk internals.
 *
 * @author skeggsc
 */
public class InternalsProxy {
    /**
     * Get the Method object behind the specified ExecutionPoint, if it exists.
     *
     * This is provided because com.sun.squawk.ExecutionPoint.getMethod is
     * broken during the build process. (No, I'm not quite sure why.)
     *
     * @param ep the ExecutionPoint to access.
     * @return the Method object.
     * @see com.sun.squawk.ExecutionPoint#getMethod()
     */
    public static Method getMethod(ExecutionPoint ep) {
        return ep.getKlass().findMethod(ep.mp);
    }
}

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
package ccre.channel;

/**
 * An event output or consumer. This can be fired (or produced or triggered or
 * activated or a number of other verbs that all mean the same thing), which
 * does something depending on where it came from.
 *
 * @author skeggsc
 */
public interface EventOutput {

    /**
     * Fire the event.
     */
    void event();

    /**
     * Fire the event with recovery: try to recover instead of throwing an
     * exception.
     * 
     * @return if anything was changed to recover from an error.
     */
    default boolean eventWithRecovery() {
        event();
        return false;
    }
}

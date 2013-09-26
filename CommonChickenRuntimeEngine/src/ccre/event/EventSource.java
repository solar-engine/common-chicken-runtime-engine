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
package ccre.event;

/**
 * An event source. This produces events when it fires. A user can register
 * listeners to be called when the EventSource fires. ccre.event.Event is a good
 * implementation of this so that you don't have to write your own listener
 * management code.
 *
 * @see Event
 * @author skeggsc
 */
public interface EventSource {

    /**
     * Register a listener for when this event is fired. Return false if the
     * listener was already registered, or true if it wasn't.
     *
     * @param listener the listener to add.
     * @return whether or not it was actually added.
     */
    boolean addListener(EventConsumer listener);

    /**
     * Remove a listener for when this event is fired. If the specified listener
     * wasn't registered, throw an IllegalStateException.
     *
     * @param listener the listener to remove.
     * @throws IllegalStateException if the listener wasn't registered.
     */
    void removeListener(EventConsumer listener) throws IllegalStateException;
}

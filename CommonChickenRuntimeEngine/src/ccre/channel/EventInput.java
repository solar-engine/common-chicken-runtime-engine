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
 * An event input or source. This produces events when it fires. A user can
 * register listeners to be called when the EventInput fires.
 * ccre.event.EventStatus is a good implementation of this so that you don't
 * have to write your own listener management code.
 *
 * @see EventStatus
 * @author skeggsc
 */
public interface EventInput {

    /**
     * Register a listener for when this event is fired.
     *
     * @param listener the listener to add.
     */
    void send(EventOutput listener);

    /**
     * Remove a listener for when this event is fired.
     *
     * @param listener the listener to remove.
     */
    void unsend(EventOutput listener);
}

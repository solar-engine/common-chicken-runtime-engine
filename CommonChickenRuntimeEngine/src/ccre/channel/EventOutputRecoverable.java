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
 * An event output or consumer that supports recovery events. When an event is
 * fired in recovery mode, this means that it should avoid throwing exceptions
 * as much as possible and should instead try to permanently correct for any
 * that would be thrown. An example would be an EventStatus detaching one of its
 * subscribers.
 *
 * @author skeggsc
 */
public interface EventOutputRecoverable extends EventOutput {

    /**
     * Fire the event with recovery: try to recover instead of throwing an
     * exception.
     * 
     * @return if anything was changed to recover from an error.
     */
    boolean eventWithRecovery();
}

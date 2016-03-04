/*
 * Copyright 2016 Cel Skeggs.
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
package ccre.time;

import ccre.channel.EventOutput;

/**
 * Provides an accessor to one of the package-private methods of FakeTime that
 * we don't want to make available. Since this is in the <code>tests</code>
 * folder, it's not part of the API and is not subject to backwards
 * compatibility guarantees.
 *
 * @author skeggsc
 */
public class SharedTimeSecrets {
    /**
     * Schedules an event to occur when the specified {@link FakeTime} reaches
     * the correct time. The time scale is based on {@link FakeTime#nowNanos()}.
     *
     * @param time the FakeTime instance.
     * @param event the event to fire when the time changes.
     * @param at the (fake) time at which to fire the event, in nanoseconds.
     */
    public static void scheduleFakeLoop(FakeTime time, EventOutput event, long at) {
        time.schedule(event, at);
    }
}

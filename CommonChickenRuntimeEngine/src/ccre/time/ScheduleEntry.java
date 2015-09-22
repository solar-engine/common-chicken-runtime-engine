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
package ccre.time;

import ccre.channel.EventOutput;

final class ScheduleEntry implements Comparable<ScheduleEntry> {// TODO: make
                                                                // sure that
                                                                // everything
                                                                // works with
                                                                // time
                                                                // wraparound
                                                                // (hint: it
                                                                // doesn't now)
    public final long expirationAt;
    public final EventOutput target;

    ScheduleEntry(long expirationAt, EventOutput target) {
        this.expirationAt = expirationAt;
        this.target = target;
    }

    @Override
    public int compareTo(ScheduleEntry o) {
        return Long.compare(expirationAt, o.expirationAt);
    }
}
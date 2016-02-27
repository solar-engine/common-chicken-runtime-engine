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
package ccre.scheduler;

import ccre.channel.CancelOutput;
import ccre.channel.EventOutput;
import ccre.time.Time;

public class Scheduler {
    // never modified except in unit tests - treat as if it were final
    private static FullLoop mainloop = new FullLoop();

    static {
        mainloop.start();
    }

    public static void scheduleNanos(String tag, long nanos, EventOutput o) {
        mainloop.scheduleOnce(tag, Time.currentTimeNanos() + nanos, o);
    }

    public static void scheduleAt(String tag, long nanoAt, EventOutput o) {
        mainloop.scheduleOnce(tag, nanoAt, o);
    }

    public static CancelOutput scheduleCancellableNanos(String tag, long nanos, EventOutput o) {
        return mainloop.scheduleCancellableOnce(tag, Time.currentTimeNanos() + nanos, o);
    }

    public static CancelOutput scheduleCancellableAt(String tag, long nanoAt, EventOutput o) {
        return mainloop.scheduleCancellableOnce(tag, nanoAt, o);
    }

    public static CancelOutput schedulePeriodicNanos(String tag, long nanos, EventOutput o) {
        return mainloop.scheduleVariableRate(tag, nanos, o);
    }

    public static CancelOutput scheduleFixedRateNanos(String tag, long nanos, EventOutput o) {
        return mainloop.scheduleFixedRate(tag, Time.currentTimeNanos() + nanos, nanos, false, o);
    }

    // only used in unit tests
    static synchronized void __UNSAFE_reset(IRunLoop loop) {
        // unsafe b/c it cancels anything currently scheduled and it's sketchy
        mainloop.terminate();
        mainloop = new FullLoop(loop);
        mainloop.start();
    }
}

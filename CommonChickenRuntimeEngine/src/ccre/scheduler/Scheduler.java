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
import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

/**
 * The main interface to the CCRE's scheduling thread. You can use this to
 * register events to occur once or multiple times. A FIFO scheduler is used
 * with no priorities. Specifically, the most proximal job will be executed, at
 * a nanosecond granularity, and if multiple jobs are scheduled at the same
 * time, the order is undefined.
 *
 * Jobs can run at any time after they are scheduled for - there are no
 * guarantees provided, except that any job will run before any job scheduled
 * for a later time.
 *
 * General concepts: there are both <code>schedule[...]Nanos</code> and
 * <code>schedule[...]At</code> methods of various types: <code>Nanos</code>
 * means that it will occur a specified amount of time after the invocation, and
 * <code>At</code> means that the time to occur after will be specified, in the
 * range returned by {@link Time#currentTimeNanos()}.
 *
 * The schedule methods also take scheduler tags, which are strings (hopefully
 * constant to avoid reallocation issues) that are used for scheduler debugging,
 * and are required for every schedule performed.
 *
 * Scheduled jobs must be provided as {@link EventOutput}s, and their
 * {@link EventOutput#event()} method will be called to perform the job.
 *
 * @author skeggsc
 */
public class Scheduler {
    // never modified except in unit tests - treat as if it were final
    private static FullLoop mainloop = new FullLoop();

    static {
        mainloop.start();
    }

    /**
     * Schedule an event to occur at a certain number of nanoseconds in the
     * future. The current time for the purpose of the target time is chosen at
     * an unspecified point during the execution of this method, usually as
     * early as possible.
     *
     * @param tag the scheduler tag.
     * @param nanos the number of nanoseconds in the future to schedule this
     * event at.
     * @param o the event to fire.
     */
    @FlowPhase
    public static void scheduleNanos(String tag, long nanos, EventOutput o) {
        mainloop.scheduleOnce(tag, Time.currentTimeNanos() + nanos, o);
    }

    /**
     * Schedule an event to occur at a certain nanosecond index, based on
     * {@link Time#currentTimeNanos()}.
     *
     * @param tag the scheduler tag.
     * @param nanoAt the time at which to run the event.
     * @param o the event to fire.
     */
    @FlowPhase
    public static void scheduleAt(String tag, long nanoAt, EventOutput o) {
        mainloop.scheduleOnce(tag, nanoAt, o);
    }

    /**
     * Schedule an event to occur at a certain number of nanoseconds in the
     * future. The current time for the purpose of the target time is chosen at
     * an unspecified point during the execution of this method, usually as
     * early as possible.
     *
     * The event can be cancelled before it occurs by calling
     * {@link CancelOutput#cancel()} on the returned {@link CancelOutput}.
     *
     * @param tag the scheduler tag.
     * @param nanos the number of nanoseconds in the future to schedule this
     * event at.
     * @param o the event to fire.
     * @return the {@link CancelOutput} that can cancel the event.
     */
    @FlowPhase
    public static CancelOutput scheduleCancellableNanos(String tag, long nanos, EventOutput o) {
        return mainloop.scheduleCancellableOnce(tag, Time.currentTimeNanos() + nanos, o);
    }

    /**
     * Schedule an event to occur at a certain nanosecond index, based on
     * {@link Time#currentTimeNanos()}.
     *
     * The event can be cancelled before it occurs by calling
     * {@link CancelOutput#cancel()} on the returned {@link CancelOutput}.
     *
     * @param tag the scheduler tag.
     * @param nanoAt the time at which to run the event.
     * @param o the event to fire.
     * @return the {@link CancelOutput} that can cancel the event.
     */
    @FlowPhase
    public static CancelOutput scheduleCancellableAt(String tag, long nanoAt, EventOutput o) {
        return mainloop.scheduleCancellableOnce(tag, nanoAt, o);
    }

    /**
     * Schedule an event to occur every <code>nanos</code> nanoseconds until
     * cancelled by calling {@link CancelOutput#cancel()} on the returned
     * {@link CancelOutput}.
     *
     * Unlike {@link #scheduleFixedRateNanos(String, long, EventOutput)},
     * scheduling for subsequent events will be based on when the schedule
     * actually occurs, not when it would in theory have occurred. This is so
     * that, under load, this event won't bog down the system badly, but it
     * increases jitter.
     *
     * @param tag the scheduler tag.
     * @param nanos the number of nanoseconds between scheduled events.
     * @param o the event to fire.
     * @return the {@link CancelOutput} that can cancel the event.
     */
    @SetupPhase
    public static CancelOutput schedulePeriodicNanos(String tag, long nanos, EventOutput o) {
        return mainloop.scheduleVariableRate(tag, nanos, o);
    }

    /**
     * Schedule an event to occur every <code>nanos</code> nanoseconds until
     * cancelled by calling {@link CancelOutput#cancel()} on the returned
     * {@link CancelOutput}.
     *
     * Unlike {@link #schedulePeriodicNanos(String, long, EventOutput)},
     * scheduling for subsequent events will be based on when the event was
     * supposed to run, not when it actually ran. This is so that, overall, the
     * rate will be consistent, even across load spikes.
     *
     * @param tag the scheduler tag.
     * @param nanos the number of nanoseconds between scheduled events.
     * @param o the event to fire.
     * @return the {@link CancelOutput} that can cancel the event.
     */
    @SetupPhase
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

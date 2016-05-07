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
package ccre.timers;

import ccre.channel.AbstractUpdatingInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.scheduler.Scheduler;
import ccre.time.Time;
import ccre.util.Utils;
import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

/**
 * A timer that reports the amount of time since the last reset.
 *
 * @author skeggsc
 */
public final class StopwatchTimer extends AbstractUpdatingInput implements FloatInput {
    private volatile long startNanos, lastNanos;

    /**
     * Creates a new timer with the default period of 10 milliseconds.
     */
    public StopwatchTimer() {
        this(Utils.getMethodCaller(1).toString(), 10);
    }

    /**
     * Creates a new timer with the specified period.
     *
     * @param period the interval of time between updates, in milliseconds.
     */
    public StopwatchTimer(int period) {
        this(Utils.getMethodCaller(1).toString(), period);
    }

    /**
     * Creates a new timer with the specified period and a descriptive tag for
     * the time that it consumes.
     *
     * @param tag the scheduler tag.
     * @param period the interval of time between updates, in milliseconds.
     */
    public StopwatchTimer(String tag, int period) {
        if (period <= 0) {
            throw new IllegalArgumentException();
        }
        Scheduler.schedulePeriodicNanos(tag, period * Time.NANOSECONDS_PER_MILLISECOND, () -> {
            lastNanos = Time.currentTimeNanos();
            perform();
        });
        reset();
    }

    /**
     * Resets the timer to zero.
     */
    @FlowPhase
    public void reset() {
        lastNanos = startNanos = Time.currentTimeNanos();
        perform();
    }

    /**
     * Provides an EventOutput that resets the timer to zero.
     *
     * @return the EventOutput.
     */
    @SetupPhase
    public EventOutput eventReset() {
        return this::reset;
    }

    /**
     * Resets the timer to zero whenever <code>when</code> fires.
     *
     * @param when when the timer should reset.
     */
    @SetupPhase
    public void resetWhen(EventInput when) {
        when.send(this.eventReset());
    }

    @Override
    public float get() {
        return (lastNanos - startNanos) / (float) Time.NANOSECONDS_PER_SECOND;
    }
}

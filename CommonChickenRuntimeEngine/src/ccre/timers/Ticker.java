/*
 * Copyright 2013-2016 Cel Skeggs
 * Copyright 2013 Vincent Miller
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
import ccre.channel.CancelOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.log.Logger;
import ccre.scheduler.Scheduler;
import ccre.time.Time;
import ccre.util.Utils;

/**
 * An EventInput that will fire the event in all its consumers at a specified
 * interval.
 *
 * @author MillerV, SkeggsC
 */
public final class Ticker extends AbstractUpdatingInput implements EventInput {

    private CancelOutput terminate;
    private final boolean fixedRate;
    private final int millisPeriodic;
    private final String tag;

    /**
     * Create a new Ticker with the specified interval. The timer will start
     * immediately, executing for the first time after the specified interval.
     *
     * This will not run at a fixed rate, as extra time taken for one cycle will
     * not be corrected for in the time between the cycles.
     *
     * @param interval The desired interval, in milliseconds.
     */
    public Ticker(int interval) {
        this(Utils.getMethodCaller(1).toString(), interval, false);
    }

    /**
     * Create a new Ticker with the specified interval and fixed rate option.
     * The timer will start immediately, executing for the first time after the
     * specified interval.
     *
     * If fixedRate is false, this will not run at a fixed rate, as extra time
     * taken for one cycle will not be corrected for in the time between the
     * cycles.
     *
     * If fixedRate is true, this will run at a fixed rate, as extra time taken
     * for one cycle will be removed from the time before the subsequent cycle.
     * This does mean that if a cycle takes too long, that produces of the event
     * can bunch up and execute a number of times back-to-back.
     *
     * @param interval The desired interval, in milliseconds.
     * @param fixedRate Should the rate be corrected?
     */
    public Ticker(int interval, boolean fixedRate) {
        this(Utils.getMethodCaller(1).toString(), interval, fixedRate);
    }

    public Ticker(String tag, int interval, boolean fixedRate) {
        this.tag = tag;
        this.millisPeriodic = interval;
        this.fixedRate = fixedRate;
    }

    /**
     * Destroys this Ticker. It won't function after this.
     */
    public synchronized void terminate() {
        if (this.terminate != null) {
            this.terminate.cancel();
            this.terminate = CancelOutput.nothing;
        }
        this.__UNSAFE_clearListeners();
    }

    @Override
    public CancelOutput onUpdate(EventOutput notify) {
        if (terminate == CancelOutput.nothing) {
            throw new IllegalStateException("Already terminated!");
        }
        if (terminate == null) {
            synchronized (this) {
                if (terminate == null) {
                    try {
                        if (fixedRate) {
                            this.terminate = Scheduler.scheduleFixedRateNanos(this.tag, this.millisPeriodic * (long) Time.NANOSECONDS_PER_MILLISECOND, this::perform);
                        } else {
                            this.terminate = Scheduler.schedulePeriodicNanos(this.tag, this.millisPeriodic * (long) Time.NANOSECONDS_PER_MILLISECOND, this::perform);
                        }
                    } catch (Throwable thr) {
                        Logger.severe("Could not start Ticker!", thr);
                    }
                }
            }
        }
        return super.onUpdate(notify);
    }
}

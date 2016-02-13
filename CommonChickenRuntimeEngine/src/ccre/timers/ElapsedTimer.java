/*
 * Copyright 2016 Alexander Mackworth
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

import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.concurrency.ReporterThread;
import ccre.time.Time;

/**
 * A timer that provides a channel that reads the amount of time since it was
 * last reset.
 * 
 * @author amackworth
 */
public final class ElapsedTimer {
    private final int period;
    private final FloatCell cell = new FloatCell();
    private volatile boolean terminated = false;
    private long startTime;

    /**
     * When fired, resets the timer.
     */
    public final EventOutput reset = () -> {
        startTime = Time.currentTimeMillis();
    };

    /**
     * Create a new timer, with a default sleep period of 10 milliseconds
     * between updates.
     */
    public ElapsedTimer() {
        this(10);
    }

    /**
     * Creates a new timer with the specified period.
     * 
     * @param period the amount of time to sleep between updates, in
     * milliseconds.
     */
    public ElapsedTimer(int period) {
        this.period = period;
    }

    /**
     * A channel that updates with the amount of time passed since the timer was
     * reset, in milliseconds.
     * 
     * @return the channel.
     */
    public FloatInput getElapsedTimeMillis() {
        return cell;
    }

    private final ReporterThread main = new ReporterThread("ElapsedTimer") {
        @Override
        protected void threadBody() throws Throwable {
            try {
                while (!terminated) {
                    cell.safeSet(Time.currentTimeMillis() - startTime);
                    Time.sleep(period);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    };

    /**
     * Start running the timer.
     */
    public synchronized void start() {
        terminated = false;
        if (!main.isAlive()) {
            main.start();
        }
    }

    /**
     * Terminate the update loop of the timer.
     */
    public synchronized void terminate() {
        terminated = true;
        main.interrupt();
    }
}
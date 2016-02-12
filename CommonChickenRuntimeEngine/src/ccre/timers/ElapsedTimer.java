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

import ccre.channel.BooleanCell;
import ccre.time.Time;

/**
 * A timer that, when read, returns the amount of time since it was last reset.
 * 
 * @author amackworth
 */
public final class ElapsedTimer {
    /**
     * Is the timer currently running?
     */
    public final BooleanCell isRunning = new BooleanCell();
    private long startTime;

    /**
     * Starts the timer. Note: {@link #reset()} must be called first before a timer is started anew.
     */
    public void start() {
        if (isRunning.get()) {
            throw new IllegalStateException("Timer is running!");
        }

        isRunning.safeSet(true);
        startTime = Time.currentTimeMillis();
    }

    /**
     * Reads how long the timer has been running.
     * 
     * @return the milliseconds passed since the timer was started.
     */
    public long read() {
        if (!isRunning.get()) {
            throw new IllegalStateException("Timer not running!");
        }

        return Time.currentTimeMillis() - startTime;
    }

    /**
     * Stops and resets the timer.
     */
    public void reset() {
        isRunning.safeSet(false);
        startTime = 0;
    }
}
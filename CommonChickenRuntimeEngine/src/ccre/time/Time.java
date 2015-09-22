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

public abstract class Time {

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int MICROSECONDS_PER_SECOND = 1000000;
    public static final int NANOSECONDS_PER_SECOND = 1000000000;
    public static final int MICROSECONDS_PER_MILLISECOND = 1000;
    public static final int NANOSECONDS_PER_MILLISECOND = 1000000;
    public static final int NANOSECONDS_PER_MICROSECOND = 1000;

    private static Time time = new NormalTime(0);// allows for replacement as
                                                 // necessary. for example while
                                                 // testing.

    public static void setTimeProvider(Time time) {// TODO: not thread safe -
                                                   // does this need to be?
        if (time == null) {
            throw new NullPointerException();
        }
        Time old = Time.time;
        // logger.info("Replacing time provider "
        // + old + " with " + time);
        Time.time = time;
        // logger.info("Replaced time provider!");
        old.close();// we do it afterwards so that anything new will get sent to
                    // the new time provider
    }

    public static Time getTimeProvider() {
        return time;
    }

    public static long currentTimeMillis() {
        return time.nowMillis();
    }

    public static long currentTimeNanos() {
        return time.nowNanos();
    }

    // TODO: fix tests

    public static void sleep(long millis) throws InterruptedException {
        time.sleepFor(millis);
    }

    public static void wait(Object object, long timeout) throws InterruptedException {
        time.waitOn(object, timeout);
    }

    public static void scheduleLightweight(long millis, EventOutput update) {
        time.scheduleTimer(millis, update);
    }

    protected abstract long nowMillis();

    protected abstract long nowNanos();

    protected abstract void sleepFor(long millis) throws InterruptedException;

    protected abstract void waitOn(Object object, long timeout) throws InterruptedException;

    protected abstract void scheduleTimer(long millis, EventOutput update);

    protected abstract void close();
}

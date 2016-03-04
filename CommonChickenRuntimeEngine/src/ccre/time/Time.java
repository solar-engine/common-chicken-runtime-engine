/*
 * Copyright 2015-2016 Cel Skeggs
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

/**
 * An abstraction of time that allows multiple implementations to be used. For
 * example, time can be faked during execution of JUnit tests.
 *
 * This abstraction is for control-based time, not human time: it is useless for
 * tracking dates, for example.
 *
 * @author skeggsc
 */
public abstract class Time {

    /**
     * The number of milliseconds in a second. This is for use in code such that
     * fewer magic numbers are necessary.
     */
    public static final long MILLISECONDS_PER_SECOND = 1000;
    /**
     * The number of microseconds in a second. This is for use in code such that
     * fewer magic numbers are necessary.
     */
    public static final long MICROSECONDS_PER_SECOND = 1000000;
    /**
     * The number of nanoseconds in a second. This is for use in code such that
     * fewer magic numbers are necessary.
     */
    public static final long NANOSECONDS_PER_SECOND = 1000000000;
    /**
     * The number of microseconds in a millisecond. This is for use in code such
     * that fewer magic numbers are necessary.
     */
    public static final long MICROSECONDS_PER_MILLISECOND = 1000;
    /**
     * The number of nanoseconds in a millisecond. This is for use in code such
     * that fewer magic numbers are necessary.
     */
    public static final long NANOSECONDS_PER_MILLISECOND = 1000000;
    /**
     * The number of nanoseconds in a microsecond. This is for use in code such
     * that fewer magic numbers are necessary.
     */
    public static final long NANOSECONDS_PER_MICROSECOND = 1000;

    /**
     * The current provider for time. This allows replacement as necessary, for
     * example while testing.
     */
    private static Time time = new NormalTime(0);

    /**
     * Changes the current provider that specifies the time in the CCRE. This is
     * not threadsafe! The old time provider's {@link #close()} method will be
     * called once the new provider is installed.
     *
     * @param time the new time provider.
     */
    // TODO: not thread safe - does this need to be?
    public static void setTimeProvider(Time time) {
        if (time == null) {
            throw new NullPointerException();
        }
        Time old = Time.time;
        Time.time = time;
        // we do it afterwards so that anything new will get sent to the new
        // time provider
        old.close();
    }

    /**
     * Gets the current time provider. This is a {@link NormalTime} instance by
     * default.
     *
     * @return the Time instance.
     */
    public static Time getTimeProvider() {
        return time;
    }

    /**
     * Queries the current time based on an unspecified zero point. This is not
     * suitable for determining the time of day, but is suitable for tracking
     * the time since an event.
     *
     * This delegates to {@link #nowMillis()} on the current provider.
     *
     * @return the current time, in milliseconds.
     */
    public static long currentTimeMillis() {
        return time.nowMillis();
    }

    /**
     * Queries the current time based on an unspecified zero point. This is not
     * suitable for determining the time of day, but is suitable for tracking
     * the time since an event.
     *
     * This delegates to {@link #nowNanos()} on the current provider.
     *
     * @return the current time, in nanoseconds.
     */
    public static long currentTimeNanos() {
        return time.nowNanos();
    }

    /**
     * Sleeps for at least the specified amount of time before returning, unless
     * the thread is interrupted.
     *
     * The call to <code>sleep</code> may last longer than specified in a number
     * of cases.
     *
     * This delegates to the current provider's {@link #sleepFor(long)} method.
     *
     * @param millis how long to sleep for.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    public static void sleep(long millis) throws InterruptedException {
        time.sleepFor(millis);
    }

    /**
     * Waits for an object's monitor to be notified, as if
     * {@link Object#wait(long)} were called.
     *
     * The call to <code>wait</code> may last longer than specified in a number
     * of cases.
     *
     * This delegates to the current provider's {@link #waitOn(Object, long)}
     * method.
     *
     * @param object the object to wait on.
     * @param timeout how long to sleep for, in milliseconds.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public static void wait(Object object, long timeout) throws InterruptedException {
        time.waitOn(object, timeout);
    }

    /**
     * Queries the current time based on an unspecified zero point. This is not
     * suitable for determining the time of day, but is suitable for tracking
     * the time since an event.
     *
     * @return the current time, in milliseconds.
     */
    protected abstract long nowMillis();

    /**
     * Queries the current time based on an unspecified zero point. This is not
     * suitable for determining the time of day, but is suitable for tracking
     * the time since an event.
     *
     * @return the current time, in nanoseconds.
     */
    protected abstract long nowNanos();

    /**
     * Sleeps for at least the specified amount of time before returning, unless
     * the thread is interrupted.
     *
     * The call to <code>sleep</code> may last longer than specified in a number
     * of cases.
     *
     * This delegates to the current provider's {@link #sleepFor(long)} method.
     *
     * @param millis how long to sleep for.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    protected abstract void sleepFor(long millis) throws InterruptedException;

    /**
     * Waits for an object's monitor to be notified, as if
     * {@link Object#wait(long)} were called.
     *
     * The call to <code>wait</code> may last longer than specified in a number
     * of cases. It may also have spurious wake-ups, where it wakes up without
     * any cause. This may occur frequently.
     *
     * This delegates to the current provider's {@link #waitOn(Object, long)}
     * method.
     *
     * @param object the object to wait on.
     * @param timeout how long to sleep for, in milliseconds.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    protected abstract void waitOn(Object object, long timeout) throws InterruptedException;

    /**
     * This provider has been replaced; transition to the new one. For example,
     * one might wake up any current sleepers.
     */
    protected abstract void close();
}

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
 * A "normal" implementation of time, which is tied to changes in
 * {@link System#currentTimeMillis()} and {@link System#nanoTime()}.
 *
 * A time may be specified so that transitioning back to real time can be made
 * seamless to the users.
 *
 * @author skeggsc
 */
public final class NormalTime extends Time {

    private final long baseCurrentMillis;
    private final long baseTimeNanos;

    /**
     * Sets up a new NormalTime time provider, with the number of milliseconds
     * that {@link #nowMillis()} should return at this exact moment. A similar
     * effect is established for {@link #nowNanos()}, which would be
     * <code>startTime * 1000000</code> at this exact time. Note, however, that
     * the two are not guaranteed to be synchronized.
     *
     * @param startTime the number of milliseconds that the current time should
     * appear to be.
     */
    public NormalTime(int startTime) {
        // startTime is usually zero, which means that nowMillis() would return
        // zero at first.
        baseCurrentMillis = System.currentTimeMillis() - startTime;
        baseTimeNanos = System.nanoTime() - startTime * 1000000;
    }

    @Override
    protected long nowMillis() {
        return System.currentTimeMillis() - baseCurrentMillis;
    }

    @Override
    protected long nowNanos() {
        return System.nanoTime() - baseTimeNanos;
    }

    @Override
    protected void sleepFor(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @Override
    protected void waitOn(Object object, long timeout) throws InterruptedException {
        object.wait(timeout);
    }

    @Override
    protected void close() {
        // nothing necessary
    }
}

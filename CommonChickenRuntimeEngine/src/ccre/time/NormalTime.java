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

public final class NormalTime extends Time {

    private final long baseCurrentMillis;
    private final long baseTimeNanos;
    private final Scheduler scheduler = new Scheduler(this);

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
    protected void scheduleTimer(long millis, EventOutput update) {
        scheduler.schedule(millis, update);
    }

    @Override
    protected void close() {
        scheduler.close();
    }
}

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

import java.util.PriorityQueue;

import ccre.channel.EventOutput;
import ccre.log.Logger;

/**
 * A "fake" implementation of time, in which the current time is controlled by
 * the {@link #forward(long)} method of this instance.
 *
 * WARNING: this contains complex and likely slightly broken synchronization
 * code. Do not use it in production!
 *
 * A time may be specified so that transitioning between fake time and real time
 * can be made seamless to the users.
 *
 * @author skeggsc
 */
public class FakeTime extends Time {

    private volatile long now = 0;

    /**
     * Fast-forward time by the specified number of milliseconds, including
     * waiting to attempt to synchronize threads.
     *
     * @param millis how far forward to send time.
     * @throws InterruptedException if the thread is interrupted while
     * synchronizing with other threads.
     */
    public void forward(long millis) throws InterruptedException {
        if (closing) {
            throw new IllegalStateException("The FakeTime is closing! Don't try to control it!");
        }
        if (millis <= 0) {
            throw new IllegalArgumentException("FakeTime.forward expects a positive delta!");
        }
        synchronized (this) {
            now += millis;
            this.notifyAll();
        }
        while (true) {
            Entry ent;
            synchronized (this) {
                if (queue.isEmpty() || queue.peek().time > nowNanos()) {
                    break;
                }
                ent = queue.remove();
            }
            try {
                ent.target.event();
            } catch (Throwable thr) {
                Logger.severe("Top-level failure in scheduled event", thr);
            }
        }
    }

    private static class Entry implements Comparable<Entry> {
        public final EventOutput target;
        public final long time;

        public Entry(EventOutput target, long time) {
            this.target = target;
            this.time = time;
        }

        @Override
        public int compareTo(Entry o) {
            return Long.compare(time, o.time);
        }
    }

    private final PriorityQueue<Entry> queue = new PriorityQueue<>(1024);

    void schedule(EventOutput event, long time) {
        synchronized (FakeTime.this) {
            queue.add(new Entry(event, time));
        }
    }

    @Override
    protected synchronized long nowMillis() {
        return now;
    }

    @Override
    protected synchronized long nowNanos() {
        return now * 1000000;
    }

    @Override
    protected synchronized void sleepFor(long millis) throws InterruptedException {
        if (closing) {
            throw new IllegalStateException("This FakeTime instance is shutting down! Don't try to wait on it!");
        }
        long target = now + millis;
        while (now < target) {
            if (closing) {
                throw new RuntimeException("Time provider started closing during sleep!");
            }
            this.wait();
        }
    }

    @Override
    protected void waitOn(Object object, long timeout) throws InterruptedException {
        if (closing) {
            throw new IllegalStateException("This FakeTime instance is shutting down! Don't try to wait on it!");
        }
        if (object == null) {
            throw new NullPointerException();
        }
        if (!Thread.holdsLock(object)) {
            throw new IllegalMonitorStateException("Thread does not hold lock for object!");
        }
        if (timeout == 0) {
            object.wait();
            return;
        } else if (timeout < 0) {
            throw new IllegalArgumentException("Negative wait time!");
        }
        schedule(() -> {
            synchronized (object) {
                object.notifyAll();
            }
        }, timeout);
        // TODO: recomment this
        object.wait(1000);
    }

    private boolean closing = false;

    @Override
    protected void close() {
        synchronized (this) {
            closing = true;
        }
        synchronized (this) {
            now = 0;
            closing = false;
            // just in case something's still waiting on us
            this.notifyAll();
        }
    }
}

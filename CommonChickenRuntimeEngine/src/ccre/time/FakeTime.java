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

import java.util.LinkedList;
import java.util.PriorityQueue;

import ccre.channel.EventOutput;
import ccre.log.Logger;

// NOTE: this contains complex and likely slightly broken synchronization code. do not use it in production!
public class FakeTime extends Time {

    private long now = 0;
    private final LinkedList<Object> otherSleepers = new LinkedList<>();
    private final PriorityQueue<ScheduleEntry> executionTimes = new PriorityQueue<>();

    public void forward(long millis) {
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
            ScheduleEntry entry;
            synchronized (this) {
                entry = executionTimes.poll();
                if (entry == null) {
                    break;
                }
                if (entry.expirationAt > now) {
                    executionTimes.add(entry);
                    break;
                }
            }
            try {
                entry.target.event();
            } catch (Throwable thr) {
                Logger.severe("Schedule entry threw exception!", thr);
            }
        }
        for (Object obj : otherSleepers) {
            synchronized (obj) {
                obj.notifyAll();
            }
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
        // we ignore the actual timeout... we can't tell the difference between an actual notification and a time-update notification!
        // so we just go with the spurious wakeups every time the time changes.
        synchronized (this) {
            otherSleepers.add(object);
        }
        try {
            // we only wait ONCE ... which means that we WILL have spurious wakeups! I _do_ hope that code using this can handle them like it's supposed to.

            // there's no race condition here because the notifying have to synchronize with 'object' to be able to send our own notification, and THIS thread is holding it.
            object.wait(1000);
            // but there is a possible starvation condition, if a later object needs to be notified for the lock on this to be able to be release
            // so we set a timeout on object, which should break the starvation possibilities... eventually. (but long enough away to be noticed by the user.)
            // this would be a bigger issue if it could ever happen in production, so I'm leaving it for now.
            // simply put: NEVER USE FakeTime IN A PRODUCTION SYSTEM!
        } finally {
            synchronized (this) {
                otherSleepers.remove(object);
            }
        }
    }
    
    private boolean closing = false;

    @Override
    protected synchronized void scheduleTimer(long millis, EventOutput update) {
        if (closing) {
            throw new IllegalStateException("This FakeTime instance is shutting down! Don't try to schedule more events!");
        }
        executionTimes.add(new ScheduleEntry(now + millis, update));
    }

    @Override
    protected void close() {
        synchronized (this) {
            closing = true;
            for (Object o : otherSleepers) { // wake up, everyone!
                o.notifyAll();
            }
        }
        while (true) {
            ScheduleEntry ent;
            synchronized (this) {
                ent = executionTimes.poll();
                if (ent == null) {
                    break;
                }
                now = ent.expirationAt;
            }
            ent.target.event();
        }
        synchronized (this) {
            now = 0;
            closing = false;
            this.notifyAll(); // just in case something's still waiting on us
        }
    }
}

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
package ccre.scheduler;

import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import ccre.channel.EventOutput;
import ccre.concurrency.ReporterThread;
import ccre.log.Logger;
import ccre.time.Time;
import ccre.util.ThreadedAllocationPool;

class RunLoop extends ReporterThread implements IRunLoop {

    public RunLoop() {
        super("RunLoop");
        this.setPriority(Thread.MAX_PRIORITY - 1);
    }

    private static class Entry implements Comparable<Entry> {
        public EventOutput target;
        public long time;
        public String tag;

        @Override
        public int compareTo(Entry o) {
            return Long.compare(time, o.time);
        }

        public Entry populate(String tag, EventOutput target, long time) {
            this.tag = tag;
            this.time = time;
            this.target = target;
            return this;
        }
    }

    private final ReentrantLock queueLock = new ReentrantLock();
    private final Condition update = queueLock.newCondition();
    private final PriorityQueue<Entry> queue = new PriorityQueue<>(1024);
    private final ThreadedAllocationPool<Entry> pool = new ThreadedAllocationPool<>(1024, Entry::new);
    private volatile boolean terminated;

    @Override
    public void add(String tag, EventOutput event, long time) {
        Entry ent = pool.allocate().populate(tag, event, time);
        queueLock.lock();
        try {
            queue.add(ent);
            update.signalAll();
        } finally {
            queueLock.unlock();
        }
    }

    @Override
    protected void threadBody() {
        try {
            queueLock.lockInterruptibly();
            try {
                while (!terminated) {
                    // loop until we have something ready to run
                    Entry ent = queue.peek();
                    long now = Time.currentTimeNanos();
                    if (ent == null) {
                        // just wait until we actually HAVE something
                        reportAwaiting(true);
                        // TODO: what are the guarantees if this throws an
                        // exception?
                        update.await();
                        reportAwaiting(false);
                    } else if (ent.time > now) {
                        // not yet time to run
                        reportAwaiting(true);
                        update.awaitNanos(ent.time - now);
                        reportAwaiting(false);
                    } else {
                        // ready to run an event!
                        ent = queue.remove();
                        // unlock while we process an event, so that we don't
                        // block
                        // any queue insertions.
                        queueLock.unlock();
                        reportActive(ent.tag);

                        // extract target, then free
                        EventOutput target = ent.target;
                        ent.target = null; // avoid garbage linger
                        pool.free(ent);

                        // actually run the event
                        try {
                            target.event();
                        } catch (Throwable thr) {
                            Logger.severe("Top-level failure in scheduled event", thr);
                        }

                        // back into the monitor loop
                        reportActive(null);
                        queueLock.lockInterruptibly();
                    }
                }
            } finally {
                if (queueLock.isHeldByCurrentThread()) {
                    queueLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            // interrupted!
        }
    }

    // TODO: These MUST never block! RecordedRunLoop might do it for a small
    // amount of time while waiting for a queue to be unlocked, but that's it.
    // Really, even that should be avoided, but as long as it's just a delay,
    // it'll be okay from a correctness perspective.
    protected void reportAwaiting(boolean isAwaiting) {
        // to be overridden as necessary
    }

    protected void reportActive(String tag) {
        // to be overridden as necessary
    }

    @Override
    public void terminate() {
        terminated = true;
        this.interrupt();
    }
}

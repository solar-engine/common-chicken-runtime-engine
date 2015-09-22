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

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventOutput;
import ccre.concurrency.ReporterThread;

public class Scheduler {
    private static Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private static final long MAXIMUM_REASONABLE_DELAY = 3;// warn if more than
                                                           // 3 milliseconds are
                                                           // spent handling
                                                           // anything
    private final Time provider;
    private final ReporterThread scheduler = new ReporterThread("Scheduler") {
        @Override
        protected void threadBody() throws InterruptedException {
            body();
        }
    };
    private boolean stop = false;

    private final PriorityQueue<ScheduleEntry> entries = new PriorityQueue<>();

    private synchronized void body() throws InterruptedException {
        try {
            while (!stop) {
                ScheduleEntry ent = entries.poll();
                long now = provider.nowMillis();
                if (ent == null) {
                    this.wait();
                    continue;
                }
                long remaining = ent.expirationAt - now;
                if (remaining > 0) {
                    entries.add(ent);
                    provider.waitOn(this, remaining);
                    continue;
                }
                try {
                    ent.target.event();
                } catch (Throwable throwable) {
                    logger.error("Scheduler target threw an exception!", throwable);
                }
                long delta = provider.nowMillis() - now;
                if (delta > MAXIMUM_REASONABLE_DELAY) {
                    logger.warn("[LOCAL] Event exceeded maximum reasonable delay: " + ent + " for " + delta + " millis.");
                }
            }
        } catch (InterruptedException ex) {
            if (!stop) {// if we're SUPPOSED to stop... then let's just stop,
                        // quietly. but otherwise, it's an issue and we should
                        // be LOUD!
                throw ex;
            }
        }
    }

    public Scheduler() {
        this(Time.getTimeProvider());
    }

    public Scheduler(Time provider) {
        this.provider = provider;
    }

    public synchronized void schedule(long millis, EventOutput update) {
        if (!scheduler.isAlive()) {
            scheduler.start();
        }
        entries.add(new ScheduleEntry(provider.nowMillis() + millis, update));
        this.notifyAll();
    }

    public synchronized void close() {
        stop = true;
        scheduler.interrupt();
    }
}

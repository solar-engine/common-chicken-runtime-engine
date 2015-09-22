/*
 * Copyright 2013-2015 Colby Skeggs and Vincent Miller
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.concurrency.ReporterThread;
import ccre.time.Time;

/**
 * An EventInput that will fire the event in all its consumers at a specified
 * interval.
 *
 * @author MillerV, SkeggsC
 */
public final class Ticker implements EventInput {
    private static Logger logger = LoggerFactory.getLogger(Ticker.class);

    private final EventStatus producer = new EventStatus();
    private final ReporterThread main;
    private boolean isKilled = false;
    private final Object lock = new Object();

    /**
     * Create a new Ticker with the specified interval. The timer will start
     * immediately, executing for the first time after the specified interval.
     *
     * This will not run at a fixed rate, as extra time taken for one cycle will
     * not be corrected for in the time between the cycles.
     *
     * @param interval The desired interval, in milliseconds.
     */
    public Ticker(int interval) {
        this(interval, false);
    }

    /**
     * Create a new Ticker with the specified interval and fixed rate option.
     * The timer will start immediately, executing for the first time after the
     * specified interval.
     *
     * If fixedRate is false, this will not run at a fixed rate, as extra time
     * taken for one cycle will not be corrected for in the time between the
     * cycles.
     *
     * If fixedRate is true, this will run at a fixed rate, as extra time taken
     * for one cycle will be removed from the time before the subsequent cycle.
     * This does mean that if a cycle takes too long, that produces of the event
     * can bunch up and execute a number of times back-to-back.
     *
     * @param interval The desired interval, in milliseconds.
     * @param fixedRate Should the rate be corrected?
     */
    public Ticker(final int interval, final boolean fixedRate) {
        this.main = new MainTickerThread((fixedRate ? "FixedTicker-" : "Ticker-") + interval, fixedRate, interval);
    }

    /**
     * Destroys this Ticker. It won't function after this.
     */
    public void terminate() {
        isKilled = true;
        producer.__UNSAFE_clearListeners();
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private class MainTickerThread extends ReporterThread {

        private final boolean fixedRate;
        private final int interval;
        private int countFails = 0;

        MainTickerThread(String name, boolean fixedRate, int interval) {
            super(name);
            this.fixedRate = fixedRate;
            this.interval = interval;
        }

        @Override
        protected void threadBody() throws InterruptedException {
            if (fixedRate) {
                long next = Time.currentTimeMillis() + interval;
                synchronized (lock) {
                    lock.notifyAll(); // tell the main thread that we've started
                }
                while (true) {
                    synchronized (lock) {
                        if (isKilled) {
                            break;
                        }
                        long rem = next - Time.currentTimeMillis();
                        if (rem > 0) {
                            Time.wait(lock, rem);
                            continue;
                        }
                        if (isKilled) {
                            break;
                        }
                    }
                    cycle();
                    next += interval;
                }
            } else {
                long lastTime = Time.currentTimeMillis();
                synchronized (lock) {
                    lock.notifyAll(); // tell the main thread that we've started
                }
                while (true) {
                    long doneAt = lastTime + interval;
                    synchronized (lock) {
                        while (!isKilled && Time.currentTimeMillis() < doneAt) {
                            Time.wait(lock, interval);
                        }
                        if (isKilled) {
                            break;
                        }
                    }
                    cycle();
                    lastTime = Time.currentTimeMillis();
                }
            }
        }

        private void cycle() {
            try {
                if (countFails >= 50) {
                    countFails--;
                    if (producer.produceWithFailureRecovery()) {
                        countFails = 0;
                    }
                } else {
                    producer.produce();
                    if (countFails > 0) {
                        countFails--;
                    }
                }
            } catch (Throwable thr) {
                logger.error("Exception in Ticker main loop!", thr);
                countFails += 10;
            }
        }
    }

    @Override
    public void onUpdate(EventOutput notify) {
        if (isKilled) {
            throw new IllegalStateException("Terminated!");
        }
        if (!main.isAlive()) {
            start();
        }
        producer.onUpdate(notify);
    }

    private void start() {
        synchronized (lock) {
            main.start();
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public EventOutput onUpdateR(EventOutput notify) {
        if (isKilled) {
            throw new IllegalStateException("Terminated!");
        }
        if (!main.isAlive()) {
            start();
        }
        return producer.onUpdateR(notify);
    }
}

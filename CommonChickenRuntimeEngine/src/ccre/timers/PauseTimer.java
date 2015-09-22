/*
 * Copyright 2014-2015 Colby Skeggs
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

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.concurrency.ReporterThread;
import ccre.time.Time;

/**
 * A PauseTimer has a boolean state for running or not, which is readable but
 * not directly writable. It can be started by an event, and runs until the end
 * of its time period, at which point it turns off. If the event occurs during
 * this time, the timer gets reset to the start of the duration.
 *
 * Reading a PauseTimer returns TRUE if it is running and FALSE if it is not.
 *
 * @author skeggsc
 */
public class PauseTimer implements BooleanInput, EventOutput {
    private static Logger logger = LoggerFactory.getLogger(PauseTimer.class);

    private volatile long endAt;
    private final long timeout;
    private final Object lock = new Object();
    private final CopyOnWriteArrayList<EventOutput> consumers = new CopyOnWriteArrayList<EventOutput>();
    private boolean isRunning = true;
    private final ReporterThread main = new ReporterThread("PauseTimer") {
        @Override
        protected void threadBody() throws InterruptedException {
            synchronized (lock) {
                lock.notifyAll();
            }
            while (true) {
                synchronized (lock) {
                    while (isRunning && endAt == 0) {
                        lock.wait();
                    }
                    if (!isRunning) {
                        break;
                    }
                }
                long now;
                while ((now = Time.currentTimeMillis()) < endAt) {
                    synchronized (lock) {
                        Time.wait(lock, endAt - now);
                    }
                }
                setEndAt(0);
            }
        }
    };

    /**
     * Create a new PauseTimer with the specified timeout in milliseconds.
     *
     * @param timeout The timeout for each time the timer is activated.
     */
    public PauseTimer(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("PauseTimer must have a positive timeout!");
        }
        this.timeout = timeout;
    }

    /**
     * Terminate the timer and its thread. It will not function after this point
     * - do not attempt to use it.
     */
    public void terminate() {
        isRunning = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * Start the timer running.
     */
    public void event() {
        setEndAt(Time.currentTimeMillis() + timeout);
    }

    public boolean get() {
        return endAt != 0;
    }

    private void setEndAt(long endAt) {
        long old;
        synchronized (lock) {
            old = this.endAt;
            this.endAt = endAt;
            lock.notifyAll();
        }
        boolean enabling = endAt != 0;
        if (enabling && !main.isAlive()) {
            synchronized (lock) {
                main.start();
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        boolean disabled = old == 0;
        if (disabled == !enabling) {
            return;
        }
        for (EventOutput c : consumers) {
            try {
                c.event();
            } catch (Throwable thr) {
                logger.error("Exception in PauseTimer dispatch!", thr);
            }
        }
    }

    /**
     * When this timer stops running, trigger the specified EventOutput.
     *
     * @param trigger The EventOutput to trigger.
     */
    public void triggerAtEnd(EventOutput trigger) {
        send(BooleanOutput.onChange(trigger, null));
    }

    /**
     * When this timer starts running, trigger the specified EventOutput.
     *
     * @param trigger The EventOutput to trigger.
     */
    public void triggerAtStart(EventOutput trigger) {
        send(BooleanOutput.onChange(null, trigger));
    }

    /**
     * When this timer starts or stops running, trigger the specified
     * EventOutputs.
     *
     * @param start The EventOutput to trigger when the timer starts.
     * @param end The EventOutput to trigger when the timer ends.
     */
    public void triggerAtChanges(EventOutput start, EventOutput end) {
        send(BooleanOutput.onChange(end, start));
    }

    public void onUpdate(EventOutput notify) {
        consumers.add(notify);
    }

    @Override
    public EventOutput onUpdateR(EventOutput notify) {
        consumers.add(notify);
        return () -> consumers.remove(notify);
    }
}

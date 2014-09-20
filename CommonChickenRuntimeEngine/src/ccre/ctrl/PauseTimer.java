/*
 * Copyright 2014 Colby Skeggs
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
package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.concurrency.ConcurrentDispatchArray;
import ccre.concurrency.ReporterThread;
import ccre.log.Logger;

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

    private volatile long endAt;
    private final long timeout;
    private final Object lock = new Object();
    private final ConcurrentDispatchArray<BooleanOutput> consumers = new ConcurrentDispatchArray<BooleanOutput>();
    private final ReporterThread main = new ReporterThread("PauseTimer") {
        @Override
        protected void threadBody() throws InterruptedException {
            while (true) {
                synchronized (lock) {
                    while (endAt == 0) {
                        lock.wait();
                    }
                }
                long now;
                while ((now = System.currentTimeMillis()) < endAt) {
                    Thread.sleep(endAt - now);
                }
                try {
                    setEndAt(0);
                } catch (Throwable thr) {
                    Logger.severe("Exception in PauseTimer main loop!", thr);
                }
            }
        }
    };

    /**
     * Create a new PauseTimer with the specified timeout in milliseconds.
     *
     * @param timeout The timeout for each time the timer is activated.
     */
    public PauseTimer(long timeout) {
        this.timeout = timeout;
    }

    public void event() {
        setEndAt(System.currentTimeMillis() + timeout);
    }

    public boolean get() {
        return endAt != 0;
    }

    private void setEndAt(long endAt) {
        long old;
        boolean enabling = endAt != 0;
        if (enabling && !main.isAlive()) {
            main.start();
        }
        synchronized (lock) {
            old = this.endAt;
            this.endAt = endAt;
            lock.notifyAll();
        }
        boolean disabled = old == 0;
        if (disabled == !enabling) {
            return;
        }
        for (BooleanOutput c : consumers) {
            c.set(enabling);
        }
    }

    public void send(BooleanOutput output) {
        consumers.add(output);
    }

    public void unsend(BooleanOutput output) {
        consumers.remove(output);
    }

    /**
     * When this timer stops running, trigger the specified EventOutput.
     *
     * @param trigger The EventOutput to trigger.
     */
    public void triggerAtEnd(EventOutput trigger) {
        send(BooleanMixing.triggerWhenBooleanChanges(trigger, null));
    }

    /**
     * When this timer starts running, trigger the specified EventOutput.
     *
     * @param trigger The EventOutput to trigger.
     */
    public void triggerAtStart(EventOutput trigger) {
        send(BooleanMixing.triggerWhenBooleanChanges(null, trigger));
    }

    /**
     * When this timer starts or stops running, trigger the specified
     * EventOutputs.
     *
     * @param start The EventOutput to trigger when the timer starts.
     * @param end The EventOutput to trigger when the timer ends.
     */
    public void triggerAtChanges(EventOutput start, EventOutput end) {
        send(BooleanMixing.triggerWhenBooleanChanges(end, start));
    }
}

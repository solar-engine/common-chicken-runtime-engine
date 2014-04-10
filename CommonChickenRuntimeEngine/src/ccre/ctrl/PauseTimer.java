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

import ccre.chan.BooleanInput;
import ccre.chan.BooleanOutput;
import ccre.concurrency.ConcurrentDispatchArray;
import ccre.concurrency.ReporterThread;
import ccre.event.EventConsumer;
import ccre.log.LogLevel;
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
public class PauseTimer implements BooleanInput, EventConsumer {

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
                    Logger.log(LogLevel.SEVERE, "Exception in PauseTimer main loop!", thr);
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
        main.start();
    }

    public void eventFired() {
        setEndAt(System.currentTimeMillis() + timeout);
    }

    public boolean readValue() {
        return endAt != 0;
    }

    private void setEndAt(long endAt) {
        long old;
        synchronized (lock) {
            old = this.endAt;
            this.endAt = endAt;
            lock.notifyAll();
        }
        if ((endAt == 0) != (old == 0)) {
            for (BooleanOutput c : consumers) {
                c.writeValue(endAt != 0);
            }
        }
    }

    public void addTarget(BooleanOutput output) {
        consumers.add(output);
    }

    public boolean removeTarget(BooleanOutput output) {
        return consumers.remove(output);
    }
}

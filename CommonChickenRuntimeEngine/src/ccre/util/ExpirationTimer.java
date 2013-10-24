/*
 * Copyright 2013 Colby Skeggs
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
package ccre.util;

import ccre.chan.BooleanOutput;
import ccre.concurrency.ReporterThread;
import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.Logger;

/**
 * An ExpirationTimer acts sort of like an alarm clock. You can schedule a
 * series of alarms with certain delays, and then start the timer. When each
 * delay passes, the timer will trigger the event associated with the delay. The
 * timer can be fed, which restarts the timer (can be used to implement
 * WatchDogs, hence the name). The timer can also be stopped, which resets the
 * timer and prevents it from running until it is started again.
 *
 * @author skeggsc
 */
public class ExpirationTimer { // TODO: Needs to be tested!

    /**
     * A task that is scheduled for a specific delay after the timer starts.
     */
    protected class Task {

        /**
         * The delay before the event is fired.
         */
        public final long delay;
        /**
         * The event to fire.
         */
        public final EventConsumer cnsm;

        Task(long delay, EventConsumer cnsm) {
            this.delay = delay;
            this.cnsm = cnsm;
        }
    }
    /**
     * The list of tasks, sorted in order with the first task (shortest delay)
     * first.
     */
    protected final CArrayList<Task> tasks = new CArrayList<Task>();
    /**
     * Is this timer running?
     */
    protected boolean isStarted = false;
    /**
     * When did this timer get started? Delays get computer from this point.
     */
    protected long startedAt;
    protected final ReporterThread rthr = new ReporterThread("ExpirationTimer") {
        @Override
        protected void threadBody() throws Throwable {
            body();
        }
    };

    /**
     * Schedule an EventConsumer to be triggered at a specific delay.
     *
     * @param delay the delay (in milliseconds) to trigger at.
     * @param cnsm the event to fire.
     * @throws IllegalStateException if the timer is already running.
     */
    public synchronized void schedule(long delay, EventConsumer cnsm) throws IllegalStateException {
        if (isStarted) {
            throw new IllegalStateException("Timer is running!");
        }
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).delay > delay) {
                tasks.add(i, new Task(delay, cnsm));
                return;
            }
        }
        tasks.add(new Task(delay, cnsm));
    }

    /**
     * Return an event that will be triggered at the specified delay.
     *
     * @param delay the delay (in milliseconds) to trigger at.
     * @return the event that will be fired.
     * @throws IllegalStateException if the timer is already running.
     */
    public synchronized EventSource schedule(long delay) throws IllegalStateException {
        Event evt = new Event();
        schedule(delay, evt);
        return evt;
    }

    /**
     * Start the timer running.
     *
     * @throws IllegalStateException if the timer was already running.
     */
    public synchronized void start() throws IllegalStateException {
        if (isStarted) {
            throw new IllegalStateException("Timer is running!");
        }
        isStarted = true;
        if (!rthr.isAlive()) {
            rthr.start();
        }
        feed();
    }

    private synchronized void body() {
        while (true) {
            try {
                while (!isStarted) {
                    wait();
                }
                long startAt = startedAt;
                for (Task t : tasks) {
                    long rel = startAt + t.delay - System.currentTimeMillis();
                    while (rel > 0) {
                        wait(rel);
                        rel = startAt + t.delay - System.currentTimeMillis();
                    }
                    t.cnsm.eventFired();
                }
                while (isStarted) { // Once finished, wait to stop before restarting.
                    wait();
                }
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Reset the timer. This will act as if the timer had just been started, in
     * terms of which events are fired when.
     *
     * @throws IllegalStateException if the timer was not started.
     */
    public synchronized void feed() throws IllegalStateException {
        if (!isStarted) {
            throw new IllegalStateException("Timer is not running!");
        }
        startedAt = System.currentTimeMillis();
        rthr.interrupt();
    }

    /**
     * Stop the timer. This will prevent the timer from running until start is
     * called again.
     *
     * @throws IllegalStateException if the timer was not started.
     */
    public synchronized void stop() throws IllegalStateException {
        if (!isStarted) {
            throw new IllegalStateException("Timer is not running!");
        }
        isStarted = false;
        rthr.interrupt();
    }
    /**
     * The cached value for getStartEvent()
     */
    protected EventConsumer startEvt;
    /**
     * The cached value for getFeedEvent()
     */
    protected EventConsumer feedEvt;
    /**
     * The cached value for getStopEvent()
     */
    protected EventConsumer stopEvt;

    /**
     * Get an event that, when fired, will start the timer. This will not throw
     * an IllegalStateException if the timer is already running, although it
     * will log a warning.
     *
     * @return the event to start the timer.
     */
    public EventConsumer getStartEvent() {
        if (startEvt == null) {
            startEvt = new EventConsumer() {
                public void eventFired() {
                    if (isStarted) {
                        Logger.warning("ExpirationTimer already started!");
                    } else {
                        start();
                    }
                }
            };
        }
        return startEvt;
    }

    /**
     * Get an event that, when fired, will feed the timer. This will not throw
     * an IllegalStateException if the timer is not running, although it will
     * log a warning.
     *
     * @return the event to feed the timer.
     */
    public EventConsumer getFeedEvent() {
        if (feedEvt == null) {
            feedEvt = new EventConsumer() {
                public void eventFired() {
                    if (!isStarted) {
                        Logger.warning("ExpirationTimer not started!");
                    } else {
                        feed();
                    }
                }
            };
        }
        return feedEvt;
    }

    /**
     * Get an event that, when fired, will stop the timer. This will not throw
     * an IllegalStateException if the timer is not running, although it will
     * log a warning.
     *
     * @return the event to stop the timer.
     */
    public EventConsumer getStopEvent() {
        if (stopEvt == null) {
            stopEvt = new EventConsumer() {
                public void eventFired() {
                    if (!isStarted) {
                        Logger.warning("ExpirationTimer not started!");
                    } else {
                        stop();
                    }
                }
            };
        }
        return stopEvt;
    }

    /**
     * When the specified event occurs, start the timer. See getStartEvent() for
     * details.
     *
     * @param src
     * @see #getStartEvent()
     */
    public void startWhen(EventSource src) {
        src.addListener(getStartEvent());
    }

    /**
     * When the specified event occurs, feed the timer. See getFeedEvent() for
     * details.
     *
     * @param src
     * @see #getFeedEvent()
     */
    public void feedWhen(EventSource src) {
        src.addListener(getFeedEvent());
    }

    /**
     * When the specified event occurs, stop the timer. See getStopEvent() for
     * details.
     *
     * @param src
     * @see #getStopEvent()
     */
    public void stopWhen(EventSource src) {
        src.addListener(getStopEvent());
    }

    /**
     * Get a BooleanOutput that can be written to in order to control whether or
     * not this timer is running. This will start or stop the timer when the
     * outputted value changes. This will not throw an IllegalStateException if
     * the timer is in the wrong state or log a warning.
     *
     * @return a BooleanOutput to control the ExpirationTimer.
     */
    public BooleanOutput getRunningControl() {
        return new BooleanOutput() {
            public void writeValue(boolean value) {
                if (value) {
                    if (!isStarted) {
                        start();
                    }
                } else {
                    if (isStarted) {
                        stop();
                    }
                }
            }
        };
    }
}

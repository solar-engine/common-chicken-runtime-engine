/*
 * Copyright 2013-2015 Colby Skeggs
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

import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.concurrency.ReporterThread;
import ccre.time.Time;

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
public final class ExpirationTimer {
    private static Logger logger = LoggerFactory.getLogger(ExpirationTimer.class);

    /**
     * The list of tasks, sorted in order with the first task (shortest delay)
     * first.
     */
    private final ArrayList<Task> tasks = new ArrayList<Task>();
    /**
     * Is this timer running?
     */
    private final BooleanStatus isStarted = new BooleanStatus();
    /**
     * When did this timer get started? Delays get computer from this point.
     */
    private long startedAt;
    /**
     * The main thread of the Expiration Timer.
     */
    private final ReporterThread main = new ReporterThread("ExpirationTimer") {
        @Override
        protected void threadBody() throws Throwable {
            body();
        }
    };
    /**
     * Whether or not this thread has been told to terminate.
     */
    private boolean terminated = false;

    /**
     * Schedule an EventOutput to be triggered at a specific delay.
     *
     * @param delay the delay (in milliseconds) to trigger at.
     * @param cnsm the event to fire.
     * @throws IllegalStateException if the timer is already running.
     */
    public synchronized void schedule(long delay, EventOutput cnsm) throws IllegalStateException {
        if (isStarted.get()) {
            throw new IllegalStateException("Timer is running!");
        }
        tasks.add(new Task(delay, cnsm));
    }

    /**
     * Schedule an EventOutput to be triggered at a dynamic delay.
     *
     * @param delay the dynamic delay (in seconds) to trigger at.
     * @param cnsm the event to fire.
     * @throws IllegalStateException if the timer is already running.
     */
    public synchronized void schedule(FloatInput delay, EventOutput cnsm) throws IllegalStateException {
        if (isStarted.get()) {
            throw new IllegalStateException("Timer is running!");
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
    public EventInput schedule(long delay) throws IllegalStateException {
        EventStatus evt = new EventStatus();
        schedule(delay, evt);
        return evt;
    }

    /**
     * Return an event that will be triggered at a dynamic delay.
     *
     * @param delay the dynamic delay (in second) to trigger at.
     * @return the event that will be fired.
     * @throws IllegalStateException if the timer is already running.
     */
    public EventInput schedule(FloatInput delay) throws IllegalStateException {
        EventStatus evt = new EventStatus();
        schedule(delay, evt);
        return evt;
    }

    /**
     * Start the timer running.
     *
     * @throws IllegalStateException if the timer was already running.
     */
    public synchronized void start() throws IllegalStateException {
        if (isStarted.get()) {
            throw new IllegalStateException("Timer is running!");
        }
        isStarted.set(true);
        if (!main.isAlive()) {
            main.start();
        }
        feed();
    }

    /**
     * Start or restart the timer running.
     */
    public synchronized void startOrFeed() {
        if (isStarted.get()) {
            feed();
        } else {
            start();
        }
    }

    private synchronized void recalculateTasks() {
        for (Task t : tasks) {
            t.recalculate();
        }
        Collections.sort(tasks);
    }

    private synchronized void runTasks(long startAt) throws InterruptedException {
        for (Task t : tasks) {
            long rel = startAt + t.delay - Time.currentTimeMillis();
            while (rel > 0) {
                Time.wait(this, rel);
                rel = startAt + t.delay - Time.currentTimeMillis();
            }
            try {
                t.cnsm.event();
            } catch (Throwable thr) {
                logger.error("Exception in ExpirationTimer dispatch!", thr);
            }
        }
    }

    private synchronized void body() {
        while (!terminated) {
            try {
                while (!isStarted.get()) {
                    this.wait();
                }
                if (terminated) {
                    break;
                }
                recalculateTasks();
                long startAt = startedAt;
                runTasks(startAt);
                while (isStarted.get() && !terminated && startAt == startedAt) {// Once
                                                                                // finished,
                                                                                // wait
                                                                                // to
                                                                                // stop
                                                                                // before
                                                                                // restarting.
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        // this is actually the expected way of notifying this
                        // thread
                    }
                }
            } catch (InterruptedException ex) {
                // this is actually the expected way of notifying this thread
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
        if (!isStarted.get()) {
            throw new IllegalStateException("Timer is not running!");
        }
        startedAt = Time.currentTimeMillis();
        main.interrupt();
    }

    /**
     * Stop the timer. This will prevent the timer from running until start is
     * called again.
     *
     * @throws IllegalStateException if the timer was not started.
     */
    public synchronized void stop() throws IllegalStateException {
        if (!isStarted.get()) {
            throw new IllegalStateException("Timer is not running!");
        }
        isStarted.set(false);
        main.interrupt();
    }

    /**
     * Get an event that, when fired, will start the timer. This will not throw
     * an IllegalStateException if the timer is already running.
     *
     * @return the event to start the timer.
     */
    public EventOutput getStartEvent() {
        return () -> {
            if (!isStarted.get()) {
                start();
            }
        };
    }

    /**
     * Get an event that, when fired, will start or feed the timer, like
     * startOrFeed().
     *
     * @return the event to start or feed the timer.
     */
    public EventOutput getStartOrFeedEvent() {
        return () -> startOrFeed();
    }

    /**
     * Get an event that, when fired, will feed the timer. This will not throw
     * an IllegalStateException if the timer is not running.
     *
     * @return the event to feed the timer.
     */
    public EventOutput getFeedEvent() {
        return () -> {
            if (isStarted.get()) {
                feed();
            }
        };
    }

    /**
     * Get an event that, when fired, will stop the timer. This will not throw
     * an IllegalStateException if the timer is not running.
     *
     * @return the event to stop the timer.
     */
    public EventOutput getStopEvent() {
        return () -> {
            if (isStarted.get()) {
                stop();
            }
        };
    }

    /**
     * When the specified event occurs, start the timer. See getStartEvent() for
     * details.
     *
     * @param src When to start the timer.
     * @see #getStartEvent()
     */
    public void startWhen(EventInput src) {
        src.send(getStartEvent());
    }

    /**
     * When the specified event occurs, feed the timer. See getFeedEvent() for
     * details.
     *
     * @param src When to feed the timer.
     * @see #getFeedEvent()
     */
    public void feedWhen(EventInput src) {
        src.send(getFeedEvent());
    }

    /**
     * When the specified event occurs, start or feed the timer. See
     * getStartOrFeedEvent() for details.
     *
     * @param src When to start or feed the timer.
     * @see #getStartOrFeedEvent()
     */
    public void startOrFeedWhen(EventInput src) {
        src.send(getStartOrFeedEvent());
    }

    /**
     * When the specified event occurs, stop the timer. See getStopEvent() for
     * details.
     *
     * @param src When to stop the timer.
     * @see #getStopEvent()
     */
    public void stopWhen(EventInput src) {
        src.send(getStopEvent());
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
        return value -> {
            if (value) {
                if (!isStarted.get()) {
                    start();
                }
            } else {
                if (isStarted.get()) {
                    stop();
                }
            }
        };
    }

    /**
     * Get a BooleanInput representing whether or not the timer is running.
     *
     * @return an input representing if the timer is running.
     */
    public BooleanInput getRunningStatus() {
        return isStarted;
    }

    /**
     * Check if the timer is running.
     *
     * @return if the timer is running.
     */
    public boolean isRunning() {
        return isStarted.get();
    }

    /**
     * A task that is scheduled for a specific delay after the timer starts.
     */
    private static class Task implements Comparable<Task> {

        /**
         * The delay before the event is fired.
         */
        public long delay;
        /**
         * The event to fire.
         */
        public final EventOutput cnsm;
        /**
         * The source of tuning for the delay.
         */
        public final FloatInput tuning;

        /**
         * Create a new task with a hard-coded delay.
         *
         * @param delay The delay after which the task is fired, in
         * milliseconds.
         * @param cnsm The EventOutput fired by this Task.
         */
        Task(long delay, EventOutput cnsm) {
            this.delay = delay;
            this.cnsm = cnsm;
            this.tuning = null;
        }

        /**
         * Create a new task with a tunable delay.
         *
         * @param delay The delay after which the task is fired, in seconds.
         * @param cnsm The EventOutput fired by this Task.
         */
        Task(FloatInput delay, EventOutput cnsm) {
            this.cnsm = cnsm;
            this.tuning = delay;
            recalculate();
        }

        public void recalculate() {
            if (tuning != null) {
                this.delay = (long) (tuning.get() * 1000);
            }
        }

        public int compareTo(Task o) {
            return Long.compare(delay, o.delay);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Task && delay == ((Task) obj).delay;
        }

        @Override
        public int hashCode() {
            return (int) (delay ^ (delay >> 32));
        }
    }

    /**
     * End the ExpirationTimer's thread as soon as possible.
     */
    public synchronized void terminate() {
        terminated = true;
        if (isRunning()) {
            stop();
        }
        main.interrupt();
    }
}

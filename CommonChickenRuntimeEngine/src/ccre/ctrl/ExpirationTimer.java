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
package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.concurrency.ReporterThread;
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;

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

    /**
     * The list of tasks, sorted in order with the first task (shortest delay)
     * first.
     */
    private final CArrayList<Task> tasks = new CArrayList<Task>();
    /**
     * Is this timer running?
     */
    private final BooleanStatus isStarted = new BooleanStatus();
    /**
     * When did this timer get started? Delays get computer from this point.
     */
    private long startedAt;
    /**
     * The synchronization object for this class.
     */
    private final Object synch = new Object();
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
     * The cached value for getStartEvent()
     */
    private EventOutput startEvt;
    /**
     * The cached value for getStartOrFeedEvent()
     */
    private EventOutput startOrFeedEvt;
    /**
     * The cached value for getFeedEvent()
     */
    private EventOutput feedEvt;
    /**
     * The cached value for getStopEvent()
     */
    private EventOutput stopEvt;
    /**
     * Whether or not this thread has been told to terminate.
     */
    private boolean terminated = false;

    /**
     * Schedule a BooleanOutput to be set to a specified value at a specific
     * delay.
     *
     * @param delay the delay (in milliseconds) to trigger at.
     * @param out the BooleanOutput to modify.
     * @param value the value to modify it to.
     * @throws IllegalStateException if the timer is already running.
     */
    public void scheduleSet(long delay, BooleanOutput out, boolean value) throws IllegalStateException {
        schedule(delay, out.getSetEvent(value));
    }

    /**
     * Schedule a BooleanOutput to be set to true at a specific delay.
     *
     * @param delay the delay (in milliseconds) to trigger at.
     * @param out the BooleanOutput to modify.
     * @throws IllegalStateException if the timer is already running.
     */
    public void scheduleEnable(long delay, BooleanOutput out) throws IllegalStateException {
        scheduleSet(delay, out, true);
    }

    /**
     * Schedule a BooleanOutput to be set to false at a specific delay.
     *
     * @param delay the delay (in milliseconds) to trigger at.
     * @param out the BooleanOutput to modify.
     * @throws IllegalStateException if the timer is already running.
     */
    public void scheduleDisable(long delay, BooleanOutput out) throws IllegalStateException {
        scheduleSet(delay, out, false);
    }

    /**
     * Schedule a BooleanOutput to be set to a specific value during the
     * specified range of times, and then set to the inversion of the value
     * afterwards.
     *
     * @param start the beginning of the period.
     * @param stop the end of the period.
     * @param out the BooleanOutput to modify.
     * @param setToDuring the value to set to during the period.
     * @throws IllegalStateException if the timer is already running.
     */
    public void scheduleBooleanPeriod(long start, long stop, BooleanOutput out, boolean setToDuring) throws IllegalStateException {
        scheduleSet(start, out, setToDuring);
        scheduleSet(stop, out, !setToDuring);
    }

    /**
     * Schedule a BooleanOutput to go through a series of changes over the
     * course of the timer's execution. The output will be set to the specified
     * boolean at the start, and then toggled at each specified time after that.
     *
     * @param control the BooleanOutput to modify.
     * @param beginWith the boolean to begin with.
     * @param beginAt when to begin.
     * @param additionalToggles when each subsequent toggle should occur. (each
     * element should be larger than the previous, but this is not checked.)
     * @throws IllegalStateException if the timer is already running.
     */
    public void scheduleToggleSequence(BooleanOutput control, boolean beginWith, long beginAt, long... additionalToggles) throws IllegalStateException {
        scheduleSet(beginAt, control, beginWith);
        boolean stateToSet = beginWith;
        for (long cur : additionalToggles) {
            stateToSet = !stateToSet;
            scheduleSet(cur, control, stateToSet);
        }
    }

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
        CArrayUtils.sort(tasks);
    }

    private synchronized void runTasks() throws InterruptedException {
        long startAt = startedAt;
        for (Task t : tasks) {
            long rel = startAt + t.delay - System.currentTimeMillis();
            while (rel > 0) {
                synch.wait(rel);
                rel = startAt + t.delay - System.currentTimeMillis();
            }
            try {
                t.cnsm.event();
            } catch (Throwable thr) {
                Logger.severe("Exception in ExpirationTimer dispatch!", thr);
            }
        }
    }

    private synchronized void body() {
        while (!terminated) {
            try {
                while (!isStarted.get()) {
                    synch.wait();
                }
                recalculateTasks();
                runTasks();
                while (isStarted.get()) {// Once finished, wait to stop before restarting.
                    synch.wait();
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
        if (!isStarted.get()) {
            throw new IllegalStateException("Timer is not running!");
        }
        startedAt = System.currentTimeMillis();
        synch.notifyAll();
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
        synch.notifyAll();
        main.interrupt();
    }

    /**
     * Get an event that, when fired, will start the timer. This will not throw
     * an IllegalStateException if the timer is already running.
     *
     * @return the event to start the timer.
     */
    public EventOutput getStartEvent() {
        if (startEvt == null) {
            startEvt = new EventOutput() {
                public void event() {
                    if (!isStarted.get()) {
                        start();
                    }
                }
            };
        }
        return startEvt;
    }

    /**
     * Get an event that, when fired, will start or feed the timer, like
     * startOrFeed().
     *
     * @return the event to start or feed the timer.
     */
    public EventOutput getStartOrFeedEvent() {
        if (startOrFeedEvt == null) {
            startOrFeedEvt = new EventOutput() {
                public void event() {
                    startOrFeed();
                }
            };
        }
        return startOrFeedEvt;
    }

    /**
     * Get an event that, when fired, will feed the timer. This will not throw
     * an IllegalStateException if the timer is not running.
     *
     * @return the event to feed the timer.
     */
    public EventOutput getFeedEvent() {
        if (feedEvt == null) {
            feedEvt = new EventOutput() {
                public void event() {
                    if (isStarted.get()) {
                        feed();
                    }
                }
            };
        }
        return feedEvt;
    }

    /**
     * Get an event that, when fired, will stop the timer. This will not throw
     * an IllegalStateException if the timer is not running.
     *
     * @return the event to stop the timer.
     */
    public EventOutput getStopEvent() {
        if (stopEvt == null) {
            stopEvt = new EventOutput() {
                public void event() {
                    if (isStarted.get()) {
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
        return new BooleanOutput() {
            public void set(boolean value) {
                if (value) {
                    if (!isStarted.get()) {
                        start();
                    }
                } else {
                    if (isStarted.get()) {
                        stop();
                    }
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

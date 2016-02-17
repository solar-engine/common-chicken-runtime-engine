/*
 * Copyright 2013-2016 Cel Skeggs
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
import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.CancelOutput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.scheduler.Scheduler;
import ccre.time.Time;
import ccre.util.Utils;

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

    private final String tag;

    public ExpirationTimer() {
        this(Utils.getMethodCaller(1).toString());
    }

    public ExpirationTimer(String tag) {
        this.tag = tag;
    }

    /**
     * The list of tasks, in an arbitrary order.
     */
    private final ArrayList<Task> tasks = new ArrayList<Task>();
    private final BooleanCell isStarted = new BooleanCell();
    private CancelOutput cancel;

    /**
     * Schedule an EventOutput to be triggered at a specific delay.
     *
     * @param delay the delay (in milliseconds) to trigger at.
     * @param cnsm the event to fire.
     * @throws IllegalStateException if the timer is already running.
     */
    public synchronized void schedule(long delay, EventOutput cnsm) throws IllegalStateException {
        schedule(FloatInput.always(delay / 1000f), cnsm);
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
        EventCell evt = new EventCell();
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
        EventCell evt = new EventCell();
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
        isStarted.safeSet(true);
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
        if (cancel != null) {
            cancel.cancel();
            cancel = null;
        }
        cancel = CancelOutput.nothing;
        long base = Time.currentTimeNanos();
        for (Task t : tasks) {
            cancel = cancel.combine(Scheduler.scheduleCancellableAt(this.tag, base + t.getDelayNanos(), t.cnsm));
        }
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
        if (cancel != null) {
            cancel.cancel();
            cancel = null;
        }
        isStarted.safeSet(false);
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
     * Control this timer with the given BooleanIO. This will start or stop the
     * timer when the input changes. This will not throw an
     * IllegalStateException if the timer is in the wrong state or log a
     * warning.
     *
     * Warning: the use of this method in conjunction with other control methods
     * may lead to unexpected results!
     *
     * @param when when this boolean is true, the timer will be running, and
     * when this boolean is false, the timer will be stopped.
     */
    public void runWhen(BooleanInput when) {
        when.send(this.getRunningControl());
    }

    /**
     * Get a BooleanIO that represents whether or not this timer is running.
     * This will start or stop the timer when the value is changed, and will
     * appear to be the value representing the new state afterward. This will
     * not throw an IllegalStateException if the timer is in the wrong state or
     * log a warning.
     *
     * @return a BooleanIO to monitor and control the ExpirationTimer.
     */
    public BooleanIO getRunning() {
        return BooleanIO.compose(getRunningStatus(), getRunningControl());
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
    private static class Task {

        /**
         * The event to fire.
         */
        public final EventOutput cnsm;
        /**
         * The source of tuning for the delay.
         */
        public final FloatInput tuning;

        /**
         * Create a new task with a tunable delay.
         *
         * @param delay The delay after which the task is fired, in seconds.
         * @param cnsm The EventOutput fired by this Task.
         */
        Task(FloatInput delay, EventOutput cnsm) {
            this.cnsm = cnsm;
            this.tuning = delay;
        }

        long getDelayNanos() {
            return (long) (tuning.get() * Time.NANOSECONDS_PER_SECOND);
        }
    }

    /**
     * End the ExpirationTimer's thread as soon as possible.
     */
    public synchronized void terminate() {
        if (isRunning()) {
            stop();
        }
    }
}

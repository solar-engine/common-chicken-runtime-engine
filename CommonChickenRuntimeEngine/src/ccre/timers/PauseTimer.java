/*
 * Copyright 2014-2016 Cel Skeggs
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

import ccre.channel.AbstractUpdatingInput;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.CancelOutput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.log.Logger;
import ccre.scheduler.Scheduler;
import ccre.time.Time;
import ccre.util.Utils;

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
public class PauseTimer extends AbstractUpdatingInput implements BooleanInput, EventOutput {

    private final String tag;

    private final Object cancelLock = new Object();
    // null if not running; value if running
    private volatile CancelOutput cancel = null;
    private final FloatInput timeout;

    /**
     * Create a new PauseTimer with the specified timeout in milliseconds.
     *
     * @param timeout The timeout for each time the timer is activated.
     */
    public PauseTimer(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("PauseTimer must have a positive timeout!");
        }
        this.tag = Utils.getMethodCaller(1).toString();
        this.timeout = FloatInput.always(timeout / 1000f);
    }

    /**
     * Create a new PauseTimer with the specified dynamic timeout in seconds.
     *
     * @param timeout The timeout for each time the timer is activated.
     */
    public PauseTimer(FloatInput timeout) {
        if (timeout == null) {
            throw new NullPointerException();
        }
        this.tag = Utils.getMethodCaller(1).toString();
        this.timeout = timeout;
    }

    /**
     * Create a new PauseTimer with the specified dynamic timeout in seconds,
     * and a descriptive tag for the time that it consumes.
     *
     * @param tag the scheduling tag
     * @param timeout The timeout for each time the timer is activated.
     */
    public PauseTimer(String tag, FloatInput timeout) {
        if (timeout == null) {
            throw new NullPointerException();
        }
        this.tag = tag;
        this.timeout = timeout;
    }

    /**
     * Terminate the timer and its thread. It will not function after this point
     * - do not attempt to use it.
     */
    public void terminate() {
        // nothing necessary since the scheduler rewrite
    }

    private final EventOutput end = () -> {
        synchronized (cancelLock) {
            cancel = null;
        }
        perform();
    };

    /**
     * Start the timer running.
     */
    public void event() {
        boolean raise = true;
        synchronized (cancelLock) {
            if (cancel != null) {
                cancel.cancel();
                cancel = null;
                raise = false;
            }
            cancel = Scheduler.scheduleCancellableNanos(this.tag, (long) (timeout.get() * Time.NANOSECONDS_PER_SECOND), end);
        }
        if (raise) {
            try {
                perform();
            } catch (Throwable thr) {
                Logger.severe("Failure while starting PauseTimer", thr);
            }
        }
    }

    public boolean get() {
        return cancel != null;
    }

    /**
     * When this timer stops running, trigger the specified EventOutput.
     *
     * @param trigger The EventOutput to trigger.
     */
    public void triggerAtEnd(EventOutput trigger) {
        send(BooleanOutput.polarize(trigger, null));
    }

    /**
     * When this timer starts running, trigger the specified EventOutput.
     *
     * @param trigger The EventOutput to trigger.
     */
    public void triggerAtStart(EventOutput trigger) {
        send(BooleanOutput.polarize(null, trigger));
    }

    /**
     * When this timer starts or stops running, trigger the specified
     * EventOutputs.
     *
     * @param start The EventOutput to trigger when the timer starts.
     * @param end The EventOutput to trigger when the timer ends.
     */
    public void triggerAtChanges(EventOutput start, EventOutput end) {
        send(BooleanOutput.polarize(end, start));
    }
}

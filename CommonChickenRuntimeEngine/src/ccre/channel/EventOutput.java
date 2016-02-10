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
package ccre.channel;

import ccre.log.Logger;
import ccre.time.Time;

/**
 * An event output or consumer. This can be fired (or produced or triggered or
 * activated or a number of other verbs that all mean the same thing), which
 * does something depending on where it came from.
 *
 * @author skeggsc
 */
public interface EventOutput {

    /**
     * An EventOutput that, when fired, does absolutely nothing.
     */
    public static final EventOutput ignored = new EventOutput() {
        @Override
        public void event() {
            // do nothing
        }

        // Important to the functioning of static EventOutput.combine
        public EventOutput combine(EventOutput other) {
            if (other == null) {
                throw new NullPointerException();
            }
            return other;
        };
    };

    /**
     * Fires the event. In other words, causes whatever it represents to happen.
     *
     * If any exception occurs during the propagation of the event, it will be
     * passed on by <code>event</code>.
     *
     * @see #safeEvent() for a version that catches any errors that occur.
     */
    public void event();

    /**
     * Fires the event. In other words, causes whatever it represents to happen.
     *
     * If any exception occurs during the propagation of the changes,
     * <code>safeEvent</code> will catch and log it as a
     * {@link ccre.log.LogLevel#SEVERE} error.
     *
     * @see #event() for a version that throws any errors that occur.
     */
    public default void safeEvent() {
        try {
            event();
        } catch (Throwable ex) {
            Logger.severe("Error during event propagation", ex);
        }
    }

    /**
     * Provides a combined version of this EventOutput and <code>other</code>,
     * such that when the provided EventOutput is fired, both this EventOutput
     * and <code>other</code> are fired.
     *
     * If any error occurs during propagation of events to either EventOutput,
     * the other output will still be fired. If both throw exceptions, then one
     * of the exceptions will be added as a suppressed exception to the other.
     *
     * @param other the other EventOutput to include.
     * @return the combined version of the EventOutputs.
     */
    public default EventOutput combine(EventOutput other) {
        if (other == null) {
            throw new NullPointerException();
        }
        EventOutput original = this;
        return () -> {
            try {
                original.event();
            } catch (Throwable thr) {
                try {
                    other.event();
                } catch (Throwable thr2) {
                    thr.addSuppressed(thr2);
                }
                throw thr;
            }
            other.event();
        };
    }

    /**
     * Combines any number of EventOutputs into a single EventOutput, such that
     * when the returned EventOutput is fired, all of the EventOutputs are
     * fired.
     *
     * If any error occurs during propagation of events to any EventOutput, the
     * other outputs will still be fired. If multiple outputs throw exceptions,
     * then one of them will be chosen arbitrarily and all of the others will be
     * added as suppressed exceptions.
     *
     * @param outputs the EventOutputs to include.
     * @return the combined version of the EventOutputs.
     */
    public static EventOutput combine(EventOutput... outputs) {
        // This works without including 'ignored' in the actual data structure
        // by having 'ignored' drop itself during combine.
        EventOutput o = ignored;
        for (EventOutput eo : outputs) {
            o = o.combine(eo);
        }
        return o;
    }

    /**
     * Provides a version of this EventOutput that only propagates events when
     * <code>allow</code> is true.
     *
     * @param allow if events should be allowed through the provided
     * EventOutput.
     * @return the lockable version of this EventOutput.
     */
    public default EventOutput filter(BooleanInput allow) {
        if (allow == null) {
            throw new NullPointerException();
        }
        EventOutput original = this;
        return () -> {
            if (allow.get()) {
                original.event();
            }
        };
    }

    /**
     * Provides a version of this EventOutput that only propagates events when
     * <code>deny</code> is false.
     *
     * @param deny if events should be disallowed through the provided
     * EventOutput.
     * @return the lockable version of this EventOutput.
     */
    public default EventOutput filterNot(BooleanInput deny) {
        return this.filter(deny.not());
    }

    /**
     * Provides a debounced version of this EventOutput, such that if it is
     * fired within <code>minMillis</code> of the last time it was fired, it
     * will be ignored.
     *
     * Only events that are propagated reset the timer: if an event is ignored,
     * it does not extend the reactivation deadline.
     *
     * @param minMillis the minimum amount of time between events.
     * @return the debounced version of this EventOutput.
     */
    public default EventOutput debounce(final long minMillis) {
        if (minMillis <= 0) {
            throw new IllegalArgumentException("debounce() parameter must be positive!");
        }
        EventOutput original = this;
        return new EventOutput() {
            private long nextFire = 0;

            public synchronized void event() {
                long now = Time.currentTimeMillis();
                if (now < nextFire) {
                    return; // Ignore event.
                }
                nextFire = now + minMillis;
                original.event();
            }
        };
    }

    /**
     * Fires this event when the specified event fires.
     *
     * <code>what.on(when)</code> is equivalent to <code>when.send(what)</code>
     *
     * @param when when to fire this event.
     * @return an EventOutput that deregisters the registered EventOutput. DO
     * NOT FIRE THIS RETURNED EVENT MORE THAN ONCE: UNDEFINED BEHAVIOR MAY
     * RESULT.
     */
    public default CancelOutput on(EventInput when) {
        return when.send(this);
    }

    /**
     * Returns a EventIO version of this event. If it is already a EventIO, it
     * will be returned directly. If it is not, a new EventCell will be created
     * around this EventOutput.
     *
     * @return a new IO
     */
    public default EventIO cell() {
        return new EventCell(this);
    }
}

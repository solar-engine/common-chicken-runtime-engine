/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.time.Time;
import ccre.util.Utils;

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
    public static final EventOutput ignored = () -> {
    };

    /**
     * Fire the event.
     */
    public void event();

    /**
     * Fire the event with recovery: try to recover instead of throwing an
     * exception.
     * 
     * @return if anything was changed to recover from an error.
     */
    public default boolean eventWithRecovery() {
        event();
        return false;
    }

    public default EventOutput combine(EventOutput other) {
        Utils.checkNull(other);
        EventOutput original = this;
        return new EventOutput() {
            @Override
            public void event() {
                original.event();
                other.event();
            }

            @Override
            public boolean eventWithRecovery() {
                // uses '|' instead of '||' to avoid short-circuiting.
                return original.eventWithRecovery() | other.eventWithRecovery();
            }
        };
    }

    public default EventOutput filter(BooleanInput allow) {
        if (allow == null) {
            throw new NullPointerException();
        }
        EventOutput original = this;
        return new EventOutput() {
            @Override
            public void event() {
                if (allow.get()) {
                    original.event();
                }
            }

            @Override
            public boolean eventWithRecovery() {
                if (allow.get()) {
                    return original.eventWithRecovery();
                }
                return false;
            }
        };
    }

    public default EventOutput filterNot(BooleanInput deny) {
        if (deny == null) {
            throw new NullPointerException();
        }
        EventOutput original = this;
        return new EventOutput() {
            @Override
            public void event() {
                if (!deny.get()) {
                    original.event();
                }
            }

            @Override
            public boolean eventWithRecovery() {
                if (!deny.get()) {
                    return original.eventWithRecovery();
                }
                return false;
            }
        };
    }

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
                    return;// Ignore event.
                }
                nextFire = now + minMillis;
                original.event();
            }

            public boolean eventWithRecovery() {
                long now = Time.currentTimeMillis();
                if (now < nextFire) {
                    return false;// Ignore event.
                }
                nextFire = now + minMillis;
                return original.eventWithRecovery();
            }
        };
    }

    /**
     * Fires this event when the specified event fires.
     *
     * <code>what.on(when)</code> is equivalent to <code>when.send(what)</code>
     *
     * @param when when to fire this event.
     */
    public default void on(EventInput when) {
        when.send(this);
    }
}

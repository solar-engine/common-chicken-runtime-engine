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
package ccre.channel;

import ccre.log.Logger;
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

    public default void safeEvent() {
        try {
            event();
        } catch (Throwable ex) {
            Logger.severe("Error during event propagation", ex);
        }
    }

    public default EventOutput combine(EventOutput other) {
        Utils.checkNull(other);
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

    public default EventOutput filterNot(BooleanInput deny) {
        return this.filter(deny.not());
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
     */
    public default EventOutput on(EventInput when) {
        return when.send(this);
    }
}

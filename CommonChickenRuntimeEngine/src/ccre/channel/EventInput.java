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
 * An event input or source. This produces events when it fires. A user can
 * register listeners to be called when the EventInput fires.
 * ccre.event.EventStatus is a good implementation of this so that you don't
 * have to write your own listener management code.
 *
 * @see EventStatus
 * @author skeggsc
 */
public interface EventInput extends UpdatingInput {

    /**
     * An EventInput that will never be fired. Ever.
     */
    public static EventInput never = new EventInput() {
        @Override
        public EventOutput onUpdateR(EventOutput notify) {
            return EventOutput.ignored;
        }
    };

    /**
     * Register a listener for when this event is fired, so that whenever this
     * event is fired, the specified output will get fired as well.
     *
     * If the same listener is added multiple times, it has the same effect as
     * if it was added once.
     *
     * @param listener the listener to add.
     * @see #unsend(EventOutput)
     */
    // TODO: rename this to 'then'?
    public default void send(EventOutput output) {
        if (output == null) {
            throw new NullPointerException();
        }
        onUpdate(output);
    }

    public default EventOutput sendR(EventOutput output) {
        if (output == null) {
            throw new NullPointerException();
        }
        return onUpdateR(output);
    }

    public default EventInput or(EventInput other) {
        Utils.checkNull(other);
        EventInput original = this;
        return new EventInput() {
            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                return original.onUpdateR(notify).combine(other.onUpdateR(notify));
            }

            @Override
            public void onUpdate(EventOutput notify) {
                original.onUpdate(notify);
                other.onUpdate(notify);
            }
        };
    }

    public default EventInput and(BooleanInput condition) {
        Utils.checkNull(condition);
        EventInput original = this;
        return new DerivedEventInput(original) {
            @Override
            protected boolean shouldProduce() {
                return condition.get();
            }
        };
    }

    public default EventInput andNot(BooleanInput condition) {
        Utils.checkNull(condition);
        EventInput original = this;
        return new DerivedEventInput(original) {
            @Override
            protected boolean shouldProduce() {
                return !condition.get();
            }
        };
    }

    public default EventInput debounced(final long minMillis) {
        if (minMillis <= 0) {
            throw new IllegalArgumentException("debounced() parameter must be positive!");
        }
        return new DerivedEventInput(this) {
            private long nextFire = 0;

            @Override
            protected boolean shouldProduce() {
                long now = Time.currentTimeMillis();
                if (now < nextFire) {
                    return false;// Ignore event.
                }
                nextFire = now + minMillis;
                return true;
            }
        };
    }
}

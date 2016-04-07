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

import ccre.time.Time;
import ccre.verifier.SetupPhase;

/**
 * An event input or source. This produces events when it fires. A user can
 * register listeners to be called when the EventInput fires.
 *
 * @see EventCell
 * @author skeggsc
 */
public interface EventInput extends UpdatingInput {

    /**
     * An EventInput that will never be fired. Ever.
     */
    public static EventInput never = new EventInput() {
        @Override
        public CancelOutput onUpdate(EventOutput notify) {
            if (notify == null) {
                throw new NullPointerException();
            }
            return CancelOutput.nothing;
        }
    };

    /**
     * Register a listener for when this event is fired, so that whenever this
     * event is fired, the specified output will get fired as well.
     *
     * If the same listener is added multiple times, it has the same effect as
     * if it was added once.
     *
     * @param output the listener to add.
     * @return a CancelOutput that deregisters the registered EventOutput. DO
     * NOT FIRE THIS RETURNED EVENT MORE THAN ONCE: UNDEFINED BEHAVIOR MAY
     * RESULT.
     */
    @SetupPhase
    public default CancelOutput send(EventOutput output) {
        if (output == null) {
            throw new NullPointerException();
        }
        return onUpdate(output);
    }

    /**
     * Provides an EventInput that fires when either this EventInput or
     * <code>other</code> fires.
     *
     * @param other the other EventInput.
     * @return the combined EventInput.
     */
    public default EventInput or(EventInput other) {
        if (other == null) {
            throw new NullPointerException();
        }
        EventInput original = this;
        return new EventInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                return original.onUpdate(notify).combine(other.onUpdate(notify));
            }
        };
    }

    /**
     * Provides an EventInput that fires only when this EventInput fires and the
     * current value of <code>allow</code> is true.
     *
     * @param allow if events should be allowed through.
     * @return the restricted EventInput.
     */
    public default EventInput and(BooleanInput allow) {
        if (allow == null) {
            throw new NullPointerException();
        }
        EventInput original = this;
        return new DerivedEventInput(original) {
            @Override
            protected boolean shouldProduce() {
                return allow.get();
            }
        };
    }

    /**
     * Provides an EventInput that fires only when this EventInput fires and the
     * current value of <code>deny</code> is false.
     *
     * @param deny if events should be allowed through.
     * @return the restricted EventInput.
     */
    public default EventInput andNot(BooleanInput deny) {
        if (deny == null) {
            throw new NullPointerException();
        }
        EventInput original = this;
        return new DerivedEventInput(original) {
            @Override
            protected boolean shouldProduce() {
                return !deny.get();
            }
        };
    }

    /**
     * Provides a debounced version of this EventInput, such that any events
     * that occur within <code>minMillis</code> of the last event will be
     * dropped.
     *
     * Only events that are propagated reset the timer: if an event is ignored,
     * it does not extend the reactivation deadline.
     *
     * @param minMillis the minimum spacing between events.
     * @return the debounced version of this EventInput.
     */
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

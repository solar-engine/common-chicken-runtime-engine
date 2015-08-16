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
import ccre.channel.DerivedEventInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;

/**
 * EventMixing is a class that provides a wide variety of useful static methods
 * to accomplish various common actions primarily relating to event channels.
 *
 * @author skeggsc
 * @see BooleanMixing
 * @see FloatMixing
 * @see Mixing
 */
public class EventMixing {

    /**
     * An EventOutput that, when fired, does absolutely nothing.
     */
    public static final EventOutput ignored = new EventOutput() {
        public void event() {
        }
    };
    /**
     * An EventInput that will never be fired. Ever.
     */
    public static final EventInput never = new EventInput() {
        @Override
        public EventOutput onUpdateR(EventOutput notify) {
            return ignored;
        }
    };

    /**
     * Combine two EventInputs so that either event firing will fire the result
     * event.
     *
     * @param a the first event source
     * @param b the second event source
     * @return the source that is fired by either of the original sources.
     */
    public static EventInput combine(final EventInput a, final EventInput b) {
        Mixing.checkNull(a, b);
        return new EventInput() {
            public void onUpdate(EventOutput notify) {
                a.onUpdate(notify);
                b.onUpdate(notify);
            }
            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                return combine(a.onUpdateR(notify), b.onUpdateR(notify));
            }
        };
    }

    /**
     * Combine multiple EventInputs so that any event firing will fire the
     * result event.
     *
     * @param sources the event sources
     * @return the source that is fired by any of the original sources.
     */
    public static EventInput combine(final EventInput... sources) {
        Mixing.checkNull((Object[]) sources);
        if (sources.length == 0) {
            return EventMixing.never;
        } else if (sources.length == 1) {
            return sources[0];
        }
        return new EventInput() {
            public void onUpdate(EventOutput notify) {
                for (EventInput input : sources) {
                    input.onUpdate(notify);
                }
            }
            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                EventOutput[] removals = new EventOutput[sources.length];
                for (int i = 0; i < sources.length; i++) {
                    EventInput input = sources[i];
                    removals[i] = input.onUpdateR(notify);
                }
                return combine(removals);
            }
        };
    }

    /**
     * Returns a combination of the specified events such that the returned
     * EventOutput will fire all arguments when fired.
     *
     * @param events the events to fire
     * @return the trigger for firing the arguments.
     */
    public static EventOutput combine(final EventOutput... events) {
        Mixing.checkNull((Object[]) events);
        return new EventOutput() {
            public void event() {
                for (EventOutput cnsm : events) {
                    cnsm.event();
                }
            }
        };
    }

    /**
     * Returns a combination of the specified events such that the returned
     * EventOutput will fire both arguments when fired.
     *
     * @param a the first event
     * @param b the second event
     * @return the trigger for firing the arguments.
     */
    public static EventOutput combine(final EventOutput a, final EventOutput b) {
        Mixing.checkNull(a, b);
        return new EventOutput() {
            public void event() {
                a.event();
                b.event();
            }
        };
    }

    /**
     * Returns a debounced version of the specified EventOutput, such that there
     * is a minimum delay of minMillis milliseconds between events.
     *
     * Any event sent before the timeout will be ignored.
     *
     * @param orig The EventOutput to debounce.
     * @param minMillis The minimum event delay.
     * @return The debounced version of the event consumer.
     */
    public static EventOutput debounce(final EventOutput orig, final int minMillis) {
        Mixing.checkNull(orig);
        return new EventOutput() {
            private long nextFire = 0;

            public synchronized void event() {
                long now = System.currentTimeMillis();
                if (now < nextFire) {
                    return; // Ignore event.
                }
                nextFire = now + minMillis;
                orig.event();
            }

            public boolean eventWithRecovery() {
                long now = System.currentTimeMillis();
                if (now < nextFire) {
                    return false; // Ignore event.
                }
                nextFire = now + minMillis;
                return orig.eventWithRecovery();
            }
        };
    }

    /**
     * Returns a debounced version of the specified EventInput, such that there
     * is a minimum delay of minMillis milliseconds between events.
     *
     * Any event sent before the timeout will be ignored.
     *
     * @param orig The EventInput to debounce.
     * @param minMillis The minimum event delay.
     * @return The debounced version of the event source.
     */
    public static EventInput debounce(EventInput orig, int minMillis) {
        Mixing.checkNull(orig);
        EventStatus e = new EventStatus();
        orig.send(debounce((EventOutput) e, minMillis));
        return e;
    }

    public static EventOutput filter(final BooleanInput shouldAllow, final EventOutput target) {
        Mixing.checkNull(shouldAllow, target);
        return new EventOutput() {
            public void event() {
                if (shouldAllow.get()) {
                    target.event();
                }
            }

            public boolean eventWithRecovery() {
                if (shouldAllow.get()) {
                    return target.eventWithRecovery();
                }
                return false;
            }
        };
    }

    public static EventOutput filterNot(final BooleanInput shouldAllow, final EventOutput target) {
        Mixing.checkNull(shouldAllow, target);
        return new EventOutput() {
            public void event() {
                if (!shouldAllow.get()) {
                    target.event();
                }
            }

            public boolean eventWithRecovery() {
                if (!shouldAllow.get()) {
                    return target.eventWithRecovery();
                }
                return false;
            }
        };
    }

    public static EventInput filter(final BooleanInput shouldAllow, final EventInput target) {
        Mixing.checkNull(shouldAllow, target);
        return new DerivedEventInput(target) {
            @Override
            protected boolean shouldProduce() {
                return shouldAllow.get();
            }
        };
    }

    public static EventInput filterNot(final BooleanInput shouldAllow, final EventInput target) {
        Mixing.checkNull(shouldAllow, target);
        return new DerivedEventInput(target) {
            @Override
            protected boolean shouldProduce() {
                return !shouldAllow.get();
            }
        };
    }

    private EventMixing() {
    }
}

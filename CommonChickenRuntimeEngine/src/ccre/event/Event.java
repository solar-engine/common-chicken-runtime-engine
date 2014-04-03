/*
 * Copyright 2013 Colby Skeggs
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
package ccre.event;

import ccre.concurrency.ConcurrentDispatchArray;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CArrayUtils;
import java.util.Iterator;

/**
 * An implementation of an EventSource. This can be fired using the .produce()
 * method, the .run() method or by firing this event as an EventConsumer.
 *
 * @author skeggsc
 */
public class Event implements EventSource, EventConsumer, Runnable {

    /**
     * Create an EventConsumer that, when fired, will run the specified
     * Runnable.
     *
     * @param target the runnable to run.
     * @return the EventConsumer that runs the runnable.
     */
    public static EventConsumer consumerForRunnable(final Runnable target) {
        return new EventConsumer() {
            public void eventFired() {
                target.run();
            }
        };
    }

    /**
     * Create a new Event.
     */
    public Event() {
        consumers = new ConcurrentDispatchArray<EventConsumer>();
    }

    /**
     * Create a new Event that fires the specified event when fired. This is
     * equivalent to adding the event as a listener.
     *
     * @param event the event to fire when this event is fired.
     * @see #addListener(ccre.event.EventConsumer)
     */
    public Event(EventConsumer event) {
        consumers = new ConcurrentDispatchArray<EventConsumer>();
        consumers.add(event);
    }

    /**
     * Create a new Event that fires the specified events when fired. This is
     * equivalent to adding the events as listeners.
     *
     * @param events the events to fire when this event is fired.
     * @see #addListener(ccre.event.EventConsumer)
     */
    public Event(EventConsumer... events) {
        consumers = new ConcurrentDispatchArray<EventConsumer>();
        consumers.addAll(CArrayUtils.asList(events));
    }
    /**
     * The events to fire when this event is fired.
     */
    private final ConcurrentDispatchArray<EventConsumer> consumers;

    /**
     * Returns whether or not this has any consumers that will get fired. If
     * this returns false, the produce() method will do nothing.
     *
     * @return whether or not the produce method would do anything.
     * @see #produce()
     */
    public boolean hasConsumers() {
        return !consumers.isEmpty();
    }

    /**
     * Produce this event - fire all listenering events.
     */
    public void produce() {
        for (EventConsumer ec : consumers) {
            ec.eventFired();
        }
    }

    public boolean addListener(EventConsumer client) {
        if (consumers.contains(client)) {
            return false;
        } else {
            consumers.add(client);
            return true;
        }
    }

    public void removeListener(EventConsumer client) throws IllegalStateException {
        if (!consumers.remove(client)) {
            throw new IllegalStateException("Listener not in event list: " + client);
        }
    }

    public void run() {
        produce();
    }

    @Override
    public void eventFired() {
        produce();
    }

    /**
     * Same as produce, but if an exception is thrown, the event will be
     * DETACHED and reported as such!
     *
     * @return If anything was detached.
     * @see #produce()
     */
    public boolean produceWithFailureRecovery() {
        boolean found = false;
        for (Iterator<EventConsumer> it = consumers.iterator(); it.hasNext();) {
            EventConsumer ec = it.next();
            try {
                ec.eventFired();
            } catch (Throwable thr) {
                Logger.log(LogLevel.SEVERE, "Event Subscribed Detached: " + ec, thr);
                it.remove();
                found = true;
            }
        }
        return found;
    }

    public void clearListeners() {
        consumers.clear();
    }
}

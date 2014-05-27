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

import ccre.concurrency.ConcurrentDispatchArray;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CArrayUtils;
import java.util.Iterator;

/**
 * An implementation of an EventInput. This can be fired using the .produce()
 * method or by firing this event as an EventOutput.
 *
 * @author skeggsc
 */
public class EventStatus implements EventInput, EventOutput {

    /**
     * The events to fire when this event is fired.
     */
    private final ConcurrentDispatchArray<EventOutput> consumers;

    /**
     * Create a new Event.
     */
    public EventStatus() {
        consumers = new ConcurrentDispatchArray<EventOutput>();
    }

    /**
     * Create a new Event that fires the specified event when fired. This is
     * equivalent to adding the event as a listener.
     *
     * @param event the event to fire when this event is fired.
     * @see #send(ccre.channel.EventOutput)
     */
    public EventStatus(EventOutput event) {
        consumers = new ConcurrentDispatchArray<EventOutput>();
        consumers.add(event);
    }

    /**
     * Create a new Event that fires the specified events when fired. This is
     * equivalent to adding the events as listeners.
     *
     * @param events the events to fire when this event is fired.
     * @see #send(ccre.channel.EventOutput)
     */
    public EventStatus(EventOutput... events) {
        consumers = new ConcurrentDispatchArray<EventOutput>();
        consumers.addAll(CArrayUtils.asList(events));
    }

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
        for (EventOutput ec : consumers) {
            ec.event();
        }
    }

    public void send(EventOutput client) {
        if (!consumers.contains(client)) {
            consumers.add(client);
        }
    }

    public void unsend(EventOutput client) throws IllegalStateException {
        if (!consumers.remove(client)) {
            throw new IllegalStateException("Listener not in event list: " + client);
        }
    }

    @Override
    public void event() {
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
        for (Iterator<EventOutput> it = consumers.iterator(); it.hasNext();) {
            EventOutput ec = it.next();
            try {
                ec.event();
            } catch (Throwable thr) {
                Logger.severe("Event Subscribed Detached: " + ec, thr);
                it.remove();
                found = true;
            }
        }
        return found;
    }

    /**
     * Clear all listeners on this EventStatus. Only do this if you have a very
     * good reason!
     */
    public void clearListeners() {
        consumers.clear();
    }
}

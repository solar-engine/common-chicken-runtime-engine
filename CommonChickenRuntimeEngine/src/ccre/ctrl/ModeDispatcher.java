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
package ccre.ctrl;

import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.util.CHashMap;

/**
 * A provider for a system that routes fired events along to one of a set of
 * specified events based on the current 'mode' of the dispatcher.
 *
 * @author skeggsc
 */
public final class ModeDispatcher {
    // TODO: This class needs some love! It's unused and should either be improved or scrapped.

    /**
     * The names of the states of the ModeDispatcher. The names are not
     * currently used, although the length is.
     */
    private final String[] modenames;
    /**
     * The current mode. In the range 0&lt;=currentMode&lt;modenames.length
     */
    private int currentMode;

    /**
     * A node representing a specific EventSource's routed EventSources.
     */
    private class Node {

        /**
         * The EventSources that are routed to.
         */
        private EventSource[] sources;
        /**
         * The Event objects behind the sources. They are the same, but here
         * they are stored as an Event so that they can be produced.
         */
        private Event[] events;

        /**
         * Create a new node for the given EventSource.
         *
         * @param base the EventSource to create routes for.
         */
        private Node(EventSource base) {
            events = new Event[modenames.length];
            sources = new EventSource[modenames.length];
            for (int i = 0; i < modenames.length; i++) {
                Event e = new Event();
                events[i] = e;
                sources[i] = e;
            }
            base.addListener(new EventConsumer() {
                public void eventFired() {
                    events[currentMode].produce();
                }
            });
        }
    }
    /**
     * All the currently routed EventSources and their nodes. Used to cache so
     * that only one Node is allocated for each EventSource.
     */
    protected CHashMap<EventSource, Node> dispatches = new CHashMap<EventSource, Node>();

    /**
     * Create a new ModeDispatcher with the specified mode to start at, and the
     * specified list of nodes.
     *
     * @param defaultMode the mode to start at.
     * @param modenames the list of modes.
     */
    public ModeDispatcher(int defaultMode, String... modenames) {
        this.modenames = modenames;
        currentMode = defaultMode;
    }

    /**
     * Create routes for each mode and the specified event. The 0th route will
     * be fired when the mode is 0 and the specified event is fired, the 1st
     * route will be fired when the mode is 1 and the specified event is fired,
     * and so on.
     *
     * @param event the event that fires the corresponding route.
     * @return the created routes.
     */
    public EventSource[] getSources(EventSource event) {
        Node n = dispatches.get(event);
        if (n == null) {
            n = new Node(event);
            dispatches.put(event, n);
        }
        return n.sources;
    }

    /**
     * Change the mode to the specified index.
     *
     * @param mode the new mode.
     */
    public void setMode(int mode) {
        if (mode < 0 || mode >= modenames.length) {
            throw new IndexOutOfBoundsException("Mode is out of range!");
        }
        currentMode = mode;
    }
}

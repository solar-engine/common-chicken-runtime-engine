package ccre.ctrl;

import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import java.util.Hashtable;

/**
 * A provider for a system that routes fired events along to one of a set of
 * specified events based on the current 'mode' of the dispatcher.
 *
 * @author skeggsc
 */
public class ModeDispatcher {
    // TODO: This class needs some love! It's unused and should either be improved or scrapped.

    /**
     * The names of the states of the ModeDispatcher. The names are not
     * currently used, although the length is.
     */
    protected final String[] modenames;
    /**
     * The current mode. In the range 0&lt;=currentMode&lt;modenames.length
     */
    protected int currentMode;

    /**
     * A node representing a specific EventSource's routed EventSources.
     */
    protected class Node {

        /**
         * The EventSources that are routed to.
         */
        protected EventSource[] sources;
        /**
         * The Event objects behind the sources. They are the same, but here
         * they are stored as an Event so that they can be produced.
         */
        protected Event[] events;

        /**
         * Create a new node for the given EventSource.
         *
         * @param base the EventSource to create routes for.
         */
        protected Node(EventSource base) {
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
    protected Hashtable<EventSource, Node> dispatches = new Hashtable<EventSource, Node>();

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
     * @param mode the new mode.
     */
    public void setMode(int mode) {
        if (mode < 0 || mode >= modenames.length) {
            throw new IndexOutOfBoundsException("Mode is out of range!");
        }
        currentMode = mode;
    }
}

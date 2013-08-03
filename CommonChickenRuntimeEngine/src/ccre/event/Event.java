package ccre.event;

import ccre.util.CArrayList;
import ccre.util.CArrayUtils;

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
        consumers = new CArrayList<EventConsumer>();
    }

    /**
     * Create a new Event that fires the specified event when fired. This is
     * equivalent to adding the event as a listener.
     *
     * @param event the event to fire when this event is fired.
     * @see #addListener(ccre.event.EventConsumer)
     */
    public Event(EventConsumer event) {
        consumers = new CArrayList<EventConsumer>();
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
        consumers = new CArrayList<EventConsumer>(CArrayUtils.asList(events));
    }
    /**
     * The events to fire when this event is fired.
     */
    protected CArrayList<EventConsumer> consumers;

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
}

package ccre.event;

/**
 * An event consumer. This can be fired (or produced or triggered or activated
 * or a number of other verbs that all mean the same thing), which does
 * something depending on where it came from.
 *
 * @author skeggsc
 */
public interface EventConsumer {

    /**
     * Fire the event.
     */
    void eventFired();
}

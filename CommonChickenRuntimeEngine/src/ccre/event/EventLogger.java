package ccre.event;

import ccre.log.LogLevel;
import ccre.log.Logger;

/**
 * An event that logs a string at a LogLevel when fired.
 *
 * @author skeggsc
 */
public class EventLogger implements EventConsumer {

    /**
     * When the specified event is fired, log the specified message at the
     * specified logging level.
     *
     * @param when when to log.
     * @param level what level to log at.
     * @param message what message to log.
     */
    public static void log(EventSource when, LogLevel level, String message) {
        when.addListener(new EventLogger(level, message));
    }
    /**
     * The logging level at which to log the message.
     */
    public LogLevel level;
    /**
     * The message to log.
     */
    public String message;

    /**
     * When this event is fired, log the specified message at the specified
     * logging level.
     *
     * @param level the level to log at.
     * @param message the message to log.
     */
    public EventLogger(LogLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public void eventFired() {
        Logger.log(level, message);
    }
}

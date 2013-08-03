package ccre.log;

/**
 * A target that can receive logging messages.
 *
 * @author skeggsc
 */
public interface LoggingTarget {

    /**
     * Log the given message at the given level with an optional throwable (can
     * be null).
     *
     * @param level the level to log at.
     * @param message the message to log.
     * @param throwable the optional throwable to log.
     */
    public void log(LogLevel level, String message, Throwable throwable);

    /**
     * Log the given message at the given level with an optional extended
     * string. (usually more details, such as a throwable traceback or
     * instructions on how to fix the error)
     *
     * @param level the level to log to.
     * @param message the message to log.
     * @param extended the optional extended message to log.
     */
    public void log(LogLevel level, String message, String extended);
}

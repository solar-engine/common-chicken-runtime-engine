package ccre.log;

/**
 * A logging target that sends all logged messages to a set of specified
 * loggers. This is useful, for example, if you want to log across the network
 * but don't want to stop logging to standard error.
 *
 * @author skeggsc
 */
public class MultiTargetLogger implements LoggingTarget {

    /**
     * Create a new MultiTargetLogger. It will log to a specified list of
     * targets.
     *
     * @param targets the targets to log to.
     */
    public MultiTargetLogger(LoggingTarget... targets) {
        this.targets = targets;
    }
    /**
     * The list of targets that should receive logging.
     */
    protected LoggingTarget[] targets;

    public void log(LogLevel level, String message, Throwable thr) {
        for (LoggingTarget t : targets) {
            t.log(level, message, thr);
        }
    }

    public void log(LogLevel level, String message, String extended) {
        for (LoggingTarget t : targets) {
            t.log(level, message, extended);
        }
    }
}

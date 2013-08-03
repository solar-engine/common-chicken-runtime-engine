package ccre.log;

/**
 * A logging target that will write all messages to the standard error. This is
 * the default logger.
 *
 * @author skeggsc
 */
class StandardStreamLogger implements LoggingTarget {

    public void log(LogLevel level, String message, Throwable thr) {
        if (thr != null) {
            System.err.println("LOG{" + level.abbreviation + "} " + message);
            thr.printStackTrace();
        } else {
            System.err.println("LOG[" + level.abbreviation + "] " + message);
        }
    }

    public void log(LogLevel level, String message, String extended) {
        System.err.println("LOG[" + level.abbreviation + "] " + message);
        if (extended != null && extended.length() != 0) {
            System.err.println(extended);
        }
    }
}

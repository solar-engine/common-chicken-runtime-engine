package ccre.log;

import ccre.workarounds.ThrowablePrinter;
import java.io.PrintStream;

/**
 * A logging target that will write all messages to the specified PrintStream.
 *
 * @author skeggsc
 */
public class PrintStreamLogger implements LoggingTarget {

    /**
     * The PrintStream to write the logs to.
     */
    protected PrintStream str;

    /**
     * Create a new PrintStreamLogger to log to the specific output.
     *
     * @param out the PrintStream to log to.
     */
    public PrintStreamLogger(PrintStream out) {
        if (out == null) {
            throw new NullPointerException();
        }
        this.str = out;
    }

    public void log(LogLevel level, String message, Throwable thr) {
        if (thr != null) {
            str.println("LOG{" + level.abbreviation + "} " + message);
            ThrowablePrinter.printThrowable(thr, str);
        } else {
            str.println("LOG[" + level.abbreviation + "] " + message);
        }
    }

    public void log(LogLevel level, String message, String extended) {
        str.println("LOG[" + level.abbreviation + "] " + message);
        if (extended != null && extended.length() != 0) {
            str.println(extended);
        }
    }
}

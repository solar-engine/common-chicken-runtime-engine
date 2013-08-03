package ccre.workarounds;

import ccre.log.LogLevel;
import ccre.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A provider that can print out a Throwable to a given PrintStream.
 *
 * This is only needed because Squawk doesn't have a
 * Throwable.printStackTrace(PrintStream) method, so a different provider is
 * needed for Squawk.
 *
 * @author skeggsc
 */
public abstract class ThrowablePrinter {

    /**
     * The current ThrowablePrinter.
     */
    static ThrowablePrinter provider;

    /**
     * Ensure that there is an available provider. At least, there will be a
     * fake throwable printer that can't actually print the exception traceback.
     */
    public static void initProvider() {
        if (provider == null) {
            try {
                provider = (ThrowablePrinter) Class.forName("ccre.workarounds.DefaultThrowablePrinter").newInstance();
            } catch (InstantiationException ex) {
                provider = new FakeThrowablePrinter();
                Logger.log(LogLevel.WARNING, "No throwable printing provider!", ex);
            } catch (IllegalAccessException ex) {
                provider = new FakeThrowablePrinter();
                Logger.log(LogLevel.WARNING, "No throwable printing provider!", ex);
            } catch (ClassNotFoundException ex) {
                provider = new FakeThrowablePrinter();
                Logger.log(LogLevel.WARNING, "No throwable printing provider!", ex);
            }
        }
    }

    /**
     * Print the specified Throwable to the specified PrintStream.
     *
     * @param thr the throwable to print.
     * @param pstr the PrintStream to write to.
     */
    public static void printThrowable(Throwable thr, PrintStream pstr) {
        initProvider();
        provider.send(thr, pstr);
    }

    /**
     * Convert the specified Throwable to a String that contains what would have
     * been printed by printThrowable.
     *
     * Printing this value is equivalent to just calling printThrowable
     * originally.
     *
     * @param thr the throwable to print.
     * @return the String version of the throwable, including the trackback.
     */
    public static String toStringThrowable(Throwable thr) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        printThrowable(thr, new PrintStream(out));
        return out.toString();
    }

    /**
     * Send the specified Throwable to the specified PrintStream.
     *
     * @param thr the throwable
     * @param pstr the PrintStream.
     */
    public abstract void send(Throwable thr, PrintStream pstr);
}

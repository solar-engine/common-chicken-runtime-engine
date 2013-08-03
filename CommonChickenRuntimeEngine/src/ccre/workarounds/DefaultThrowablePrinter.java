package ccre.workarounds;

import java.io.PrintStream;

/**
 * The default ThrowablePrinter that is used when a fully-features java system
 * is available. This does the same as
 * <code>thr.printStackTrace(pstr);</code>
 *
 * @author skeggsc
 */
public class DefaultThrowablePrinter extends ThrowablePrinter {

    @Override
    public void send(Throwable thr, PrintStream pstr) {
        thr.printStackTrace(pstr);
    }
}

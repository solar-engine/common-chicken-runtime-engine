package ccre.workarounds;

import java.io.PrintStream;

/**
 * A fake throwable printer for when nothing better is available. This just
 * prints out the result of .toString() on the Throwable object.
 *
 * @author skeggsc
 */
public class FakeThrowablePrinter extends ThrowablePrinter {

    @Override
    public void send(Throwable thr, PrintStream pstr) {
        pstr.println(thr);
    }
}

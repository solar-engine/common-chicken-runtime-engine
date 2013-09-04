package ccre.workarounds;

import ccre.log.Logger;
import com.sun.squawk.ExecutionPoint;
import com.sun.squawk.GC;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;
import com.sun.squawk.vm.FieldOffsets;
import java.io.PrintStream;

/**
 * A throwable printer that works on Squawk and the FRC robot. This is because
 * Java ME doesn't use the same interface as Java SE.
 *
 * @author skeggsc
 */
public class IgneousThrowablePrinter extends ThrowablePrinter {

    /**
     * Has this yet been registered as the storage provider?
     */
    private static boolean registered = false;

    /**
     * Ensure that this is registered as the storage provider. A warning will be
     * logged if this is called a second time.
     */
    public static void register() {
        if (registered) {
            Logger.warning("IgneousThrowablePrinter already registered!");
            return;
        }
        registered = true;
        ThrowablePrinter.provider = new IgneousThrowablePrinter();
    }

    private IgneousThrowablePrinter() {
    }

    public void send(Throwable thr, PrintStream pstr) {
        // The code here works similarly to VM.printExceptionAndTrace
        pstr.print(GC.getKlass(thr).getName());
        String message = thr.getMessage();
        if (message != null) {
            pstr.print(": ");
            pstr.println(message);
        } else {
            pstr.println();
        }
        ExecutionPoint[] trace = (ExecutionPoint[]) NativeUnsafe.getObject(thr, (int) FieldOffsets.java_lang_Throwable$trace);
        if (thr != VM.getOutOfMemoryError() && trace != null) {
            for (int i = 0; i < trace.length; ++i) {
                if (trace[i] != null) {
                    pstr.print("    ");
                    trace[i].print(pstr);
                    pstr.println();
                } else {
                    pstr.println("    undecipherable");
                }
            }
        }
    }
}

package ccre.phidget;

import ccre.chan.BooleanInput;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInput;
import ccre.cluck.CluckGlobals;
import ccre.holders.StringHolder;

/**
 * A system to read data over the network about the Phidget system, and provide
 * it in a structured format.
 *
 * @author skeggsc
 */
public class PhidgetReader {

    static {
        CluckGlobals.ensureInitializedCore();
    }
    /**
     * Digital outputs on the phidget.
     */
    public final static BooleanOutput[] digitalOutputs = new BooleanOutput[8];

    static {
        for (int i = 0; i < digitalOutputs.length; i++) {
            digitalOutputs[i] = CluckGlobals.encoder.subscribeBooleanOutput("phidget-bo" + i);
        }
    }
    /**
     * Digital inputs on the phidget.
     */
    public static final BooleanInput[] digitalInputs = new BooleanInput[8];

    static {
        for (int i = 0; i < digitalInputs.length; i++) {
            digitalInputs[i] = CluckGlobals.encoder.subscribeBooleanInputProducer("phidget-bi" + i, false);
        }
    }
    /**
     * Analog inputs on the phidget.
     */
    public static final FloatInput[] analogInputs = new FloatInput[8];

    static {
        for (int i = 0; i < analogInputs.length; i++) {
            analogInputs[i] = CluckGlobals.encoder.subscribeFloatInputProducer("phidget-ai" + i, 0);
        }
    }
    /**
     * LCD lines on the phidget.
     */
    public static final StringHolder[] lcdLines = new StringHolder[2];

    static {
        for (int i = 0; i < 2; i++) {
            lcdLines[i] = CluckGlobals.encoder.subscribeStringHolder("phidget-lcd" + i, "?");
        }
    }
}

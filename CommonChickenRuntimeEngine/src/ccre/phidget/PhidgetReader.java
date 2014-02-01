/*
 * Copyright 2013-2014 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package ccre.phidget;

import ccre.chan.BooleanInput;
import ccre.chan.BooleanInputProducer;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInput;
import ccre.chan.FloatInputProducer;
import ccre.cluck.CluckGlobals;
import ccre.ctrl.Mixing;
import java.io.PrintStream;

/**
 * A system to read data over the network about the Phidget system, and provide
 * it in a structured format.
 *
 * @author skeggsc
 */
public class PhidgetReader {

    private PhidgetReader() {
    }

    static {
        CluckGlobals.ensureInitializedCore();
    }
    /**
     * Digital outputs on the phidget.
     */
    public final static BooleanOutput[] digitalOutputs = new BooleanOutput[8];

    static {
        for (int i = 0; i < digitalOutputs.length; i++) {
            digitalOutputs[i] = CluckGlobals.node.subscribeBO("phidget/phidget-bo" + i);
        }
    }
    /**
     * Digital inputs on the phidget, as Producers. To get Polls, use
     * getDigitalInput.
     *
     * @see #getDigitalInput(int)
     */
    public static final BooleanInputProducer[] digitalInputs = new BooleanInputProducer[8];

    /**
     * Get a full version of the BooleanInputs - including polling
     * functionality.
     *
     * @param id The index in digitalInputs.
     * @return The full BooleanInput.
     */
    public static BooleanInput getDigitalInput(int id) {
        BooleanInput bi = (BooleanInput) digitalInputs[id];
        bi.addTarget(Mixing.ignoredBooleanOutput);
        return bi;
    }

    static {
        for (int i = 0; i < digitalInputs.length; i++) {
            digitalInputs[i] = CluckGlobals.node.subscribeBIP("phidget/phidget-bi" + i, false);
        }
    }
    /**
     * Analog inputs on the phidget.
     */
    public static final FloatInputProducer[] analogInputs = new FloatInputProducer[8];

    /**
     * Get a full version of the FloatInputs - including polling functionality.
     *
     * @param id The index in analogInputs.
     * @return The full FloatInput.
     */
    public static FloatInput getAnalogInput(int id) {
        FloatInput fi = (FloatInput) analogInputs[id];
        fi.addTarget(Mixing.ignoredFloatOutput);
        return fi;
    }

    static {
        for (int i = 0; i < analogInputs.length; i++) {
            analogInputs[i] = CluckGlobals.node.subscribeFIP("phidget/phidget-ai" + i, false);
        }
    }
    /**
     * LCD lines on the phidget.
     */
    public static final PrintStream[] phidgetLCD = new PrintStream[2];

    static {
        for (int i = 0; i < phidgetLCD.length; i++) {
            phidgetLCD[i] = new PrintStream(CluckGlobals.node.subscribeOS("phidget/phidget-lcd" + i));
        }
    }
    /**
     * If the Phidget is attached to the driver station.
     */
    public static final BooleanInputProducer attached = CluckGlobals.node.subscribeBIP("phidget/phidget-attached", false);
}

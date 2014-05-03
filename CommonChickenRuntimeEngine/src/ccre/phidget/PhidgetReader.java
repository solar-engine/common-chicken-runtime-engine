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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInput;
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

    /**
     * Digital outputs on the phidget.
     */
    private static final BooleanOutput[] digitalOutputs = new BooleanOutput[8];

    /**
     * Get the specified BooleanOutput.
     *
     * @param id The index in digitalInputs.
     * @return The full BooleanInput.
     */
    public static BooleanOutput getDigitalOutput(int id) {
        return digitalOutputs[id];
    }

    static {
        for (int i = 0; i < digitalOutputs.length; i++) {
            digitalOutputs[i] = CluckGlobals.getNode().subscribeBO("phidget/phidget-bo" + i);
        }
    }
    /**
     * Digital inputs on the phidget, as Producers. To get Polls, use
     * getDigitalInput.
     *
     * @see #getDigitalInput(int)
     */
    private static final BooleanInput[] digitalInputs = new BooleanInput[8];

    /**
     * Get a full version of the BooleanInputs - including polling
     * functionality.
     *
     * @param id The index in digitalInputs.
     * @return The full BooleanInput.
     */
    public static BooleanInput getDigitalInput(int id) {
        BooleanInput bi = digitalInputs[id];
        bi.send(Mixing.ignoredBooleanOutput);
        return bi;
    }

    static {
        for (int i = 0; i < digitalInputs.length; i++) {
            digitalInputs[i] = CluckGlobals.getNode().subscribeBI("phidget/phidget-bi" + i, false);
        }
    }
    /**
     * Analog inputs on the phidget.
     */
    private static final FloatInput[] analogInputs = new FloatInput[8];

    /**
     * Get a full version of the FloatInputs - including polling functionality.
     *
     * @param id The index in analogInputs.
     * @return The full FloatInput.
     */
    public static FloatInput getAnalogInput(int id) {
        FloatInput fi = analogInputs[id];
        fi.send(Mixing.ignoredFloatOutput);
        return fi;
    }

    static {
        for (int i = 0; i < analogInputs.length; i++) {
            analogInputs[i] = CluckGlobals.getNode().subscribeFI("phidget/phidget-ai" + i, false);
        }
    }
    /**
     * LCD lines on the Phidget.
     */
    private static final PrintStream[] phidgetLCD = new PrintStream[2];

    static {
        for (int i = 0; i < phidgetLCD.length; i++) {
            phidgetLCD[i] = new PrintStream(CluckGlobals.getNode().subscribeOS("phidget/phidget-lcd" + i));
        }
    }

    /**
     * Get the specified line of the Phidget screen.
     *
     * @param id The index in digitalInputs.
     * @return The full BooleanInput.
     */
    public static PrintStream getLCDLine(int id) {
        return phidgetLCD[id];
    }
    /**
     * If the Phidget is attached to the driver station.
     */
    public static final BooleanInput attached = CluckGlobals.getNode().subscribeBI("phidget/phidget-attached", true);
}

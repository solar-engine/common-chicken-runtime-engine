/*
 * Copyright 2013 Colby Skeggs
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
package ccre.obsidian;

import ccre.chan.FloatInputPoll;
import java.io.*;

/**
 * A system that manages analog-to-digital converters, so that they can be used
 * by other systems.
 *
 * @author skeggsc
 */
class ADCManager {

    /**
     * The base directory for analog inputs.
     */
    private static final File adcdir;

    /**
     * An object representing an analog channel.
     */
    private static class ADC implements FloatInputPoll {

        /**
         * The input ID of this analog input.
         */
        public final int inputID;
        /**
         * The file that is read to read this analog input.
         */
        public final File curF;

        private ADC(int inputID) {
            this.inputID = inputID;
            this.curF = new File(adcdir, "AIN" + inputID);
        }

        @Override
        public float readValue() {
            try {
                FileInputStream fin = new FileInputStream(curF);
                try {
                    char[] cur = new char[4];
                    int i = 0;
                    while (true) {
                        int c = fin.read();
                        if (c == -1 || c == '\n' || c == '\r') {
                            break;
                        }
                        if (i >= cur.length) {
                            throw new ObsidianHardwareException("Invalid length of ADC read: too long!");
                        }
                        cur[i++] = (char) c;
                    }
                    if (i == 0) {
                        throw new ObsidianHardwareException("Invalid length of ADC read: too short!");
                    }
                    String s = new String(cur, 0, i);
                    int val;
                    try {
                        val = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        throw new ObsidianHardwareException("Invalid number from ADC: " + s, ex);
                    }
                    if (val < 0 || val >= 4096) {
                        throw new ObsidianHardwareException("Value out of range from ADC: " + val);
                    }
                    return val / 4095f;
                } finally {
                    fin.close();
                }
            } catch (IOException ex) {
                throw new ObsidianHardwareException("IO exception during analog read", ex);
            }
        }
    }
    /**
     * The current array of analog inputs.
     */
    private static final ADC[] adcs = new ADC[7];

    /**
     * Open the specified analog channel for input.
     *
     * @param inputID The channel number for the analog input.
     * @return a FloatInputPoll that represents the current uncalibrated value
     * of the analog input, from 0.0 to 1.0.
     * @throws ObsidianHardwareException
     */
    public static FloatInputPoll getChannel(int inputID) throws ObsidianHardwareException {
        if (inputID < 0 || inputID > 6) {
            throw new ObsidianHardwareException("Expected an analog input ID in the range 0 to 6!");
        }
        ADC a = adcs[inputID];
        if (a == null) {
            a = new ADC(inputID);
            adcs[inputID] = a;
        }
        return a;
    }

    static {
        File helpDir = null;
        try {
            // Init ADC
            DeviceTree.loadCapeManager("cape-bone-iio");
            File ocp = DeviceTree.autocompletePath(new File("/sys/devices"), "ocp");
            helpDir = DeviceTree.autocompletePath(ocp, "helper");
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load ADC Cape Manager!", ex);
        }
        adcdir = helpDir;
    }
}

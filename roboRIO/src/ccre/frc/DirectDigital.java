/*
 * Copyright 2015-2016 Colby Skeggs
 * Copyright 2015 Jake Springer
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
 *
 *
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.frc;

import edu.wpi.first.wpilibj.hal.DIOJNI;
import edu.wpi.first.wpilibj.hal.InterruptJNI;
import edu.wpi.first.wpilibj.hal.JNIWrapper;

class DirectDigital {
    public static final int DIGITAL_NUM = 26, INTERRUPT_NUM = 8;

    static final boolean WATCH_ASYNCHRONOUS = false, WATCH_SYNCHRONOUS = true;
    static final boolean TRIGGER_DIGITAL = false, TRIGGER_ANALOG = true;

    /**
     * Number of digital pins on a roboRIO.
     */
    static final int DIGITAL_PINS = 26;

    private static final long[] digitals = new long[DIGITAL_NUM];
    private static final boolean[] asInputs = new boolean[DIGITAL_NUM];
    private static final long[] interrupts = new long[INTERRUPT_NUM];
    private static final Integer[] interruptMap = new Integer[DIGITAL_NUM];

    public static synchronized void init(int channel, boolean asInput) {
        if (channel < 0 || channel >= DIGITAL_NUM) {
            throw new RuntimeException("Invalid digital port: " + channel);
        }

        if (digitals[channel] == 0) {
            long port = DIOJNI.initializeDigitalPort(JNIWrapper.getPort((byte) channel));
            DIOJNI.allocateDIO(port, asInput);
            digitals[channel] = port;
            asInputs[channel] = asInput;
        } else if (asInputs[channel] != asInput) {
            throw new RuntimeException("Digital port allocated for I and O: " + channel);
        }
    }

    public static synchronized void free(int channel) {
        long port = digitals[channel];
        if (port == 0) {
            throw new RuntimeException("Unallocated digital port: " + channel);
        }

        DIOJNI.freeDIO(port);
    }

    public static void set(int channel, boolean value) {
        long dig = digitals[channel];
        if (dig == 0) {
            throw new RuntimeException("Unallocated digital port: " + channel);
        }
        if (asInputs[channel]) {
            throw new RuntimeException("Digital port not opened for writing: " + channel);
        }

        DIOJNI.setDIO(dig, (short) (value ? 1 : 0));
    }

    public static boolean get(int channel) {
        long dig = digitals[channel];
        if (dig == 0) {
            throw new RuntimeException("Unallocated digital port: " + channel);
        }
        if (!asInputs[channel]) {
            throw new RuntimeException("Digital port not opened for reading: " + channel);
        }
        return DIOJNI.getDIO(dig);
    }

    private static synchronized int allocateInterrupt() {
        for (int i = 0; i < INTERRUPT_NUM; i++) {
            if (interrupts[i] == 0) {
                // TODO: what if this fails? intr is leaked.
                interrupts[i] = InterruptJNI.initializeInterrupts(i, WATCH_SYNCHRONOUS);
                return i;
            }
        }
        throw new RuntimeException("Ran out of interrupts! Consider using fewer interrupts in your code.");
    }

    public static synchronized void initInterruptsSynchronous(int id, boolean risingEdge, boolean fallingEdge) {
        if (interruptMap[id] == null) {
            int intr = allocateInterrupt();

            byte module_id = 0;
            InterruptJNI.requestInterrupts(interrupts[intr], module_id, id, TRIGGER_DIGITAL);

            interruptMap[id] = intr;
        }

        InterruptJNI.setInterruptUpSourceEdge(interrupts[interruptMap[id]], risingEdge, fallingEdge);
    }

    // returns if timed out
    public static boolean waitForInterrupt(int id, float timeout, boolean ignorePrevious) {
        if (interruptMap[id] == null) {
            throw new RuntimeException("No interrupt allocated for digital input: " + id);
        }

        return InterruptJNI.waitForInterrupt(interrupts[interruptMap[id]], timeout, ignorePrevious) == 0;
    }

    /**
     * Returns a pointer to the digital source port on the specified channel.
     * Will return null if digital source has not been initialized.
     *
     * @param channel the digital channel number to access, starting at 0.
     * @return the C pointer for this digital channel, or null if uninitialized.
     */
    public static long getDigitalSource(int channel) {
        return digitals[channel];
    }

    public static boolean isDigitalSourceInput(int channel) {
        return asInputs[channel];
    }
}

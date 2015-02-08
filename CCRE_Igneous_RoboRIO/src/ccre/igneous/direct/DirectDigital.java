/*
 * Copyright 2015 Colby Skeggs
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
package ccre.igneous.direct;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.DIOJNI;
import edu.wpi.first.wpilibj.hal.JNIWrapper;

class DirectDigital {
    public static final int DIGITAL_NUM = 26;

    private static final ByteBuffer[] digitals = new ByteBuffer[DIGITAL_NUM];
    private static final boolean[] asInputs = new boolean[DIGITAL_NUM];

    public static synchronized void init(int channel, boolean asInput) {
        if (channel < 0 || channel >= DIGITAL_NUM) {
            throw new RuntimeException("Invalid digital port: " + channel);
        }

        if (digitals[channel] == null) {
            IntBuffer status = Common.allocateInt();
            ByteBuffer port = DIOJNI.initializeDigitalPort(JNIWrapper.getPort((byte) channel), status);
            Common.check(status);
            DIOJNI.allocateDIO(port, (byte) (asInput ? 1 : 0), status);
            Common.check(status);
            digitals[channel] = port;
            asInputs[channel] = asInput;
        } else if (asInputs[channel] != asInput) {
            throw new RuntimeException("Digital port allocated for I and O: " + channel);
        }
    }

    public static synchronized void free(int channel) {
        ByteBuffer port = digitals[channel];
        if (port == null) {
            throw new RuntimeException("Unallocated digital port: " + channel);
        }
        IntBuffer status = Common.allocateInt();
        DIOJNI.freeDIO(port, status);
        Common.check(status);
    }

    public static void set(int channel, boolean value) {
        ByteBuffer dig = digitals[channel];
        if (dig == null) {
            throw new RuntimeException("Unallocated digital port: " + channel);
        }
        if (asInputs[channel]) {
            throw new RuntimeException("Digital port not opened for writing: " + channel);
        }
        IntBuffer status = Common.allocateInt();
        DIOJNI.setDIO(dig, (short) (value ? 1 : 0), status); // just FPGA errors
        Common.check(status);
    }

    public static boolean get(int channel) {
        ByteBuffer dig = digitals[channel];
        if (dig == null) {
            throw new RuntimeException("Unallocated digital port: " + channel);
        }
        if (!asInputs[channel]) {
            throw new RuntimeException("Digital port not opened for reading: " + channel);
        }
        IntBuffer status = Common.allocateInt();
        boolean value = DIOJNI.getDIO(dig, status) != 0; // just FPGA errors
        Common.check(status);
        return value;
    }
}

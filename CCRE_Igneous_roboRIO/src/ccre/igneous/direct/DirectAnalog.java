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
import java.nio.LongBuffer;

import edu.wpi.first.wpilibj.hal.AnalogJNI;
import edu.wpi.first.wpilibj.hal.JNIWrapper;

class DirectAnalog {
    public static final int ANALOG_NUM = 8;

    private static final ByteBuffer[] analogs = new ByteBuffer[ANALOG_NUM];

    public static synchronized ByteBuffer init(int channel) {
        if (channel < 0 || channel >= ANALOG_NUM) {
            throw new RuntimeException("Analog channel: " + channel);
        }

        if (analogs[channel] == null) {
            IntBuffer status = Common.allocateInt();
            analogs[channel] = AnalogJNI.initializeAnalogInputPort(JNIWrapper.getPort((byte) channel), status);
            Common.check(status);
        }

        return analogs[channel];
    }

    public static ByteBuffer initWithAccumulator(int channel) {
        if (channel != 0 && channel != 1) {
            throw new RuntimeException("Only Analog channels 0 and 1 have accumulators.");
        }
        ByteBuffer analog = init(channel);
        IntBuffer status = Common.allocateInt();
        AnalogJNI.initAccumulator(analog, status);
        Common.check(status);

        AnalogJNI.setAccumulatorDeadband(analog, 0, status);
        Common.check(status);

        return analog;
    }

    public static void configure(ByteBuffer port, int averageBits, int oversampleBits) {
        IntBuffer status = Common.getCheckBuffer();
        AnalogJNI.setAnalogAverageBits(port, averageBits, status);
        AnalogJNI.setAnalogOversampleBits(port, oversampleBits, status);
        Common.check(status);
    }

    public static float getAverageVoltage(ByteBuffer channel) {
        IntBuffer status = Common.getCheckBuffer();
        double out = AnalogJNI.getAnalogAverageVoltage(channel, status);
        Common.check(status); // just FPGA and FRCComm-calibrate errors
        return (float) out;
    }

    public static int getAverageValue(ByteBuffer channel) {
        IntBuffer status = Common.getCheckBuffer();
        int out = AnalogJNI.getAnalogAverageValue(channel, status);
        Common.check(status); // just FPGA errors
        return out;
    }

    public static void setGlobalSampleRate(double sampleRate) {
        IntBuffer status = Common.getCheckBuffer();
        AnalogJNI.setAnalogSampleRate(sampleRate, status);
        Common.check(status);
    }

    public static double getGlobalSampleRate() {
        IntBuffer status = Common.getCheckBuffer();
        double value = AnalogJNI.getAnalogSampleRate(status); // only FPGA errors
        Common.check(status);
        return value;
    }

    public static void resetAccumulator(ByteBuffer analog) {
        IntBuffer status = Common.getCheckBuffer();
        AnalogJNI.resetAccumulator(analog, status); // FPGA errors or null accumulator, which should only be a concern if the port is not 0 or 1. Not an issue.
        Common.check(status); // if this fails with a NULL_PARAMETER, that's because the channel is not an accumulator channel.
    }

    public static long readAccumulatorValue(ByteBuffer analog, IntBuffer directCount) {
        LongBuffer value = Common.allocateLong();
        IntBuffer status = Common.getCheckBuffer();
        AnalogJNI.getAccumulatorOutput(analog, value, directCount, status); // FPGA errors, null accumulator, or null pointer. Not an issue.
        Common.check(status);
        return value.get(0);
    }

    public static void setAccumulatorCenter(ByteBuffer analog, int center) {
        IntBuffer status = Common.getCheckBuffer();
        AnalogJNI.setAccumulatorCenter(analog, center, status);
        Common.check(status);
    }

    public static long getLSBWeight(ByteBuffer gyro) {
        IntBuffer status = Common.getCheckBuffer();
        long value = AnalogJNI.getAnalogLSBWeight(gyro, status); // just FRCComm-calibrate errors
        Common.check(status);
        return value;
    }
}

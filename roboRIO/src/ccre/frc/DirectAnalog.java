/*
 * Copyright 2015-2016 Cel Skeggs
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

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import edu.wpi.first.wpilibj.hal.AnalogJNI;
import edu.wpi.first.wpilibj.hal.JNIWrapper;

class DirectAnalog {
    public static final int ANALOG_NUM = 8;

    private static final long[] analogs = new long[ANALOG_NUM];

    public static synchronized long init(int channel) {
        if (channel < 0 || channel >= ANALOG_NUM) {
            throw new RuntimeException("Analog channel: " + channel);
        }

        if (analogs[channel] == 0) {
            analogs[channel] = AnalogJNI.initializeAnalogInputPort(JNIWrapper.getPort((byte) channel));
        }

        return analogs[channel];
    }

    public static long initWithAccumulator(int channel) {
        if (channel != 0 && channel != 1) {
            throw new RuntimeException("Only Analog channels 0 and 1 have accumulators.");
        }
        long analog = init(channel);
        AnalogJNI.initAccumulator(analog);

        AnalogJNI.setAccumulatorDeadband(analog, 0);

        return analog;
    }

    public static void configure(long port, int averageBits, int oversampleBits) {
        AnalogJNI.setAnalogAverageBits(port, averageBits);
        AnalogJNI.setAnalogOversampleBits(port, oversampleBits);
    }

    public static float getAverageVoltage(long channel) {
        return (float) AnalogJNI.getAnalogAverageVoltage(channel);
    }

    public static int getAverageValue(long channel) {
        return AnalogJNI.getAnalogAverageValue(channel);
    }

    public static void setGlobalSampleRate(double sampleRate) {
        AnalogJNI.setAnalogSampleRate(sampleRate);
    }

    public static double getGlobalSampleRate() {
        return AnalogJNI.getAnalogSampleRate();
    }

    public static void resetAccumulator(long analog) {
        // FPGA errors or null accumulator, which should only be a concern if
        // the port is not 0 or 1. Not an issue.
        AnalogJNI.resetAccumulator(analog);
        // if this fails with a NULL_PARAMETER, that's because the channel is
        // not an accumulator channel.
    }

    public static long readAccumulatorValue(long analog, IntBuffer directCount) {
        LongBuffer value = Common.allocateLong();
        AnalogJNI.getAccumulatorOutput(analog, value, directCount);
        return value.get(0);
    }

    public static void setAccumulatorCenter(long analog, int center) {
        AnalogJNI.setAccumulatorCenter(analog, center);
    }

    public static long getLSBWeight(long gyro) {
        return AnalogJNI.getAnalogLSBWeight(gyro);
    }
}

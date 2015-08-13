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

class DirectGyro {
    private static final ByteBuffer[] gyros = new ByteBuffer[DirectAnalog.ANALOG_NUM];
    private static final double[] offsets = new double[DirectAnalog.ANALOG_NUM];
    private static final int AVERAGE_BITS = 0, OVERSAMPLE_BITS = 10;

    public static ByteBuffer init(int channel) throws InterruptedException {
        if (channel < 0 || channel >= DirectAnalog.ANALOG_NUM) {
            throw new RuntimeException("Invalid Gyro port: " + channel);
        }
        if (gyros[channel] == null) {
            ByteBuffer analog = DirectAnalog.initWithAccumulator(channel);
            DirectAnalog.configure(analog, AVERAGE_BITS, OVERSAMPLE_BITS);
            DirectAnalog.setGlobalSampleRate(50.0 * (1 << (AVERAGE_BITS + OVERSAMPLE_BITS)));

            Thread.sleep(1000); // TODO: Don't do it like this. This is just for WPILib compatibility.

            DirectAnalog.resetAccumulator(analog);

            Thread.sleep(5000);

            IntBuffer countB = Common.allocateInt();
            long value = DirectAnalog.readAccumulatorValue(analog, countB);
            int count = countB.get(0);

            int center = (int) ((double) value / (double) count + .5);

            offsets[channel] = ((double) value / (double) count) - center;

            DirectAnalog.setAccumulatorCenter(analog, center);
            DirectAnalog.resetAccumulator(analog);

            gyros[channel] = analog;
        }

        return gyros[channel];
    }

    public static void reset(ByteBuffer gyro) {
        DirectAnalog.resetAccumulator(gyro);
    }

    public static float getAngle(ByteBuffer gyro, int channel, double voltsPerDegreePerSecond) {
        IntBuffer countB = Common.allocateInt();
        long raw = DirectAnalog.readAccumulatorValue(gyro, countB);
        long count = countB.get(0);

        long value = raw - (long) (count * offsets[channel]);

        double scaledValue = value * 1e-9 * DirectAnalog.getLSBWeight(gyro) * (1 << AVERAGE_BITS) / (DirectAnalog.getGlobalSampleRate() * voltsPerDegreePerSecond);

        return (float) scaledValue;
    }
}

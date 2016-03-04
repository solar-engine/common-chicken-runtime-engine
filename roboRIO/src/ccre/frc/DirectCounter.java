/*
 * Copyright 2015 Jake Springer, 2016 Cel Skeggs
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
package ccre.frc;

import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.CounterJNI;

// TODO: document all the Direct* classes.
class DirectCounter {

    public static final boolean ANALOG_INPUT = true;
    public static final boolean DIGITAL_INPUT = false;

    public static long init(int channelUp, int channelDown, int mode) {
        IntBuffer index = Common.getSharedBuffer();
        long counter = CounterJNI.initializeCounter(mode, index);

        if (channelUp == FRC.UNUSED && channelDown == FRC.UNUSED) {
            throw new RuntimeException("At least one channel must be used.");
        }
        if (channelUp != FRC.UNUSED) {
            setUpSource(counter, channelUp);
        }
        if (channelDown != FRC.UNUSED) {
            setDownSource(counter, channelDown);
        }

        // detect rising edge (first argument), but don't detect falling edge
        // (second argument)
        setUpSourceEdge(counter, true, false);

        // don't detect rising edge (first argument), but detect falling edge
        // (second argument)
        setDownSourceEdge(counter, false, true);

        return counter;
    }

    public static void free(long counter) {
        CounterJNI.freeCounter(counter);
    }

    public static void setUpSource(long counter, int channel) {
        if (DirectDigital.getDigitalSource(channel) == 0) {
            throw new RuntimeException("Digital source has not been allocated yet");
        }
        if (!DirectDigital.isDigitalSourceInput(channel)) {
            throw new RuntimeException("Channel " + channel + " is a digital output when it needs to be a digital input");
        }

        CounterJNI.setCounterUpSource(counter, channel, DIGITAL_INPUT);
    }

    public static void setDownSource(long counter, int channel) {
        if (DirectDigital.getDigitalSource(channel) == 0) {
            throw new RuntimeException("Digital source has not been allocated yet");
        }
        if (!DirectDigital.isDigitalSourceInput(channel)) {
            throw new RuntimeException("Channel " + channel + " is a digital output when it needs to be a digital input");
        }

        CounterJNI.setCounterDownSource(counter, channel, DIGITAL_INPUT);
    }

    public static void clearUpSource(long counter) {
        CounterJNI.clearCounterUpSource(counter);
    }

    public static void clearDownSource(long counter) {
        CounterJNI.clearCounterDownSource(counter);
    }

    public static void setUpSourceEdge(long counter, boolean risingEdge, boolean fallingEdge) {
        CounterJNI.setCounterUpSourceEdge(counter, risingEdge, fallingEdge);
    }

    public static void setDownSourceEdge(long counter, boolean risingEdge, boolean fallingEdge) {
        CounterJNI.setCounterDownSourceEdge(counter, risingEdge, fallingEdge);
    }

    public static int get(long channel) {
        return CounterJNI.getCounter(channel);
    }
}

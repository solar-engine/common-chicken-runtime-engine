package ccre.frc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.CounterJNI;

public class DirectCounter {

    public static byte ANALOG_INPUT = 1;
    public static byte DIGITAL_INPUT = 0;
    
    public static ByteBuffer init(int channelUp, int channelDown, int mode) {
        IntBuffer status = Common.getCheckBuffer();
        IntBuffer index = Common.allocateInt();
        ByteBuffer counter = CounterJNI.initializeCounter(mode, index, status);
        Common.check(status);

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

    public static void free(ByteBuffer counter) {
        IntBuffer status = Common.getCheckBuffer();
        CounterJNI.freeCounter(counter, status);
        Common.check(status);
    }

    public static void setUpSource(ByteBuffer counter, int channel) {
        IntBuffer status = Common.getCheckBuffer();
        if (DirectDigital.getDigitalSource(channel) == null) {
            throw new RuntimeException("Digital source has not been allocated yet");
        }
        if (!DirectDigital.isDigitalSourceInput(channel)) {
            throw new RuntimeException("Cannot set up source as a digital output");
        }

        // the third argument is a c++ bool that represents if the counter
        // source is an analog input
        CounterJNI.setCounterUpSource(counter, channel, DIGITAL_INPUT, status);
        Common.check(status);
    }

    public static void setDownSource(ByteBuffer counter, int channel) {
        IntBuffer status = Common.getCheckBuffer();
        if (DirectDigital.getDigitalSource(channel) == null) {
            throw new RuntimeException("Digital source has not been allocated yet");
        }
        if (!DirectDigital.isDigitalSourceInput(channel)) {
            throw new RuntimeException("Cannot set up source as a digital output");
        }

        // the third argument is a c++ bool that represents if the counter
        // source is an analog input
        CounterJNI.setCounterDownSource(counter, channel, DIGITAL_INPUT, status);
        Common.check(status);
    }

    public static void clearUpSource(ByteBuffer counter) {
        IntBuffer status = Common.getCheckBuffer();
        CounterJNI.clearCounterUpSource(counter, status);
        Common.check(status);
    }

    public static void clearDownSource(ByteBuffer counter) {
        IntBuffer status = Common.getCheckBuffer();
        CounterJNI.clearCounterDownSource(counter, status);
        Common.check(status);
    }

    public static void setUpSourceEdge(ByteBuffer counter, boolean risingEdge, boolean fallingEdge) {
        IntBuffer status = Common.getCheckBuffer();
        CounterJNI.setCounterUpSourceEdge(counter, (byte) (risingEdge ? 1 : 0),
                (byte) (fallingEdge ? 1 : 0), status);
        Common.check(status);
    }

    public static void setDownSourceEdge(ByteBuffer counter, boolean risingEdge, boolean fallingEdge) {
        IntBuffer status = Common.getCheckBuffer();
        CounterJNI.setCounterDownSourceEdge(counter, (byte) (risingEdge ? 1 : 0),
                (byte) (fallingEdge ? 1 : 0), status);
        Common.check(status);
    }

    public static int get(ByteBuffer channel) {
        IntBuffer status = Common.getCheckBuffer();
        int value = CounterJNI.getCounter(channel, status);
        Common.check(status);
        return value;
    }
}

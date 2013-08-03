package ccre.ctrl;

import ccre.chan.FloatInputPoll;
import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;
import ccre.event.EventConsumer;
import ccre.event.EventSource;

/**
 * Contains various presets for driving the robot.
 *
 * Provided types of drive:
 *
 * Tank drive is where two joystick axes are used to control two motors,
 * respectively.
 *
 * Extended tank drive adds another joystick axis that is added to both motors'
 * outputs, so that direct forward and backward movement is easy.
 *
 * Other types to come later.
 *
 * Types of implementations:
 *
 * Asynchronous: Requires FloatInputProducers for the inputs, and requires no
 * event to write through values.
 *
 * Event: Can use FloatInputPoll, and returns an event that will update the
 * motors.
 *
 * Synchronous: Can use FloatInputPoll; when a given EventSource is fired, the
 * motors will update.
 *
 * Mixing contains many more generic methods to work with channels.
 * 
 * @see Mixing
 * @author skeggsc
 */
public class DriverImpls {

    /**
     * Run tank drive on the given two FloatInputProducers and FloatOutputs.
     *
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createAsynchTankDriver(FloatInputProducer leftIn, FloatInputProducer rightIn, FloatOutput leftOut, FloatOutput rightOut) {
        leftIn.addTarget(leftOut);
        rightIn.addTarget(rightOut);
    }

    /**
     * When the returned EventConsumer is fired, run tank drive on the given two
     * FloatInputPolls and FloatOutputs.
     *
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @return the EventConsumer that will update the motors.
     * @see DriverImpls
     */
    public static EventConsumer createTankDriverEvent(final FloatInputPoll leftIn, final FloatInputPoll rightIn, final FloatOutput leftOut, final FloatOutput rightOut) {
        return new EventConsumer() {
            public void eventFired() {
                leftOut.writeValue(leftIn.readValue());
                rightOut.writeValue(rightIn.readValue());
            }
        };
    }

    /**
     * When the specified EventSource is fired, run tank drive on the given two
     * FloatInputPolls and FloatOutputs.
     *
     * @param source when to update the motors.
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createSynchTankDriver(EventSource source, FloatInputPoll leftIn, FloatInputPoll rightIn, FloatOutput leftOut, FloatOutput rightOut) {
        source.addListener(createTankDriverEvent(leftIn, rightIn, leftOut, rightOut));
    }

    /**
     * When the returned EventConsumer is fired, run extended tank drive on the
     * given three FloatInputPolls and FloatOutputs.
     *
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param allIn the forward control axis. will be added to both outputs.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @return the EventConsumer that will update the motors.
     * @see DriverImpls
     */
    public static EventConsumer createExtendedTankDriverEvent(final FloatInputPoll leftIn, final FloatInputPoll rightIn, final FloatInputPoll allIn, final FloatOutput leftOut, final FloatOutput rightOut) {
        return new EventConsumer() {
            public void eventFired() {
                float ai = allIn.readValue();
                leftOut.writeValue(leftIn.readValue() + ai);
                rightOut.writeValue(rightIn.readValue() + ai);
            }
        };
    }

    /**
     * When the specified EventSource is fired, run extended tank drive on the
     * given three FloatInputPolls and FloatOutputs.
     *
     * @param source when to update the motors.
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param allIn the forward control axis. will be added to both outputs.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createExtendedSynchTankDriver(EventSource source, FloatInputPoll leftIn, FloatInputPoll rightIn, FloatInputPoll allIn, FloatOutput leftOut, FloatOutput rightOut) {
        source.addListener(createExtendedTankDriverEvent(leftIn, rightIn, allIn, leftOut, rightOut));
    }
}

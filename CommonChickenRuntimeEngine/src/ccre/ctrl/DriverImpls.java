/*
 * Copyright 2013-2014 Colby Skeggs, Alexander Mackworth (single joystick)
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
package ccre.ctrl;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;

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
 * Synchronous: Can use FloatInputPoll; when a given EventInput is fired, the
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
    public static void createAsynchTankDriver(FloatInput leftIn, FloatInput rightIn, FloatOutput leftOut, FloatOutput rightOut) {
        leftIn.send(leftOut);
        rightIn.send(rightOut);
    }

    /**
     * When the returned EventOutput is fired, run tank drive on the given two
     * FloatInputPolls and FloatOutputs.
     *
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @return the EventOutput that will update the motors.
     * @see DriverImpls
     */
    public static EventOutput createTankDriverEvent(final FloatInputPoll leftIn, final FloatInputPoll rightIn, final FloatOutput leftOut, final FloatOutput rightOut) {
        return new EventOutput() {
            public void event() {
                leftOut.set(leftIn.get());
                rightOut.set(rightIn.get());
            }
        };
    }

    /**
     * When the specified EventInput is fired, run tank drive on the given two
     * FloatInputPolls and FloatOutputs.
     *
     * @param source when to update the motors.
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createSynchTankDriver(EventInput source, FloatInputPoll leftIn, FloatInputPoll rightIn, FloatOutput leftOut, FloatOutput rightOut) {
        source.send(createTankDriverEvent(leftIn, rightIn, leftOut, rightOut));
    }

    /**
     * When the returned EventOutput is fired, run extended tank drive on the
     * given three FloatInputPolls and FloatOutputs.
     *
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param allIn the forward control axis. will be added to both outputs.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @return the EventOutput that will update the motors.
     * @see DriverImpls
     */
    public static EventOutput createExtendedTankDriverEvent(final FloatInputPoll leftIn, final FloatInputPoll rightIn, final FloatInputPoll allIn, final FloatOutput leftOut, final FloatOutput rightOut) {
        return new EventOutput() {
            public void event() {
                float ai = allIn.get();
                leftOut.set(leftIn.get() + ai);
                rightOut.set(rightIn.get() + ai);
            }
        };
    }

    /**
     * When the specified EventInput is fired, run extended tank drive on the
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
    public static void createExtendedSynchTankDriver(EventInput source, FloatInputPoll leftIn, FloatInputPoll rightIn, FloatInputPoll allIn, FloatOutput leftOut, FloatOutput rightOut) {
        source.send(createExtendedTankDriverEvent(leftIn, rightIn, allIn, leftOut, rightOut));
    }

    /**
     * Run single joystick drive on the given two FloatInputProducers and
     * FloatOutputs.
     *
     * @param joystickXAxis the joystick's x-axis.
     * @param joystickYAxis the joystick's y-axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createAsynchSingleJoystickDriver(final FloatInput joystickXAxis, final FloatInput joystickYAxis, final FloatOutput leftOut, final FloatOutput rightOut) {
        joystickXAxis.send(new FloatOutput() {
            public void set(final float xAxis) {
                final float yAxis = joystickYAxis.get();
                leftOut.set(yAxis + xAxis);
                rightOut.set(yAxis - xAxis);
            }
        });

        joystickYAxis.send(new FloatOutput() {
            public void set(final float yAxis) {
                final float xAxis = joystickXAxis.get();
                leftOut.set(yAxis + xAxis);
                rightOut.set(yAxis - xAxis);
            }
        });
    }

    /**
     * Run single joystick drive on the given two FloatInputProducers and
     * FloatOutputs.
     *
     * @param joystick the joystick.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createAsynchSingleJoystickDriver(final IJoystick joystick, final FloatOutput leftOut, final FloatOutput rightOut) {
        createAsynchSingleJoystickDriver(joystick.getXAxisSource(), joystick.getYAxisSource(), leftOut, rightOut);
    }

    /**
     * When the returned EventInput is fired, run single joystick drive on the
     * given two FloatInputPolls and FloatOutputs.
     *
     * @param joystickXAxis the joystick's x-axis.
     * @param joystickYAxis the joystick's y-axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @return the EventOutput that will update the motors.
     * @see DriverImpls
     */
    public static EventOutput createSingleJoystickDriveEvent(final FloatInputPoll joystickXAxis, final FloatInputPoll joystickYAxis, final FloatOutput leftOut, final FloatOutput rightOut) {
        return new EventOutput() {
            public void event() {
                final float xAxis = joystickXAxis.get();
                final float yAxis = joystickYAxis.get();
                leftOut.set(yAxis + xAxis);
                rightOut.set(yAxis - xAxis);
            }
        };
    }

    /**
     * When the returned EventInput is fired, run single joystick drive on the
     * given joystick and two FloatOutputs.
     *
     * @param joystick the joystick.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @return the EventOutput that will update the motors.
     * @see DriverImpls
     */
    public static EventOutput createSingleJoystickDriveEvent(IJoystick joystick, FloatOutput leftOut, FloatOutput rightOut) {
        return createSingleJoystickDriveEvent(joystick.getXChannel(), joystick.getYChannel(), leftOut, rightOut);
    }

    /**
     * When the specified EventInput is fired, run single joystick drive on the
     * given two FloatInputPolls and FloatOutputs.
     *
     * @param source when to update the motors.
     * @param joystickXAxis the joystick's x-axis.
     * @param joystickYAxis the joystick's y-axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createSynchSingleJoystickDriver(EventInput source, final FloatInputPoll joystickXAxis, final FloatInputPoll joystickYAxis, FloatOutput leftOut, FloatOutput rightOut) {
        source.send(createSingleJoystickDriveEvent(joystickXAxis, joystickYAxis, leftOut, rightOut));
    }

    /**
     * When the specified EventInput is fired, run single joystick drive on the
     * given joystick and two FloatOutputs.
     *
     * @param source when to update the motors.
     * @param joystick the joystick.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void createSynchSingleJoystickDriver(EventInput source, final IJoystick joystick, FloatOutput leftOut, FloatOutput rightOut) {
        createSynchSingleJoystickDriver(source, joystick.getXChannel(), joystick.getYChannel(), leftOut, rightOut);
    }

    private DriverImpls() {
    }
}

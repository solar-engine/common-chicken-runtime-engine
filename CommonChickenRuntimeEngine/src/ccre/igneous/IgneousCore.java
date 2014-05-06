/*
 * Copyright 2013-2014 Colby Skeggs
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
package ccre.igneous;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;
import ccre.ctrl.Mixing;
import ccre.instinct.InstinctRegistrar;

/**
 * A Core class for Igneous. Extend this (or SimpleCore, which is easier) in
 * order to write an application to run on the robot.
 *
 * @see SimpleCore
 * @author skeggsc
 */
public abstract class IgneousCore implements InstinctRegistrar {
    // TODO: Refactor the Igneous system so that it doesn't need all this make... stuff.

    /**
     * Signifies that the motor should be directly outputted without negation.
     */
    public static final boolean MOTOR_FORWARD = false;
    /**
     * Signifies that the motor should be outputted after negating the value.
     */
    public static final boolean MOTOR_REVERSE = true;
    /**
     * The first joystick attached to the driver station.
     */
    protected IJoystick joystick1;
    /**
     * The second joystick attached to the driver station.
     */
    protected IJoystick joystick2;
    /**
     * The third joystick attached to the driver station.
     */
    protected IJoystick joystick3;
    /**
     * The fourth joystick attached to the driver station.
     */
    protected IJoystick joystick4;

    /**
     * The launcher that provides all implementations for this.
     */
    IgneousLauncher launcher;
    /**
     * Produced during every state where the driver station is attached.
     */
    protected EventInput globalPeriodic;
    /**
     * Produced when the robot enters autonomous mode.
     */
    protected EventInput startedAutonomous;
    /**
     * Produced during autonomous mode.
     */
    protected EventInput duringAutonomous;
    /**
     * Produced when the robot enters disabled mode.
     */
    protected EventInput robotDisabled;
    /**
     * Produced while the robot is disabled.
     */
    protected EventInput duringDisabled;
    /**
     * Produced when the robot enters teleop mode.
     */
    protected EventInput startedTeleop;
    /**
     * Produced during teleop mode.
     */
    protected EventInput duringTeleop;
    /**
     * Produced when the robot enters testing mode.
     */
    protected EventInput startedTesting;
    /**
     * Produced during testing mode.
     */
    protected EventInput duringTesting;
    /**
     * Constant time periodic. Should pulse every 10 ms, as accurately as
     * possible.
     */
    protected EventInput constantPeriodic;

    /**
     * Implement this method - it should set up everything that your robot needs
     * to do.
     */
    protected abstract void createRobotControl();

    // Factory methods
    /**
     * Get an IJoystick for the specified kinect joystick. Joysticks on the
     * driver station are accessed through the variables joystick1...joystick4.
     *
     * @param isRightArm If the right arm joystick should be used instead of the
     * left. (6 instead of 5 if you're used to the old system.)
     * @return the IJoystick.
     */
    protected final IJoystick getKinectJoystick(boolean isRightArm) {
        return launcher.getKinectJoystick(isRightArm);
    }

    /**
     * Create a reference to a Jaguar speed controller on the specified ID and
     * motor reversal, with a specified ramping rate.
     *
     * If the ramping rate is zero, then no ramping is applied. Don't use this
     * if you don't know what you're doing! Otherwise, the ramping rate is the
     * maximum difference allowed per 10 milliseconds (constantPeriodic). (So a
     * rate of 0.1f means that you need 200 milliseconds to go from -1.0 to
     * 1.0.)
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor should be unmodified,
     * MOTOR_REVERSE if the motor should be reversed.
     * @param ramping the ramping rate.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    protected final FloatOutput makeJaguarMotor(int id, boolean negate, float ramping) {
        return FloatMixing.addRamping(ramping, constantPeriodic, launcher.makeJaguar(id, negate));
    }

    /**
     * Create a reference to a Victor speed controller on the specified ID and
     * motor reversal, with a specified ramping rate.
     *
     * If the ramping rate is zero, then no ramping is applied. Don't use this
     * if you don't know what you're doing! Otherwise, the ramping rate is the
     * maximum difference allowed per 10 milliseconds (constantPeriodic). (So a
     * rate of 0.1f means that you need 200 milliseconds to go from -1.0 to
     * 1.0.)
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor should be unmodified,
     * MOTOR_REVERSE if the motor should be reversed.
     * @param ramping the ramping rate.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    protected final FloatOutput makeVictorMotor(int id, boolean negate, float ramping) {
        return FloatMixing.addRamping(ramping, constantPeriodic, launcher.makeVictor(id, negate));
    }

    /**
     * Create a reference to a Talon speed controller on the specified ID and
     * motor reversal, with a specified ramping rate.
     *
     * If the ramping rate is zero, then no ramping is applied. Don't use this
     * if you don't know what you're doing! Otherwise, the ramping rate is the
     * maximum difference allowed per 10 milliseconds (constantPeriodic). (So a
     * rate of 0.1f means that you need 200 milliseconds to go from -1.0 to
     * 1.0.)
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor should be unmodified,
     * MOTOR_REVERSE if the motor should be reversed.
     * @param ramping the ramping rate.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    protected final FloatOutput makeTalonMotor(int id, boolean negate, float ramping) {
        return FloatMixing.addRamping(ramping, constantPeriodic, launcher.makeTalon(id, negate));
    }

    /**
     * Create a reference to a solenoid on the specified port.
     *
     * @param id the port of the solenoid.
     * @return the output that will control the solenoid.
     */
    protected final BooleanOutput makeSolenoid(int id) {
        return launcher.makeSolenoid(id);
    }

    /**
     * Create a reference to a digital output on the specified port.
     *
     * @param id the port of the digital output.
     * @return the output that will control the digital output.
     */
    protected final BooleanOutput makeDigitalOutput(int id) {
        return launcher.makeDigitalOutput(id);
    }

    /**
     * Get a reference to the analog input that reads the current battery
     * voltage, scaled to represent the real battery voltage.
     *
     * @return The current battery voltage.
     */
    protected final FloatInputPoll getBatteryVoltage() {
        return launcher.getBatteryVoltage();
    }

    /**
     * Create a reference to an analog input on the specified port with the
     * specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @return the analog input, reporting in voltage.
     */
    protected final FloatInputPoll makeAnalogInput(int id, int averageBits) {
        return launcher.makeAnalogInput(id, averageBits);
    }

    /**
     * Create a reference to an analog input's raw value on the specified port
     * with the specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @return the analog input, reporting in uncalibrated units.
     */
    protected final FloatInputPoll makeAnalogInput_ValueBased(int id, int averageBits) {
        return launcher.makeAnalogInput_ValuedBased(id, averageBits);
    }

    /**
     * Create a reference to a digital input on the specified port.
     *
     * @param id the port number.
     * @return the digital input.
     */
    protected final BooleanInputPoll makeDigitalInput(int id) {
        return launcher.makeDigitalInput(id);
    }

    /**
     * Create a reference to a servo controller for the specified port and
     * minimum and maximum values.
     *
     * @param id the port number.
     * @param minInput the value on the output that should correspond to the
     * servo's minimum position.
     * @param maxInput the value on the output that should correspond to the
     * servo's maximum position.
     * @return the FloatOutput that controls the servo.
     */
    protected final FloatOutput makeServo(int id, float minInput, float maxInput) {
        return launcher.makeServo(id, minInput, maxInput);
    }

    /**
     * Create an output that will display the current value on the driver
     * station's LCD.
     *
     * @param prefix the prefix, or label, of the output. this is prepended to
     * the value.
     * @param line the line to display the value on, from 1 to 6.
     * @return the output that will write to the LCD.
     */
    protected final FloatOutput makeDSFloatReadout(final String prefix, final int line) {
        return new DSFloatReadout(prefix, line);
    }

    /**
     * Create an output that will display the current value on the driver
     * station's LCD.
     *
     * @param prefix the prefix, or label, of the output. this is prepended to
     * the value.
     * @param line the line to display the value on, from 1 to 6.
     * @return the output that will write to the LCD.
     */
    protected final BooleanOutput makeDSBooleanReadout(final String prefix, final int line) {
        return new DSBooleanReadout(prefix, line);
    }

    /**
     * Display the current value of the specified FloatInputPoll on the driver
     * station's LCD, whenever the specified event is triggered.
     *
     * @param prefix the prefix, or label, of the output. this is prepended to
     * the value.
     * @param line the line to display the value on, from 1 to 6.
     * @param value the value to display.
     * @param when when to update the output.
     */
    protected final void makeDSFloatReadout(String prefix, int line, FloatInputPoll value, EventInput when) {
        FloatMixing.pumpWhen(when, value, makeDSFloatReadout(prefix, line));
    }

    /**
     * Display the current value of the specified BooleanInputPoll on the driver
     * station's LCD, whenever the specified event is triggered.
     *
     * @param prefix the prefix, or label, of the output. this is prepended to
     * the value.
     * @param line the line to display the value on, from 1 to 6.
     * @param value the value to display.
     * @param when when to update the output.
     */
    protected final void makeDSBooleanReadout(String prefix, int line, BooleanInputPoll value, EventInput when) {
        BooleanMixing.pumpWhen(when, value, makeDSBooleanReadout(prefix, line));
    }

    /**
     * Send the specified string to the specified line of the driver station.
     *
     * @param value The string to display.
     * @param line The line number (1-6).
     */
    protected final void sendDSUpdate(String value, int line) {
        launcher.sendDSUpdate(value, line);
    }

    /**
     * Get a boolean input that checks if the robot is currently disabled.
     *
     * @return the input.
     */
    protected final BooleanInputPoll getIsDisabled() {
        return launcher.getIsDisabled();
    }

    /**
     * Get a boolean input that checks if the robot is currently in autonomous,
     * as opposed to teleop and testing.
     *
     * @return the input.
     */
    protected final BooleanInputPoll getIsAutonomous() {
        return launcher.getIsAutonomous();
    }

    /**
     * Get a boolean input that checks if the robot is currently in testing
     * mode, as opposed to teleop and autonomous.
     *
     * @return the input.
     */
    protected final BooleanInputPoll getIsTest() {
        return launcher.getIsTest();
    }

    /**
     * Get a boolean input that checks if the robot is currently in teleop mode,
     * as opposed to testing and autonomous.
     *
     * @return the input.
     */
    protected final BooleanInputPoll getIsTeleop() {
        return BooleanMixing.invert(BooleanMixing.orBooleans(launcher.getIsTest(), launcher.getIsAutonomous()));
    }

    /**
     * Activate the compressor on the given pressure switch channel and
     * compressor relay channel.
     *
     * @param pressureSwitchChannel the channel of the pressure switch digital
     * input.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    protected final void useCompressor(int pressureSwitchChannel, int compressorRelayChannel) {
        useCustomCompressor(makeDigitalInput(pressureSwitchChannel), compressorRelayChannel);
    }

    /**
     * Activate the compressor on the given pressure switch input and compressor
     * relay channel.
     *
     * @param shouldDisable should the compressor be turned off.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    protected final void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        launcher.useCustomCompressor(shouldDisable, compressorRelayChannel);
    }

    public BooleanInputPoll getWhenShouldAutonomousBeRunning() {
        return BooleanMixing.andBooleans(BooleanMixing.invert.wrap(getIsDisabled()), getIsAutonomous());
    }

    public void updatePeriodicallyAlways(EventOutput toUpdate) {
        globalPeriodic.send(toUpdate);
    }

    /**
     * Create a reference to an Encoder on the specified ports with the
     * specified number of average bits.
     *
     * @param aChannel The alpha-channel for the encoder.
     * @param bChannel The beta-channel for the encoder.
     * @param reverse Should the result of the encoder be negated?
     * @param resetWhen If provided, the Encoder's value will be reset when this
     * event is produced.
     * @return the Encoder, reporting encoder ticks.
     */
    protected final FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
        return launcher.makeEncoder(aChannel, bChannel, reverse, resetWhen);
    }

    /**
     * Create a reference to an Encoder on the specified ports with the
     * specified number of average bits.
     *
     * @param aChannel The alpha-channel for the encoder.
     * @param bChannel The beta-channel for the encoder.
     * @param reverse Should the result of the encoder be negated?
     * @return the Encoder, reporting encoder ticks.
     */
    protected final FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse) {
        return launcher.makeEncoder(aChannel, bChannel, reverse, null);
    }

    /**
     * Create a reference to the Forward side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the forward side of the channel.
     */
    protected final BooleanOutput makeForwardRelay(int channel) {
        return launcher.makeRelayForwardOutput(channel);
    }

    /**
     * Create a reference to the Reverse side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the reverse side of the channel.
     */
    protected final BooleanOutput makeReverseRelay(int channel) {
        return launcher.makeRelayReverseOutput(channel);
    }

    /**
     * Create a reference to a Gyro on the specified port with the specified
     * sensitivity. This will allow reading the current rotation of the Gyro.
     * This also takes an EventSource, and when this is fired, the Gyro will be
     * reset.
     *
     * Increased sensitivity means a smaller output for the same turn.
     *
     * @param port The Gyro port number.
     * @param sensitivity The sensitivity of the Gyro. This is the number of
     * volts/degree/second sensitivity of the gyro and is used in calculations
     * to allow the code to work with multiple gyros. 0.007 is a good default
     * value.
     * @param evt When to reset the Gyro.
     * @return The reference to the Gyro's current value.
     */
    protected final FloatInputPoll makeGyro(int port, double sensitivity, EventInput evt) {
        return launcher.makeGyro(port, sensitivity, evt);
    }

    /**
     * Create a reference to a Gyro on the specified port with the specified
     * sensitivity. This will allow reading the current rotation of the Gyro.
     *
     * Increased sensitivity means a smaller output for the same turn.
     *
     * @param port The Gyro port number.
     * @param sensitivity The sensitivity of the Gyro. This is the number of
     * volts/degree/second sensitivity of the gyro and is used in calculations
     * to allow the code to work with multiple gyros. 0.007 is a good default
     * value.
     * @return The reference to the Gyro's current value.
     */
    protected final FloatInputPoll makeGyro(int port, double sensitivity) {
        return launcher.makeGyro(port, sensitivity, null);
    }

    /**
     * Create a reference to a Accelerometer Axis on the specified port, with
     * the specified sensitivity and voltage zero point.
     *
     * @param port The port number to attach to.
     * @param sensitivity The sensitivity of the accelerometer. This varies per
     * model.
     * @param zeropoint The voltage that corresponds to 0 G. This also varies by
     * model.
     * @return The reference to the axis on the Accelerometer.
     */
    protected final FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        return launcher.makeAccelerometerAxis(port, sensitivity, zeropoint);
    }

    private class DSFloatReadout implements FloatOutput {

        private final String prefix;
        private final int line;

        DSFloatReadout(String prefix, int line) {
            this.prefix = prefix;
            this.line = line;
        }

        public void set(float f) {
            sendDSUpdate(prefix + f, line);
        }
    }

    private class DSBooleanReadout implements BooleanOutput {

        private final String prefix;
        private final int line;

        DSBooleanReadout(String prefix, int line) {
            this.prefix = prefix;
            this.line = line;
        }

        public void set(boolean f) {
            sendDSUpdate(prefix + f, line);
        }
    }
}

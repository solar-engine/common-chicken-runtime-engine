/*
 * Copyright 2014-2015 Colby Skeggs
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

import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.SerialIO;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.IJoystick;
import ccre.ctrl.binding.CluckControlBinder;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.ctrl.binding.ControlBindingDataSource;
import ccre.ctrl.binding.ControlBindingDataSourceBuildable;
import ccre.instinct.InstinctModule;
import ccre.timers.Ticker;

/**
 * The main class to access the I/O on a roboRIO.
 *
 * @author skeggsc
 */
public class FRC {

    /**
     * Signifies that the motor should be directly outputted without negation.
     */
    public static final boolean MOTOR_FORWARD = false;
    /**
     * Signifies that the motor should be outputted after negating the value.
     */
    public static final boolean MOTOR_REVERSE = true;
    /**
     * Signifies that no ramping should be applied to this motor.
     */
    public static final float NO_RAMPING = 0.0f;
    /**
     * The battery power channel.
     */
    public static final int POWER_CHANNEL_BATTERY = 0;
    /**
     * The 3.3V rail power channel.
     */
    public static final int POWER_CHANNEL_3V3 = 1;
    /**
     * The 5V rail power channel.
     */
    public static final int POWER_CHANNEL_5V = 2;
    /**
     * The 6V rail power channel.
     */
    public static final int POWER_CHANNEL_6V = 3;

    /**
     * The FRCImplementation providing access to the robot.
     */
    public static final FRCImplementation impl = FRCImplementationHolder.getImplementation();

    /**
     * Joystick 1 on the Driver Station.
     */
    public static final IJoystick joystick1 = impl.getJoystick(1);
    /**
     * Joystick 2 on the Driver Station.
     */
    public static final IJoystick joystick2 = impl.getJoystick(2);
    /**
     * Joystick 3 on the Driver Station.
     */
    public static final IJoystick joystick3 = impl.getJoystick(3);
    /**
     * Joystick 4 on the Driver Station.
     */
    public static final IJoystick joystick4 = impl.getJoystick(4);
    /**
     * Joystick 5 on the Driver Station.
     */
    public static final IJoystick joystick5 = impl.getJoystick(5);
    /**
     * Joystick 6 on the Driver Station.
     */
    public static final IJoystick joystick6 = impl.getJoystick(6);
    /**
     * Produced during every mode if the driver station is attached.
     */
    public static final EventInput globalPeriodic = impl.getGlobalPeriodic();
    /**
     * Constant time periodic. Should pulse every 10 ms.
     */
    public static final EventInput constantPeriodic = new Ticker(10);
    /**
     * Constant time sensor update event. Should pulse every 20 ms. This should
     * be used when you want to poll an on-robot sensor.
     */
    public static final EventInput sensorPeriodic = new Ticker(20);
    /**
     * Produced when the robot enters autonomous mode.
     */
    public static final EventInput startAuto = impl.getStartAuto();
    /**
     * Produced during autonomous mode.
     */
    public static final EventInput duringAuto = impl.getDuringAuto();
    /**
     * Produced when the robot enters teleop mode.
     */
    public static final EventInput startTele = impl.getStartTele();
    /**
     * Produced during teleop mode.
     */
    public static final EventInput duringTele = impl.getDuringTele();
    /**
     * Produced when the robot enters testing mode.
     */
    public static final EventInput startTest = impl.getStartTest();
    /**
     * Produced during testing mode.
     */
    public static final EventInput duringTest = impl.getDuringTest();
    /**
     * Produced when the robot enters disabled mode.
     */
    public static final EventInput startDisabled = impl.getStartDisabled();
    /**
     * Produced while the robot is disabled.
     */
    public static final EventInput duringDisabled = impl.getDuringDisabled();

    /**
     * Create a reference to a Jaguar speed controller on the specified PWM port
     * and motor reversal, with a specified ramping rate.
     *
     * If the ramping rate is zero, then no ramping is applied. Don't use this
     * if you don't know what you're doing! Otherwise, the ramping rate is the
     * maximum difference allowed per 10 milliseconds (constantPeriodic). (So a
     * rate of 0.1f means that you need 200 milliseconds to go from -1.0 to
     * 1.0.)
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @param ramping the ramping rate.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput makeJaguarMotor(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.JAGUAR);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a Jaguar speed controller on the specified PWM port
     * and motor reversal, with a default ramping rate of 0.1, aka 200
     * milliseconds to ramp from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput makeJaguarMotor(int id, boolean negate) {
        return makeJaguarMotor(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Victor speed controller on the specified PWM port
     * and motor reversal, with a specified ramping rate.
     *
     * If the ramping rate is zero, then no ramping is applied. Don't use this
     * if you don't know what you're doing! Otherwise, the ramping rate is the
     * maximum difference allowed per 10 milliseconds (constantPeriodic). (So a
     * rate of 0.1f means that you need 200 milliseconds to go from -1.0 to
     * 1.0.)
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @param ramping the ramping rate.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput makeVictorMotor(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.VICTOR);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a Victor speed controller on the specified PWM port
     * and motor reversal, with a default ramping rate of 0.1, aka 200
     * milliseconds to ramp from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput makeVictorMotor(int id, boolean negate) {
        return makeVictorMotor(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Talon speed controller on the specified PWM port
     * and motor reversal, with a specified ramping rate.
     *
     * If the ramping rate is zero, then no ramping is applied. Don't use this
     * if you don't know what you're doing! Otherwise, the ramping rate is the
     * maximum difference allowed per 10 milliseconds (constantPeriodic). (So a
     * rate of 0.1f means that you need 200 milliseconds to go from -1.0 to
     * 1.0.)
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @param ramping the ramping rate.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput makeTalonMotor(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.TALON);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a Talon speed controller on the specified PWM port
     * and motor reversal, with a default ramping rate of 0.1, aka 200
     * milliseconds to ramp from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput makeTalonMotor(int id, boolean negate) {
        return makeTalonMotor(id, negate, 0.1f);
    }

    /**
     * Create a reference to a CAN Jaguar speed controller with the specified
     * CAN device number. This may, of course, fail, if the Jaguar cannot be
     * found.
     *
     * @param deviceNumber the device number to connect to.
     * @return the ExtendedMotor representing this output.
     */
    public static ExtendedMotor makeCANJaguar(int deviceNumber) {
        LoggerFactory.getLogger(FRC.class).warn("The CCRE CAN Jaguar functionality is NOT yet complete and is UNTESTED! Use with your own risk.");
        return impl.makeCANJaguar(deviceNumber);
    }

    /**
     * Create a reference to a CAN Talon speed controller with the specified CAN
     * device number. This may, of course, fail, if the Talon cannot be found.
     *
     * @param deviceNumber the device number to connect to.
     * @return the ExtendedMotor representing this output.
     */
    public static ExtendedMotor makeCANTalon(int deviceNumber) {
        return impl.makeCANTalon(deviceNumber);
    }

    /**
     * Create a reference to a solenoid on the specified port and the default
     * module.
     *
     * @param id the port of the solenoid.
     * @return the output that will control the solenoid.
     */
    public static BooleanOutput makeSolenoid(int id) {
        return impl.makeSolenoid(0, id);
    }

    /**
     * Create a reference to a solenoid on the specified port and module.
     *
     * @param module the module of the solenoid (PCM on roboRIO)
     * @param id the port of the solenoid.
     * @return the output that will control the solenoid.
     */
    public static BooleanOutput makeSolenoid(int module, int id) {
        return impl.makeSolenoid(module, id);
    }

    /**
     * Create a reference to a digital output on the specified port.
     *
     * @param id the port of the digital output.
     * @return the output that will control the digital output.
     */
    public static BooleanOutput makeDigitalOutput(int id) {
        return impl.makeDigitalOutput(id);
    }

    /**
     * Get a reference to the analog input that reads the current battery
     * voltage, scaled to represent the real battery voltage.
     *
     * @return The current battery voltage.
     */
    public static FloatInput getBatteryVoltage() {
        return impl.getBatteryVoltage(sensorPeriodic);
    }

    /**
     * Get a reference to the analog input that reads the current battery
     * voltage, scaled to represent the real battery voltage.
     *
     * @param updateOn when to update the sensor value.
     * @return The current battery voltage.
     */
    public static FloatInput getBatteryVoltage(EventInput updateOn) {
        return impl.getBatteryVoltage(updateOn);
    }

    /**
     * Create a reference to an analog input on the specified port.
     *
     * @param id the port number.
     * @return the analog input, reporting in voltage.
     */
    public static FloatInput makeAnalogInput(int id) {
        return impl.makeAnalogInput(id, sensorPeriodic);
    }

    /**
     * Create a reference to an analog input on the specified port.
     *
     * @param id the port number.
     * @param updateOn when to update the sensor value.
     * @return the analog input, reporting in voltage.
     */
    public static FloatInput makeAnalogInput(int id, EventInput updateOn) {
        return impl.makeAnalogInput(id, updateOn);
    }

    /**
     * Create a reference to an analog input on the specified port with the
     * specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @return the analog input, reporting in voltage.
     */
    public static FloatInput makeAnalogInput(int id, int averageBits) {
        return impl.makeAnalogInput(id, averageBits, sensorPeriodic);
    }

    /**
     * Create a reference to an analog input on the specified port with the
     * specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @param updateOn when to update the sensor value.
     * @return the analog input, reporting in voltage.
     */
    public static FloatInput makeAnalogInput(int id, int averageBits, EventInput updateOn) {
        return impl.makeAnalogInput(id, averageBits, updateOn);
    }

    /**
     * Create a reference to a digital input on the specified port.
     *
     * @param id the port number.
     * @return the digital input.
     */
    public static BooleanInput makeDigitalInput(int id) {
        return impl.makeDigitalInput(id, sensorPeriodic);
    }

    /**
     * Create a reference to a digital input on the specified port.
     *
     * @param id the port number.
     * @param updateOn when to update the sensor value.
     * @return the digital input.
     */
    public static BooleanInput makeDigitalInput(int id, EventInput updateOn) {
        return impl.makeDigitalInput(id, updateOn);
    }

    /**
     * Create a reference to a digital input on the specified port, as a
     * BooleanInput that updates by using FPGA interrupts or an equivalent.
     * Warning: many systems have a limit on the maximum number of interrupts
     * available - use sparingly!
     *
     * @param id the port number.
     * @return the digital input.
     */
    public static BooleanInput makeDigitalInputByInterrupt(int id) {
        return impl.makeDigitalInputByInterrupt(id);
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
    public static FloatOutput makeServo(int id, float minInput, float maxInput) {
        return impl.makeServo(id, minInput, maxInput);
    }

    /**
     * Get a boolean input that checks if the robot is currently disabled.
     *
     * @return the input.
     */
    public static BooleanInput getIsDisabled() {
        return impl.getIsDisabled();
    }

    /**
     * Get a boolean input that checks if the robot is currently disabled.
     *
     * @return the input.
     */
    public static BooleanInput getIsEnabled() {
        return impl.getIsDisabled().not();
    }

    /**
     * Get a boolean input that checks if the robot is currently in autonomous,
     * as opposed to teleop and testing.
     *
     * @return the input.
     */
    public static BooleanInput getIsAutonomous() {
        return impl.getIsAutonomous();
    }

    /**
     * Get a boolean input that checks if the robot is currently in testing
     * mode, as opposed to teleop and autonomous.
     *
     * @return the input.
     */
    public static BooleanInput getIsTest() {
        return impl.getIsTest();
    }

    /**
     * Get a boolean input that checks if the robot is currently in teleop mode,
     * as opposed to testing and autonomous.
     *
     * @return the input.
     */
    public static BooleanInput getIsTeleop() {
        return impl.getIsTest().or(impl.getIsAutonomous()).not();
    }

    /**
     * Get a boolean input that checks if the robot is currently connected to
     * the FMS, as opposed to being off the playing field.
     *
     * @return the input.
     */
    public static BooleanInput getIsFMS() {
        return impl.getIsFMS();
    }

    /**
     * Activate the compressor on the given pressure switch channel and
     * compressor relay channel.
     *
     * @param pressureSwitchChannel the channel of the pressure switch digital
     * input.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    public static void useCompressor(int pressureSwitchChannel, int compressorRelayChannel) {
        useCustomCompressor(makeDigitalInput(pressureSwitchChannel), compressorRelayChannel);
    }

    /**
     * Get control of the PCM-attached compressor. This makes sure that the PCM
     * compressor is running the closed loop control.
     *
     * @return a BooleanOutput that can turn closed loop control on and off.
     */
    public static BooleanOutput usePCMCompressor() {
        return impl.usePCMCompressor();
    }

    /**
     * Reads the current status of the PCM pressure switch.
     *
     * @return the pressure switch status.
     */
    public static BooleanInput getPCMPressureSwitch() {
        return impl.getPCMPressureSwitch(sensorPeriodic);
    }

    /**
     * Reads the current status of the PCM pressure switch.
     *
     * @param updateOn when to update the sensor value.
     * @return the pressure switch status.
     */
    public static BooleanInput getPCMPressureSwitch(EventInput updateOn) {
        return impl.getPCMPressureSwitch(updateOn);
    }

    /**
     * Reads the current status of the PCM compressor enable output.
     *
     * @return the compressor enable output.
     */
    public static BooleanInput getPCMCompressorRunning() {
        return impl.getPCMCompressorRunning(sensorPeriodic);
    }

    /**
     * Reads the current status of the PCM compressor enable output.
     *
     * @param updateOn when to update the sensor value.
     * @return the compressor enable output.
     */
    public static BooleanInput getPCMCompressorRunning(EventInput updateOn) {
        return impl.getPCMCompressorRunning(updateOn);
    }

    /**
     * Reads the current draw of the PCM compressor.
     *
     * @return the current being used by the compressor.
     */
    public static FloatInput getPCMCompressorCurrent() {
        return impl.getPCMCompressorCurrent(sensorPeriodic);
    }

    /**
     * Reads the current draw of the PCM compressor.
     *
     * @param updateOn when to update the sensor value.
     * @return the current being used by the compressor.
     */
    public static FloatInput getPCMCompressorCurrent(EventInput updateOn) {
        return impl.getPCMCompressorCurrent(updateOn);
    }

    /**
     * Reads the current draw of the specified PDP channel.
     *
     * @param channel the channel to monitor
     * @return the current being used by the specified channel.
     */
    public static FloatInput getPDPChannelCurrent(int channel) {
        return impl.getPDPChannelCurrent(channel, sensorPeriodic);
    }

    /**
     * Reads the current draw of the specified PDP channel.
     *
     * @param channel the channel to monitor
     * @param updateOn when to update the sensor value.
     * @return the current being used by the specified channel.
     */
    public static FloatInput getPDPChannelCurrent(int channel, EventInput updateOn) {
        return impl.getPDPChannelCurrent(channel, updateOn);
    }

    /**
     * Reads the voltage of the PDP.
     *
     * @return the voltage being measured by the PDP.
     */
    public static FloatInput getPDPVoltage() {
        return impl.getPDPVoltage(sensorPeriodic);
    }

    /**
     * Reads the voltage of the PDP.
     *
     * @param updateOn when to update the sensor value.
     * @return the voltage being measured by the PDP.
     */
    public static FloatInput getPDPVoltage(EventInput updateOn) {
        return impl.getPDPVoltage(updateOn);
    }

    /**
     * Reads the voltage from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @return the voltage being measured.
     */
    public static FloatInput getChannelVoltage(int powerChannel) {
        return impl.getChannelVoltage(powerChannel, sensorPeriodic);
    }

    /**
     * Reads the voltage from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @param updateOn when to update the sensor value.
     * @return the voltage being measured.
     */
    public static FloatInput getChannelVoltage(int powerChannel, EventInput updateOn) {
        return impl.getChannelVoltage(powerChannel, updateOn);
    }

    /**
     * Reads the current from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @return the current being measured.
     */
    public static FloatInput getChannelCurrent(int powerChannel) {
        return impl.getChannelCurrent(powerChannel, sensorPeriodic);
    }

    /**
     * Reads the current from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @param updateOn when to update the sensor value.
     * @return the current being measured.
     */
    public static FloatInput getChannelCurrent(int powerChannel, EventInput updateOn) {
        return impl.getChannelCurrent(powerChannel, updateOn);
    }

    /**
     * Checks if the specified power reading channel is enabled.
     *
     * @param powerChannel the power channel to read from.
     * @return if the channel is enabled.
     */
    public static BooleanInput getChannelEnabled(int powerChannel) {
        return impl.getChannelEnabled(powerChannel, sensorPeriodic);
    }

    /**
     * Checks if the specified power reading channel is enabled.
     *
     * @param powerChannel the power channel to read from.
     * @param updateOn when to update the sensor value.
     * @return if the channel is enabled.
     */
    public static BooleanInput getChannelEnabled(int powerChannel, EventInput updateOn) {
        return impl.getChannelEnabled(powerChannel, updateOn);
    }

    /**
     * Activate the compressor on the given pressure switch input and compressor
     * relay channel.
     *
     * @param shouldDisable should the compressor be turned off.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    public static void useCustomCompressor(BooleanInput shouldDisable, int compressorRelayChannel) {
        // TODO: do this without an extra Ticker?
        shouldDisable.send(makeForwardRelay(compressorRelayChannel).invert().limitUpdatesTo(new Ticker(500)));
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
    public static FloatInput makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
        return impl.makeEncoder(aChannel, bChannel, reverse, resetWhen, sensorPeriodic);
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
     * @param updateOn when to update the sensor value.
     * @return the Encoder, reporting encoder ticks.
     */
    public static FloatInput makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen, EventInput updateOn) {
        // TODO: check arguments; similar issue to Gyro
        return impl.makeEncoder(aChannel, bChannel, reverse, resetWhen, updateOn);
    }

    /**
     * Create a reference to the Forward side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the forward side of the channel.
     */
    public static BooleanOutput makeForwardRelay(int channel) {
        return impl.makeRelayForwardOutput(channel);
    }

    /**
     * Create a reference to the Reverse side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the reverse side of the channel.
     */
    public static BooleanOutput makeReverseRelay(int channel) {
        return impl.makeRelayReverseOutput(channel);
    }

    /**
     * Create a reference to a Gyro on the specified port with the specified
     * sensitivity. This will allow reading the current rotation of the Gyro.
     * This also takes an EventInput, and when this is fired, the Gyro will be
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
    public static FloatInput makeGyro(int port, double sensitivity, EventInput evt) {
        return impl.makeGyro(port, sensitivity, evt, sensorPeriodic);
    }

    /**
     * Create a reference to a Gyro on the specified port with the specified
     * sensitivity. This will allow reading the current rotation of the Gyro.
     * This also takes an EventInput, and when this is fired, the Gyro will be
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
     * @param updateOn when to update the sensor value.
     * @return The reference to the Gyro's current value.
     */
    public static FloatInput makeGyro(int port, double sensitivity, EventInput evt, EventInput updateOn) {
        // TODO: Figure out if anything should change about makeGyro's arguments
        // now that I got rid of the no-event versions due to argument list
        // conflicts.
        return impl.makeGyro(port, sensitivity, evt, updateOn);
    }

    /**
     * Register the specified InstinctModule as an autonomous mode. Note that
     * registering multiple autonomous modes probably won't work properly.
     *
     * @param module the InstinctModule to register.
     */
    public static void registerAutonomous(InstinctModule module) {
        module.setShouldBeRunning(getIsEnabled().and(getIsAutonomous()));
    }

    /**
     * Open the onboard serial port of the robot.
     *
     * @param baudRate the baud rate of the port.
     * @param deviceName the name of the device the serial port is connected to
     * (used for debugging and the emulator.)
     * @return a SerialIO interface to the port.
     */
    public static SerialIO makeRS232_Onboard(int baudRate, String deviceName) {
        return impl.makeRS232_Onboard(baudRate, deviceName);
    }

    /**
     * Open the roboRIO's MXP-based serial port.
     *
     * @param baudRate the baud rate of the port.
     * @param deviceName the name of the device the serial port is connected to
     * (used for debugging and the emulator.)
     * @return a SerialIO interface to the port.
     */
    public static SerialIO makeRS232_MXP(int baudRate, String deviceName) {
        return impl.makeRS232_MXP(baudRate, deviceName);
    }

    /**
     * Open a USB-attached serial port on the roboRIO.
     *
     * @param baudRate the baud rate of the port.
     * @param deviceName the name of the device the serial port is connected to
     * (used for debugging and the emulator.)
     * @return a SerialIO interface to the port.
     */
    public static SerialIO makeRS232_USB(int baudRate, String deviceName) {
        return impl.makeRS232_USB(baudRate, deviceName);
    }

    FRC() {
    }

    private static ControlBindingDataSource builtControlSource;

    /**
     * Get a ControlBindingDataSource for the six Joysticks.
     *
     * @return the data source.
     * @see #getControlBindingDataSource(String...) if you want to provide your
     * own names, or use a different number of Joysticks.
     * @see #makeControlBindingCreator(String) if you just want to bind
     * controls.
     */
    public static synchronized ControlBindingDataSource getControlBindingDataSource() {
        if (builtControlSource == null) {
            builtControlSource = getControlBindingDataSource("Joystick 1", "Joystick 2", "Joystick 3", "Joystick 4", "Joystick 5", "Joystick 6");
        }
        return builtControlSource;
    }

    /**
     * Get a ControlBindingCreator that the user can bind, over Cluck, to any
     * Joystick inputs.
     *
     * If you're running in the emulator, and bypassEmulation is false, then the
     * Emulator will skip over including Joysticks at all and just show you your
     * control bindings directly. Much easier to work with!
     *
     * @param name the name of the module that this creator is for. For example,
     * "Drive Code".
     * @param bypassEmulation if the emulator shouldn't try to emulate control
     * bindings directly.
     * @return the ControlBindingCreator that you can make your controls
     * available over.
     */
    public static ControlBindingCreator makeControlBindingCreator(String name, boolean bypassEmulation) {
        if (!bypassEmulation) {
            ControlBindingCreator out = impl.tryMakeControlBindingCreator(name);
            if (out != null) {
                return out;
            }
        }
        return CluckControlBinder.makeCreator(name, getControlBindingDataSource(), impl.getOnInitComplete());
    }

    /**
     * Get a ControlBindingCreator that the user can bind, over Cluck, to any
     * Joystick inputs.
     *
     * If you're running in the emulator, then the Emulator will skip over
     * including Joysticks at all and just show you your control bindings
     * directly. Much easier to work with!
     *
     * @param name the name of the module that this creator is for. For example,
     * "Drive Code".
     * @return the ControlBindingCreator that you can make your controls
     * available over.
     * @see #makeControlBindingCreator(String, boolean) if you want to choose
     * whether or not the emulator emulates control bindings directly.
     */
    public static ControlBindingCreator makeControlBindingCreator(String name) {
        return makeControlBindingCreator(name, false);
    }

    /**
     * This is similar to {@link #getControlBindingDataSource()} but lets you
     * give better names to your Joysticks. For example, you could say
     * <code>FRC.getControlBindingDataSource("Drive Joystick", "Copilot Joystick");</code>
     *
     * @param names the names of the Joysticks to attach to, in order.
     * @return the generated control binding source.
     */
    public static ControlBindingDataSource getControlBindingDataSource(String... names) {
        ControlBindingDataSourceBuildable ds = new ControlBindingDataSourceBuildable();
        for (int i = 0; i < names.length; i++) {
            ds.addJoystick(names[i], impl.getJoystick(i + 1), 12, 6);
        }
        return ds;
    }
}

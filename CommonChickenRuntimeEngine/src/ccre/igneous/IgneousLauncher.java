/*
 * Copyright 2013-2015 Colby Skeggs
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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.SerialIO;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.IJoystick;
import ccre.ctrl.IJoystickWithPOV;
import ccre.ctrl.binding.ControlBindingCreator;

/**
 * This is a launcher for an Igneous application. The reason for this is so that
 * the main program can be ran without a robot. Documentation for all the
 * methods here can be found in IgneousCore, where they are invoked.
 *
 * @author skeggsc
 */
public interface IgneousLauncher {

    /**
     * The ID for a Jaguar speed controller.
     */
    public static final int JAGUAR = 1;
    /**
     * The ID for a Talon speed controller.
     */
    public static final int TALON = 2;
    /**
     * The ID for a Victor speed controller.
     */
    public static final int VICTOR = 3;

    /**
     * Accesses a Joystick from the Driver Station.
     *
     * @param id the Joystick number (1-4)
     * @return the associated joystick on the driver station.
     */
    public IJoystickWithPOV getJoystick(int id);

    /**
     * Get an IJoystick for the specified Kinect virtual joystick.
     *
     * @param isRightArm If the right arm joystick should be used instead of the
     * left. (6 instead of 5 if you're used to the old system.)
     * @return the IJoystick.
     */
    public IJoystick getKinectJoystick(boolean isRightArm);

    /**
     * Create a reference to a speed controller.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param type the motor type: JAGUAR, VICTOR, or TALON.
     * @return the output that will output to the specified motor.
     * @see #JAGUAR
     * @see #VICTOR
     * @see #TALON
     */
    public FloatOutput makeMotor(int id, int type);

    /**
     * Create a reference to a CAN Jaguar.
     *
     * @param deviceNumber the Jaguar's CAN device number.
     * @return the ExtendedMotor for the Jaguar.
     */
    public ExtendedMotor makeCANJaguar(int deviceNumber);

    /**
     * Create a reference to a CAN Talon.
     *
     * @param deviceNumber the Talon's CAN device number.
     * @return the ExtendedMotor for the Talon.
     */
    public ExtendedMotor makeCANTalon(int deviceNumber);

    /**
     * Create a reference to a solenoid on the specified port and module.
     *
     * Module #0 should be interpreted as the default module.
     *
     * @param module the solenoid module (PCM on roboRIO)
     * @param id the port of the solenoid.
     * @return the output that will control the solenoid.
     */
    public BooleanOutput makeSolenoid(int module, int id);

    /**
     * Create a reference to a digital output on the specified port.
     *
     * @param id the port of the digital output.
     * @return the output that will control the digital output.
     */
    public BooleanOutput makeDigitalOutput(int id);

    /**
     * Create a reference to an analog input on the specified port with the
     * default number of average bits.
     *
     * @param id the port number.
     * @return the analog input, reporting in voltage.
     */
    public FloatInputPoll makeAnalogInput(int id);

    /**
     * Create a reference to an analog input on the specified port with the
     * specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @return the analog input, reporting in voltage.
     */
    public FloatInputPoll makeAnalogInput(int id, int averageBits);

    /**
     * Create a reference to an analog input's raw value on the specified port
     * with the specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @return the analog input, reporting in uncalibrated units.
     * @deprecated makeAnalogInput should be used directly because it gives more
     * useful volts instead of a raw value.
     */
    @Deprecated
    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits);

    /**
     * Create a reference to a digital input on the specified port.
     *
     * @param id the port number.
     * @return the digital input.
     */
    public BooleanInputPoll makeDigitalInput(int id);

    /**
     * Create a reference to a digital input on the specified port, as a
     * BooleanInput (not a BooleanInputPoll) that updates by using FPGA
     * interrupts or an equivalent. Warning: many systems have a limit on the
     * maximum number of interrupts available - use sparingly!
     *
     * @param id the port number.
     * @return the digital input.
     */
    public BooleanInput makeDigitalInputByInterrupt(int id);

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
    public FloatOutput makeServo(int id, float minInput, float maxInput);

    /**
     * Send the specified string to the specified line of the driver station.
     *
     * @param value The string to display.
     * @param lineid The line number (1-6).
     */
    public void sendDSUpdate(String value, int lineid);

    /**
     * Get a boolean input that checks if the robot is currently disabled.
     *
     * @return the input.
     */
    public BooleanInputPoll getIsDisabled();

    /**
     * Get a boolean input that checks if the robot is currently in autonomous,
     * as opposed to teleop and testing.
     *
     * @return the input.
     */
    public BooleanInputPoll getIsAutonomous();

    /**
     * Get a boolean input that checks if the robot is currently in testing
     * mode, as opposed to teleop and autonomous.
     *
     * @return the input.
     */
    public BooleanInputPoll getIsTest();

    /**
     * Get a boolean input that checks if the robot is currently connected to
     * the FMS, as opposed to being off the playing field.
     *
     * @return the input.
     */
    public BooleanInputPoll getIsFMS();

    /**
     * Activate the compressor on the given pressure switch input and compressor
     * relay channel.
     *
     * @param shouldDisable should the compressor be turned off.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    public void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel);

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
    public FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen);

    /**
     * Create a reference to the Forward side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the forward side of the channel.
     */
    public BooleanOutput makeRelayForwardOutput(int channel);

    /**
     * Create a reference to the Reverse side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the reverse side of the channel.
     */
    public BooleanOutput makeRelayReverseOutput(int channel);

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
     * @param resetWhen When to reset the Gyro.
     * @return The reference to the Gyro's current value.
     */
    public FloatInputPoll makeGyro(int port, double sensitivity, EventInput resetWhen);

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
     * @deprecated This is literally a subtraction and a division.
     */
    @Deprecated
    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint);

    /**
     * Get a reference to the analog input that reads the current battery
     * voltage, scaled to represent the real battery voltage.
     *
     * @return The current battery voltage.
     */
    public FloatInputPoll getBatteryVoltage();

    /**
     * @return an event that is produced during every mode if the driver station
     * is attached.
     */
    public EventInput getGlobalPeriodic();

    /**
     * @return an event that is produced when the robot enters autonomous mode.
     */
    public EventInput getStartAuto();

    /**
     * @return an event that is produced during autonomous mode.
     */
    public EventInput getDuringAuto();

    /**
     * @return an event that is produced when the robot enters teleop mode.
     */
    public EventInput getStartTele();

    /**
     * @return an event that is produced during teleop mode.
     */
    public EventInput getDuringTele();

    /**
     * @return an event that is produced when the robot enters testing mode.
     */
    public EventInput getStartTest();

    /**
     * @return an event that is produced during testing mode.
     */
    public EventInput getDuringTest();

    /**
     * @return an event that is produced when the robot enters disabled mode.
     */
    public EventInput getStartDisabled();

    /**
     * @return an event that is produced while the robot is disabled.
     */
    public EventInput getDuringDisabled();

    /**
     * @return an output that enables and disabled the PCM compressor's
     * closed-loop control.
     */
    public BooleanOutput usePCMCompressor();

    /**
     * @return the status of the PCM pressure switch.
     */
    public BooleanInputPoll getPCMPressureSwitch();

    /**
     * @return the status of the PCM compressor enable.
     */
    public BooleanInputPoll getPCMCompressorRunning();

    /**
     * @return the current draw of the PCM compressor.
     */
    public FloatInputPoll getPCMCompressorCurrent();

    /**
     * @param channel the channel to monitor.
     * @return the current draw of the specified PDP channel.
     */
    public FloatInputPoll getPDPChannelCurrent(int channel);

    /**
     * @return the voltage measured at the PDP.
     */
    public FloatInputPoll getPDPVoltage();

    /**
     * @return if this system is a roboRIO.
     */
    public boolean isRoboRIO();

    /**
     * @param baudRate the baud rate of the port.
     * @param deviceName the name of the device the serial port is connected to
     * (used for debugging and the emulator.)
     * @return a SerialIO interface to the port.
     */
    public SerialIO makeRS232_Onboard(int baudRate, String deviceName);

    /**
     * @param baudRate the baud rate of the port.
     * @param deviceName the name of the device the serial port is connected to
     * (used for debugging and the emulator.)
     * @return a SerialIO interface to the port.
     */
    public SerialIO makeRS232_MXP(int baudRate, String deviceName);

    /**
     * @param baudRate the baud rate of the port.
     * @param deviceName the name of the device the serial port is connected to
     * (used for debugging and the emulator.)
     * @return a SerialIO interface to the port.
     */
    public SerialIO makeRS232_USB(int baudRate, String deviceName);

    /**
     * @param powerChannel the power channel to monitor.
     * @return the active voltage of the channel.
     */
    public FloatInputPoll getChannelVoltage(int powerChannel);

    /**
     * @param powerChannel the power channel to monitor.
     * @return the active current of the channel.
     */
    public FloatInputPoll getChannelCurrent(int powerChannel);

    /**
     * @param powerChannel the power channel to monitor.
     * @return whether or not the channel is enabled.
     */
    public BooleanInputPoll getChannelEnabled(int powerChannel);

    /**
     * @param name the name of this control binding client.
     * @return a ControlBindingCreator if there is special emulation available,
     * but otherwise just return null.
     */
    public ControlBindingCreator tryMakeControlBindingCreator(String name);

    /**
     * @return an event that is fired once, right after the user program has
     * finished its initialization
     */
    public EventInput getOnInitComplete();
}

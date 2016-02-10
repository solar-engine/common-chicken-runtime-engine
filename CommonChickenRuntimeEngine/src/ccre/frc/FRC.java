/*
 * Copyright 2014-2016 Cel Skeggs
 * Copyright 2015 Jake Springer
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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.SerialIO;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.Joystick;
import ccre.ctrl.binding.CluckControlBinder;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.ctrl.binding.ControlBindingDataSource;
import ccre.ctrl.binding.ControlBindingDataSourceBuildable;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.instinct.InstinctModule;
import ccre.log.Logger;
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
     * Two pulse mode for a counter. The default mode, uses one source to count
     * up and the other to count down.
     */
    public static final int TWO_PULSE_MODE = 0;

    /**
     * Uses the counter to determine the pulse width of a high pulse (rising
     * edge to falling edge) or a low pulse (falling edge to rising edge.)
     * 
     * TODO: implement this fully
     */
    public static final int SEMIPERIOD_MODE = 1;

    /**
     * Pulse length mode for a counter. Uses the pulse width to determine if the
     * counter should count up or down.
     */
    public static final int PULSE_LENGTH_MODE = 2;

    /**
     * External direction mode for a counter. Uses one source to count up and
     * uses one source to determine direction.
     */
    public static final int EXTERNAL_DIRECTION_MODE = 3;

    /**
     * For unused channels for counters.
     */
    public static final int UNUSED = -1;

    /**
     * The FRCImplementation providing access to the robot.
     */
    public static final FRCImplementation impl = FRCImplementationHolder.getImplementation();

    /**
     * Joystick 1 on the Driver Station.
     */
    public static final Joystick joystick1 = impl.getJoystick(1);
    /**
     * Joystick 2 on the Driver Station.
     */
    public static final Joystick joystick2 = impl.getJoystick(2);
    /**
     * Joystick 3 on the Driver Station.
     */
    public static final Joystick joystick3 = impl.getJoystick(3);
    /**
     * Joystick 4 on the Driver Station.
     */
    public static final Joystick joystick4 = impl.getJoystick(4);
    /**
     * Joystick 5 on the Driver Station.
     */
    public static final Joystick joystick5 = impl.getJoystick(5);
    /**
     * Joystick 6 on the Driver Station.
     */
    public static final Joystick joystick6 = impl.getJoystick(6);
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
    public static FloatOutput jaguar(int id, boolean negate, float ramping) {
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
    public static FloatOutput jaguar(int id, boolean negate) {
        return jaguar(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Jaguar speed controller on the specified PWM
     * port, with a default ramping rate of 0.1, aka 200 milliseconds to ramp
     * from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @return the output that will output to the specified motor.
     */
    public static FloatOutput jaguar(int id) {
        return jaguar(id, false, 0.1f);
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
    public static FloatOutput victor(int id, boolean negate, float ramping) {
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
    public static FloatOutput victor(int id, boolean negate) {
        return victor(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Victor speed controller on the specified PWM
     * port, with a default ramping rate of 0.1, aka 200 milliseconds to ramp
     * from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @return the output that will output to the specified motor.
     */
    public static FloatOutput victor(int id) {
        return victor(id, false, 0.1f);
    }

    /**
     * Create a reference to a Talon SR speed controller on the specified PWM
     * port and motor reversal, with a specified ramping rate.
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
    public static FloatOutput talon(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.TALON);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a Talon SR speed controller on the specified PWM
     * port and motor reversal, with a default ramping rate of 0.1, aka 200
     * milliseconds to ramp from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput talon(int id, boolean negate) {
        return talon(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Talon SR speed controller on the specified PWM
     * port, with a default ramping rate of 0.1, aka 200 milliseconds to ramp
     * from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @return the output that will output to the specified motor.
     */
    public static FloatOutput talon(int id) {
        return talon(id, false, 0.1f);
    }

    /**
     * Create a reference to a Talon SRX speed controller on the specified PWM
     * port and motor reversal, with a specified ramping rate.
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
    public static FloatOutput talonSRX(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.TALONSRX);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a Talon SRX speed controller on the specified PWM
     * port and motor reversal, with a default ramping rate of 0.1, aka 200
     * milliseconds to ramp from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput talonSRX(int id, boolean negate) {
        return talonSRX(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Talon SRX speed controller on the specified PWM
     * port, with a default ramping rate of 0.1, aka 200 milliseconds to ramp
     * from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @return the output that will output to the specified motor.
     */
    public static FloatOutput talonSRX(int id) {
        return talonSRX(id, false, 0.1f);
    }

    /**
     * Create a reference to a Victor SP speed controller on the specified PWM
     * port and motor reversal, with a specified ramping rate.
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
    public static FloatOutput victorSP(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.VICTORSP);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a Victor SP speed controller on the specified PWM
     * port and motor reversal, with a default ramping rate of 0.1, aka 200
     * milliseconds to ramp from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_REVERSE
     */
    public static FloatOutput victorSP(int id, boolean negate) {
        return victorSP(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Victor SP speed controller on the specified PWM
     * port, with a default ramping rate of 0.1, aka 200 milliseconds to ramp
     * from stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @return the output that will output to the specified motor.
     */
    public static FloatOutput victorSP(int id) {
        return victorSP(id, false, 0.1f);
    }

    /**
     * Create a reference to a Spark speed controller on the specified PWM port
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
    public static FloatOutput spark(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.SPARK);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a Spark speed controller on the specified PWM port
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
    public static FloatOutput spark(int id, boolean negate) {
        return spark(id, negate, 0.1f);
    }

    /**
     * Create a reference to a Spark speed controller on the specified PWM port,
     * with a default ramping rate of 0.1, aka 200 milliseconds to ramp from
     * stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @return the output that will output to the specified motor.
     */
    public static FloatOutput spark(int id) {
        return spark(id, false, 0.1f);
    }

    /**
     * Create a reference to a SD540 speed controller on the specified PWM port
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
    public static FloatOutput sd540(int id, boolean negate, float ramping) {
        FloatOutput motor = impl.makeMotor(id, FRCImplementation.SD540);
        FloatOutput ramped = (negate ? motor.negate() : motor).addRamping(ramping, constantPeriodic);
        ramped.setWhen(0.0f, startDisabled);
        return ramped;
    }

    /**
     * Create a reference to a SD540 speed controller on the specified PWM port
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
    public static FloatOutput sd540(int id, boolean negate) {
        return sd540(id, negate, 0.1f);
    }

    /**
     * Create a reference to a SD540 speed controller on the specified PWM port,
     * with a default ramping rate of 0.1, aka 200 milliseconds to ramp from
     * stopped to full speed.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @return the output that will output to the specified motor.
     */
    public static FloatOutput sd540(int id) {
        return sd540(id, false, 0.1f);
    }

    /**
     * Create a reference to a CAN Jaguar speed controller with the specified
     * CAN device number. This may, of course, fail, if the Jaguar cannot be
     * found.
     *
     * @param deviceNumber the device number to connect to.
     * @return the ExtendedMotor representing this output.
     */
    public static ExtendedMotor jaguarCAN(int deviceNumber) {
        Logger.warning("The CCRE CAN Jaguar functionality is NOT yet complete and is UNTESTED! Use with your own risk.");
        return impl.makeCANJaguar(deviceNumber);
    }

    /**
     * Create a reference to a CAN Jaguar speed controller with the specified
     * CAN device number. This may, of course, fail, if the Jaguar cannot be
     * found.
     *
     * @param deviceNumber the device number to connect to.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @throws ExtendedMotorFailureException if control cannot be established.
     */
    public static FloatOutput jaguarSimpleCAN(int deviceNumber, boolean negate) throws ExtendedMotorFailureException {
        return jaguarCAN(deviceNumber).simpleControl(negate);
    }

    /**
     * Create a reference to a CAN Talon speed controller with the specified CAN
     * device number. This may, of course, fail, if the Talon cannot be found.
     *
     * @param deviceNumber the device number to connect to.
     * @return the ExtendedMotor representing this output.
     */
    public static TalonExtendedMotor talonCAN(int deviceNumber) {
        return impl.makeCANTalon(deviceNumber);
    }

    /**
     * Create a reference to a CAN Talon speed controller with the specified CAN
     * device number. This may, of course, fail, if the Talon cannot be found.
     *
     * @param deviceNumber the device number to connect to.
     * @param negate MOTOR_FORWARD if the motor direction should be unmodified,
     * MOTOR_REVERSE if the motor direction should be reversed.
     * @return the output that will output to the specified motor.
     * @throws ExtendedMotorFailureException if control cannot be established.
     */
    public static FloatOutput talonSimpleCAN(int deviceNumber, boolean negate) throws ExtendedMotorFailureException {
        return talonCAN(deviceNumber).simpleControl(negate);
    }

    /**
     * Create a reference to a solenoid on the specified port and the default
     * module.
     *
     * @param id the port of the solenoid.
     * @return the output that will control the solenoid.
     */
    public static BooleanOutput solenoid(int id) {
        return impl.makeSolenoid(0, id);
    }

    /**
     * Create a reference to a solenoid on the specified port and module.
     *
     * @param module the module of the solenoid (PCM on roboRIO)
     * @param id the port of the solenoid.
     * @return the output that will control the solenoid.
     */
    public static BooleanOutput solenoid(int module, int id) {
        return impl.makeSolenoid(module, id);
    }

    /**
     * Create a reference to a digital output on the specified port.
     *
     * @param id the port of the digital output.
     * @return the output that will control the digital output.
     */
    public static BooleanOutput digitalOutput(int id) {
        return impl.makeDigitalOutput(id);
    }

    /**
     * Get a reference to the analog input that reads the current battery
     * voltage, scaled to represent the real battery voltage.
     *
     * @return The current battery voltage.
     */
    public static FloatInput batteryVoltage() {
        return impl.getBatteryVoltage(sensorPeriodic);
    }

    /**
     * Get a reference to the analog input that reads the current battery
     * voltage, scaled to represent the real battery voltage.
     *
     * @param updateOn when to update the sensor value.
     * @return The current battery voltage.
     */
    public static FloatInput batteryVoltage(EventInput updateOn) {
        return impl.getBatteryVoltage(updateOn);
    }

    /**
     * Create a reference to an analog input on the specified port.
     *
     * @param id the port number.
     * @return the analog input, reporting in voltage.
     */
    public static FloatInput analogInput(int id) {
        return impl.makeAnalogInput(id, sensorPeriodic);
    }

    /**
     * Create a reference to an analog input on the specified port.
     *
     * @param id the port number.
     * @param updateOn when to update the sensor value.
     * @return the analog input, reporting in voltage.
     */
    public static FloatInput analogInput(int id, EventInput updateOn) {
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
    public static FloatInput analogInput(int id, int averageBits) {
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
    public static FloatInput analogInput(int id, int averageBits, EventInput updateOn) {
        return impl.makeAnalogInput(id, averageBits, updateOn);
    }

    /**
     * Create a reference to a digital input on the specified port.
     *
     * @param id the port number.
     * @return the digital input.
     */
    public static BooleanInput digitalInput(int id) {
        return impl.makeDigitalInput(id, sensorPeriodic);
    }

    /**
     * Create a reference to a digital input on the specified port.
     *
     * @param id the port number.
     * @param updateOn when to update the sensor value.
     * @return the digital input.
     */
    public static BooleanInput digitalInput(int id, EventInput updateOn) {
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
    public static BooleanInput digitalInputByInterrupt(int id) {
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
    public static FloatOutput servo(int id, float minInput, float maxInput) {
        return impl.makeServo(id, minInput, maxInput);
    }

    /**
     * Get a boolean input that checks if the robot is currently disabled.
     *
     * @return the input.
     */
    public static BooleanInput robotDisabled() {
        return impl.getIsDisabled();
    }

    /**
     * Get a boolean input that checks if the robot is currently disabled.
     *
     * @return the input.
     */
    public static BooleanInput robotEnabled() {
        return impl.getIsDisabled().not();
    }

    /**
     * Gets a boolean input that checks if the robot is currently in autonomous,
     * as opposed to teleop and testing.
     *
     * @return the input.
     */
    public static BooleanInput inAutonomousMode() {
        return impl.getIsAutonomous();
    }

    /**
     * Gets a boolean input that checks if the robot is currently in testing
     * mode, as opposed to teleop and autonomous.
     *
     * @return the input.
     */
    public static BooleanInput inTestMode() {
        return impl.getIsTest();
    }

    /**
     * Gets a boolean input that checks if the robot is currently in teleop
     * mode, as opposed to testing and autonomous.
     *
     * @return the input.
     */
    public static BooleanInput inTeleopMode() {
        return impl.getIsTest().or(impl.getIsAutonomous()).not().andNot(impl.getIsDisabled());
    }

    /**
     * Get a boolean input that checks if the robot is currently connected to
     * the FMS, as opposed to being off the playing field.
     *
     * @return the input.
     */
    public static BooleanInput isOnFMS() {
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
    public static void compressor(int pressureSwitchChannel, int compressorRelayChannel) {
        customCompressor(digitalInput(pressureSwitchChannel), compressorRelayChannel);
    }

    /**
     * Get control of the PCM-attached compressor. This makes sure that the PCM
     * compressor is running the closed loop control.
     *
     * @return a BooleanOutput that can turn closed loop control on and off.
     */
    public static BooleanOutput compressorPCM() {
        return impl.usePCMCompressor();
    }

    /**
     * Reads the current status of the PCM pressure switch.
     *
     * @return the pressure switch status.
     */
    public static BooleanInput pressureSwitchPCM() {
        return impl.getPCMPressureSwitch(sensorPeriodic);
    }

    /**
     * Reads the current status of the PCM pressure switch.
     *
     * @param updateOn when to update the sensor value.
     * @return the pressure switch status.
     */
    public static BooleanInput pressureSwitchPCM(EventInput updateOn) {
        return impl.getPCMPressureSwitch(updateOn);
    }

    /**
     * Reads the current status of the PCM compressor enable output.
     *
     * @return the compressor enable output.
     */
    public static BooleanInput compressorRunningPCM() {
        return impl.getPCMCompressorRunning(sensorPeriodic);
    }

    /**
     * Reads the current status of the PCM compressor enable output.
     *
     * @param updateOn when to update the sensor value.
     * @return the compressor enable output.
     */
    public static BooleanInput compressorRunningPCM(EventInput updateOn) {
        return impl.getPCMCompressorRunning(updateOn);
    }

    /**
     * Reads the current draw of the PCM compressor.
     *
     * @return the current being used by the compressor.
     */
    public static FloatInput compressorCurrentPCM() {
        return impl.getPCMCompressorCurrent(sensorPeriodic);
    }

    /**
     * Reads the current draw of the PCM compressor.
     *
     * @param updateOn when to update the sensor value.
     * @return the current being used by the compressor.
     */
    public static FloatInput compressorCurrentPCM(EventInput updateOn) {
        return impl.getPCMCompressorCurrent(updateOn);
    }

    /**
     * Reads the current draw of the entire PDP.
     *
     * @return the current being used by the specified channel.
     */
    public static FloatInput totalCurrentPDP() {
        return impl.getPDPTotalCurrent(sensorPeriodic);
    }

    /**
     * Reads the current draw of the entire PDP.
     *
     * @param updateOn when to update the sensor value.
     * @return the current being used by the specified channel.
     */
    public static FloatInput totalCurrentPDP(EventInput updateOn) {
        return impl.getPDPTotalCurrent(updateOn);
    }

    /**
     * Reads the current draw of the specified PDP channel.
     *
     * @param channel the channel to monitor
     * @return the current being used by the specified channel.
     */
    public static FloatInput channelCurrentPDP(int channel) {
        return impl.getPDPChannelCurrent(channel, sensorPeriodic);
    }

    /**
     * Reads the current draw of the specified PDP channel.
     *
     * @param channel the channel to monitor
     * @param updateOn when to update the sensor value.
     * @return the current being used by the specified channel.
     */
    public static FloatInput channelCurrentPDP(int channel, EventInput updateOn) {
        return impl.getPDPChannelCurrent(channel, updateOn);
    }

    /**
     * Reads the voltage of the PDP.
     *
     * @return the voltage being measured by the PDP.
     */
    public static FloatInput voltagePDP() {
        return impl.getPDPVoltage(sensorPeriodic);
    }

    /**
     * Reads the voltage of the PDP.
     *
     * @param updateOn when to update the sensor value.
     * @return the voltage being measured by the PDP.
     */
    public static FloatInput voltagePDP(EventInput updateOn) {
        return impl.getPDPVoltage(updateOn);
    }

    /**
     * Reads the voltage from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @return the voltage being measured.
     */
    public static FloatInput voltageChannel(int powerChannel) {
        return impl.getChannelVoltage(powerChannel, sensorPeriodic);
    }

    /**
     * Reads the voltage from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @param updateOn when to update the sensor value.
     * @return the voltage being measured.
     */
    public static FloatInput voltageChannel(int powerChannel, EventInput updateOn) {
        return impl.getChannelVoltage(powerChannel, updateOn);
    }

    /**
     * Reads the current from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @return the current being measured.
     */
    public static FloatInput currentChannel(int powerChannel) {
        return impl.getChannelCurrent(powerChannel, sensorPeriodic);
    }

    /**
     * Reads the current from a specified power reading channel.
     *
     * @param powerChannel the power channel to read from.
     * @param updateOn when to update the sensor value.
     * @return the current being measured.
     */
    public static FloatInput currentChannel(int powerChannel, EventInput updateOn) {
        return impl.getChannelCurrent(powerChannel, updateOn);
    }

    /**
     * Checks if the specified power reading channel is enabled.
     *
     * @param powerChannel the power channel to read from.
     * @return if the channel is enabled.
     */
    public static BooleanInput enabledChannel(int powerChannel) {
        return impl.getChannelEnabled(powerChannel, sensorPeriodic);
    }

    /**
     * Checks if the specified power reading channel is enabled.
     *
     * @param powerChannel the power channel to read from.
     * @param updateOn when to update the sensor value.
     * @return if the channel is enabled.
     */
    public static BooleanInput enabledChannel(int powerChannel, EventInput updateOn) {
        return impl.getChannelEnabled(powerChannel, updateOn);
    }

    /**
     * Activate the compressor on the given pressure switch input and compressor
     * relay channel.
     *
     * @param shouldDisable should the compressor be turned off.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    public static void customCompressor(BooleanInput shouldDisable, int compressorRelayChannel) {
        // TODO: do this without an extra Ticker?
        shouldDisable.send(relayForward(compressorRelayChannel).invert().limitUpdatesTo(new Ticker(500)));
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
    public static FloatInput encoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
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
     * @return A FloatInput that represents the count of an encoder
     */
    public static FloatInput encoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen, EventInput updateOn) {
        // TODO: check arguments; similar issue to Gyro
        return impl.makeEncoder(aChannel, bChannel, reverse, resetWhen, updateOn);
    }

    /**
     * Creates a reference to a Counter on the specified ports with the
     * specified number of average bits. Will count up as specified by the mode.
     *
     * @see FRC#EXTERNAL_DIRECTION_MODE
     * @see FRC#PULSE_LENGTH_MODE
     * @see FRC#SEMIPERIOD_MODE
     * @see FRC#TWO_PULSE_MODE
     *
     * @param upChannel the channel for the counter's side that counts up.
     * @param downChannel the channel for the counter's side that counts down.
     * @param mode the pulse tracking mode for the counter.
     * @param resetWhen If provided, the Counter's value will be reset when this
     * event is produced.
     * @param updateOn Updates the FloatInput when this event is produced.
     * @return A FloatInput that represents the count of a counter
     */
    public static FloatInput counter(int upChannel, int downChannel, int mode, EventInput resetWhen, EventInput updateOn) {
        return impl.makeCounter(upChannel, downChannel, resetWhen, updateOn, mode);
    }

    /**
     * Creates a reference to a Counter on the specified ports with the
     * specified number of average bits. Will count as specified by the mode.
     *
     * @see FRC#EXTERNAL_DIRECTION_MODE
     * @see FRC#PULSE_LENGTH_MODE
     * @see FRC#SEMIPERIOD_MODE
     * @see FRC#TWO_PULSE_MODE
     *
     * @param upChannel the channel for the counter's side that counts up.
     * @param downChannel the channel for the counter's side that counts down.
     * @param mode the pulse tracking mode for the counter.
     * @param resetWhen If provided, the Counter's value will be reset when this
     * event is produced.
     * @return A FloatInput that represents the count of a counter
     */
    public static FloatInput counter(int upChannel, int downChannel, int mode, EventInput resetWhen) {
        return impl.makeCounter(upChannel, downChannel, resetWhen, sensorPeriodic, mode);
    }

    /**
     * Creates a reference to a Counter on the specified ports with the
     * specified number of average bits. Will count as specified by
     * TWO_PULSE_MODE.
     *
     * @see FRC#TWO_PULSE_MODE
     *
     * @param upChannel the channel for the counter's side that counts up.
     * @param downChannel the channel for the counter's side that counts down.
     * @param resetWhen If provided, the Counter's value will be reset when this
     * event is produced.
     * @param updateOn Updates the FloatInput when this event is produced.
     * @return A FloatInput that represents the count of a counter
     */
    public static FloatInput counter(int upChannel, int downChannel, EventInput resetWhen, EventInput updateOn) {
        return impl.makeCounter(upChannel, downChannel, resetWhen, updateOn, TWO_PULSE_MODE);
    }

    /**
     * Creates a reference to a Counter on the specified ports. Will count as
     * specified by TWO_PULSE_MODE.
     *
     * @see FRC#TWO_PULSE_MODE
     *
     * @param upChannel the channel for the counter's side that counts up.
     * @param downChannel the channel for the counter's side that counts down.
     * @param resetWhen If provided, the Counter's value will be reset when this
     * event is produced.
     * @return A FloatInput that represents the count of a counter
     */
    public static FloatInput counter(int upChannel, int downChannel, EventInput resetWhen) {
        return impl.makeCounter(upChannel, downChannel, resetWhen, sensorPeriodic, TWO_PULSE_MODE);
    }

    /**
     * Create a reference to the Forward side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the forward side of the channel.
     */
    public static BooleanOutput relayForward(int channel) {
        return impl.makeRelayForwardOutput(channel);
    }

    /**
     * Create a reference to the Reverse side of the relay on the specified
     * channel - this side can be turned on and off.
     *
     * @param channel The relay channel.
     * @return the output that will modify the reverse side of the channel.
     */
    public static BooleanOutput relayReverse(int channel) {
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
    public static FloatInput gyro(int port, double sensitivity, EventInput evt) {
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
    public static FloatInput gyro(int port, double sensitivity, EventInput evt, EventInput updateOn) {
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
        module.setShouldBeRunning(robotEnabled().and(inAutonomousMode()));
    }

    /**
     * Open the onboard serial port of the robot.
     *
     * @param baudRate the baud rate of the port.
     * @param deviceName the name of the device the serial port is connected to
     * (used for debugging and the emulator.)
     * @return a SerialIO interface to the port.
     */
    public static SerialIO onboardRS232(int baudRate, String deviceName) {
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
    public static SerialIO mxpRS232(int baudRate, String deviceName) {
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
    public static SerialIO usbRS232(int baudRate, String deviceName) {
        return impl.makeRS232_USB(baudRate, deviceName);
    }

    FRC() {
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
    public static ControlBindingCreator controlBinding(String name, boolean bypassEmulation) {
        if (!bypassEmulation) {
            ControlBindingCreator out = impl.tryMakeControlBindingCreator(name);
            if (out != null) {
                return out;
            }
        }
        return CluckControlBinder.makeCreator(name, controlBindingSource(), impl.getOnInitComplete());
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
     * @see #controlBinding(String, boolean) if you want to choose whether or
     * not the emulator emulates control bindings directly.
     */
    public static ControlBindingCreator controlBinding(String name) {
        return controlBinding(name, false);
    }

    private static ControlBindingCreator creator;

    /**
     * Get a ControlBindingCreator that the user can bind, over Cluck, to any
     * Joystick inputs.
     *
     * If you're running in the emulator, then the Emulator will skip over
     * including Joysticks at all and just show you your control bindings
     * directly. Much easier to work with!
     *
     * @return the ControlBindingCreator that you can make your controls
     * available over.
     * @see #controlBinding(String) if you want to change the name of the
     * module.
     */
    public static ControlBindingCreator controlBinding() {
        if (creator == null) {
            creator = controlBinding("Robot");
        }
        return creator;
    }

    private static ControlBindingDataSource builtControlSource;

    /**
     * Get a ControlBindingDataSource for the six Joysticks.
     *
     * @return the data source.
     * @see #controlBindingSource(String...) if you want to provide your own
     * names, or use a different number of Joysticks.
     * @see #controlBinding(String) if you just want to bind controls.
     */
    public static synchronized ControlBindingDataSource controlBindingSource() {
        if (builtControlSource == null) {
            builtControlSource = controlBindingSource("Joystick 1", "Joystick 2", "Joystick 3", "Joystick 4", "Joystick 5", "Joystick 6");
        }
        return builtControlSource;
    }

    /**
     * This is similar to {@link #controlBindingSource()} but lets you give
     * better names to your Joysticks. For example, you could say
     * <code>FRC.getControlBindingDataSource("Drive Joystick", "Copilot Joystick");</code>
     *
     * @param names the names of the Joysticks to attach to, in order.
     * @return the generated control binding source.
     */
    public static ControlBindingDataSource controlBindingSource(String... names) {
        ControlBindingDataSourceBuildable ds = new ControlBindingDataSourceBuildable();
        for (int i = 0; i < names.length; i++) {
            ds.addJoystick(names[i], impl.getJoystick(i + 1), 12, 6);
        }
        return ds;
    }
}

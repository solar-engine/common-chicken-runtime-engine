package ccre.igneous;

import ccre.chan.*;
import ccre.cluck.CluckGlobals;
import ccre.ctrl.*;
import ccre.event.Event;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.net.IgneousNetworkProvider;
import ccre.saver.IgneousStorageProvider;
import ccre.workarounds.IgneousThrowablePrinter;
import edu.wpi.first.wpilibj.*;
import java.io.IOException;

/**
 * A Core class for Igneous. Extend this (or SimpleCore, which is easier) in
 * order to write an application to run on the robot.
 *
 * @see SimpleCore
 * @author skeggsc
 */
public abstract class IgneousCore extends IterativeRobot {

    static {
        CluckGlobals.ensureInitializedCore();
        IgneousNetworkProvider.register();
        IgneousThrowablePrinter.register();
        IgneousStorageProvider.register();
    }

    /**
     * Implement this method - it should set up everything that your robot needs
     * to do.
     */
    protected abstract void createRobotControl();
    // Default events
    /**
     * Produced during every state where the driver station is attached.
     */
    protected Event globalPeriodic = new Event();

    public final void robotInit() {
        try {
            CluckGlobals.initializeServer(80);
        } catch (IOException ex) {
            Logger.log(LogLevel.SEVERE, "Could not start Cluck server!", ex);
        }
        createRobotControl();
    }
    /**
     * Produced when the robot enters autonomous mode.
     */
    protected Event startedAutonomous = new Event();

    public final void autonomousInit() {
        startedAutonomous.produce();
    }
    /**
     * Produced during autonomous mode.
     */
    protected Event duringAutonomous = new Event();

    public final void autonomousPeriodic() {
        duringAutonomous.produce();
        globalPeriodic.produce();
    }
    /**
     * Produced when the robot enters disabled mode.
     */
    protected Event robotDisabled = new Event();

    public final void disabledInit() {
        robotDisabled.produce();
    }
    /**
     * Produced while the robot is disabled.
     */
    protected Event duringDisabled = new Event();

    public final void disabledPeriodic() {
        duringDisabled.produce();
        globalPeriodic.produce();
    }
    /**
     * Produced when the robot enters teleop mode.
     */
    protected Event startedTeleop = new Event();

    public final void teleopInit() {
        Logger.finer("Start teleop dispatch");
        startedTeleop.produce();
    }
    /**
     * Produced during teleop mode.
     */
    protected Event duringTeleop = new Event();

    public final void teleopPeriodic() {
        duringTeleop.produce();
        globalPeriodic.produce();
    }
    /**
     * Produced when the robot enters testing mode.
     */
    protected Event startedTesting = new Event();

    public final void testInit() {
        startedTesting.produce();
    }
    /**
     * Produced during testing mode.
     */
    protected Event duringTesting = new Event();

    public final void testPeriodic() {
        duringTesting.produce();
    }

    // Factory methods
    /**
     * Get an ISimpleJoystick for the specified joystick ID.
     *
     * @param id the joystick ID, from 1 to 4, inclusive.
     * @return the ISimpleJoystick.
     * @see #makeDispatchJoystick(int)
     */
    protected final ISimpleJoystick makeSimpleJoystick(int id) {
        return new CSimpleJoystick(id);
    }

    /**
     * Get an IDispatchJoystick for the specified joystick ID. The joystick will
     * update the inputs and events during teleop mode only.
     *
     * This is equivalent to
     * <code>makeDispatchJoystick(id, duringTeleop)</code>.
     *
     * @param id the joystick ID, from 1 to 4, inclusive.
     * @return the IDispatchJoystick.
     * @see #makeSimpleJoystick(int)
     * @see #makeDispatchJoystick(int, ccre.event.EventSource)
     */
    protected final IDispatchJoystick makeDispatchJoystick(int id) {
        return new CDispatchJoystick(id, duringTeleop);
    }

    /**
     * Get an IDispatchJoystick for the specified joystick ID. The joystick will
     * update the inputs and events when the specified event is fired.
     *
     * @param id the joystick ID, from 1 to 4, inclusive.
     * @param source when to update the joystick.
     * @return the IDispatchJoystick.
     * @see #makeSimpleJoystick(int)
     * @see #makeDispatchJoystick(int)
     */
    protected IDispatchJoystick makeDispatchJoystick(int id, EventSource source) {
        return new CDispatchJoystick(id, source);
    }
    /**
     * Signifies that the motor should be directly outputted without negation.
     */
    public static final boolean MOTOR_FORWARD = false;
    /**
     * Signifies that the motor should be outputted after negating the value.
     */
    public static final boolean MOTOR_REVERSE = true;

    /**
     * Create a reference to a Jaguar speed controller on the specified ID and
     * motor reversal.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor should be unmodified,
     * MOTOR_REVERSE if the motor should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_FREVERSE
     */
    protected FloatOutput makeJaguarMotor(int id, boolean negate) {
        return wrapSpeedController(new Jaguar(id), negate);
    }

    /**
     * Create a reference to a Victor speed controller on the specified ID and
     * motor reversal.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor should be unmodified,
     * MOTOR_REVERSE if the motor should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_FREVERSE
     */
    protected FloatOutput makeVictorMotor(int id, boolean negate) {
        return wrapSpeedController(new Victor(id), negate);
    }

    /**
     * Create a reference to a Talon speed controller on the specified ID and
     * motor reversal.
     *
     * @param id the motor port ID, from 1 to 10, inclusive.
     * @param negate MOTOR_FORWARD if the motor should be unmodified,
     * MOTOR_REVERSE if the motor should be reversed.
     * @return the output that will output to the specified motor.
     * @see #MOTOR_FORWARD
     * @see #MOTOR_FREVERSE
     */
    protected FloatOutput makeTalonMotor(int id, boolean negate) {
        return wrapSpeedController(new Talon(id), negate);
    }

    /**
     * Create a reference to a solenoid on the specified port.
     *
     * @param id the port of the solenoid.
     * @return the output that will control the solenoid.
     */
    protected BooleanOutput makeSolenoid(int id) {
        final Solenoid sol = new Solenoid(id);
        return new BooleanOutput() {
            public void writeValue(boolean bln) {
                sol.set(bln);
            }
        };
    }

    /**
     * Create a reference to an analog input on the specified port with the
     * specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @return the analog input, reporting in voltage.
     */
    protected FloatInputPoll makeAnalogInput(int id, int averageBits) {
        final AnalogChannel chan = new AnalogChannel(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float readValue() {
                return (float) chan.getAverageVoltage();
            }
        };
    }

    /**
     * Create a reference to an analog input's raw value on the specified port
     * with the specified number of average bits.
     *
     * @param id the port number.
     * @param averageBits the number of averaging bits.
     * @return the analog input, reporting in uncalibrated units.
     */
    protected FloatInputPoll makeAnalogInput_ValueBased(int id, int averageBits) {
        final AnalogChannel chan = new AnalogChannel(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float readValue() {
                return (float) chan.getAverageValue();
            }
        };
    }

    /**
     * Create a reference to a digital input on the spcefiied port.
     *
     * @param id the port number.
     * @return the digital input.
     */
    protected BooleanInputPoll makeDigitalInput(int id) {
        final DigitalInput dinput = new DigitalInput(id);
        return new BooleanInputPoll() {
            public boolean readValue() {
                return dinput.get();
            }
        };
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
     * @return
     */
    protected FloatOutput makeServo(int id, final float minInput, float maxInput) {
        final Servo servo = new Servo(id);
        final float deltaInput = maxInput - minInput;
        return new FloatOutput() {
            public void writeValue(float f) {
                servo.set((f - minInput) / deltaInput);
            }
        };
    }

    /**
     * Create an output that will display the current value on the driver
     * station's LCD.
     *
     * @param prefix the prefix, or label, of the output. this is prepended to
     * the value.
     * @param line the line to display the value on.
     * @return the output that will write to the LCD.
     */
    protected FloatOutput makeDSFloatReadout(final String prefix, final DriverStationLCD.Line line) {
        return new FloatOutput() {
            public void writeValue(float f) {
                DriverStationLCD dslcd = DriverStationLCD.getInstance();
                dslcd.println(line, 1, "                    ");
                dslcd.println(line, 1, prefix + f);
                dslcd.updateLCD();
            }
        };
    }

    /**
     * Display the current value of the specified FloatInputPoll on the driver
     * station's LCD, whenever the specified event is triggered.
     *
     * @param prefix the prefix, or label, of the output. this is prepended to
     * the value.
     * @param line the line to display the value on.
     * @param value the value to display.
     * @param when when to update the output.
     */
    protected void makeDSFloatReadout(String prefix, DriverStationLCD.Line line, FloatInputPoll value, EventSource when) {
        Mixing.pumpWhen(when, value, makeDSFloatReadout(prefix, line));
    }

    /**
     * Get a boolean input that checks if the robot is currently disabled.
     *
     * @return the input.
     */
    protected BooleanInputPoll getIsDisabled() {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return DriverStation.getInstance().isDisabled();
            }
        };
    }

    /**
     * Get a boolean input that checks if the robot is currently in autonomous,
     * as opposed to teleop.
     *
     * @return the input.
     */
    protected BooleanInputPoll getIsAutonomous() {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return DriverStation.getInstance().isAutonomous();
            }
        };
    }

    /**
     * Return a FloatOutput that writes to the specified speed controller.
     *
     * @param spc
     * @param negate
     * @return
     */
    static FloatOutput wrapSpeedController(final SpeedController spc, final boolean negate) {
        return new FloatOutput() {
            public void writeValue(float f) {
                if (negate) {
                    spc.set(-f);
                } else {
                    spc.set(f);
                }
            }
        };
    }
    /**
     * The robot's compressor.
     */
    private CCustomCompressor compressor;

    /**
     * Activate the compressor on the given pressure switch channel and
     * compressor relay channel.
     *
     * @param pressureSwitchChannel the channel of the pressure switch digital
     * input.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    protected void useCompressor(int pressureSwitchChannel, int compressorRelayChannel) {
        if (compressor == null) {
            compressor = new CCustomCompressor(makeDigitalInput(pressureSwitchChannel), compressorRelayChannel);
            compressor.start();
        } else {
            throw new IllegalStateException("Compressor already started!");
        }
    }

    /**
     * Activate the compressor on the given pressure switch input and
     * compressor relay channel.
     *
     * @param shouldDisable should the compressor be turned off.
     * @param compressorRelayChannel the channel of the compressor's relay.
     */
    protected void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        if (compressor == null) {
            compressor = new CCustomCompressor(shouldDisable, compressorRelayChannel);
            compressor.start();
        } else {
            throw new IllegalStateException("Compressor already started!");
        }
    }
}

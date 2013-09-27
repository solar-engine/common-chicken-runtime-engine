/*
 * Copyright 2013 Colby Skeggs
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

import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.cluck.CluckGlobals;
import ccre.ctrl.IDispatchJoystick;
import ccre.ctrl.ISimpleJoystick;
import ccre.event.Event;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.log.MultiTargetLogger;
import ccre.net.IgneousNetworkProvider;
import ccre.saver.IgneousStorageProvider;
import ccre.workarounds.IgneousThrowablePrinter;
import com.sun.squawk.VM;
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.DriverStationLCD.Line;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;
import java.io.IOException;

/**
 * The Squawk implementation of the IgneousLauncher interface. Do not use this!
 * This should only be referenced from the MANIFEST.MF file.
 *
 * @see IgneousLauncher
 * @author skeggsc
 */
class IgneousLauncherImpl extends IterativeRobot implements IgneousLauncher {

    /**
     * The robot's core program.
     */
    public final IgneousCore core;
    /**
     * The robot's compressor.
     */
    private CCustomCompressor compressor;

    IgneousLauncherImpl() {
        IgneousNetworkProvider.register();
        IgneousThrowablePrinter.register();
        IgneousStorageProvider.register();
        CluckGlobals.ensureInitializedCore();
        Logger.target = new MultiTargetLogger(new LoggingTarget[]{Logger.target, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger")});
        String name = VM.getManifestProperty("Igneous-Main");
        if (name == null) {
            throw new RuntimeException("Could not find MANIFEST-specified launchee!");
        }
        try {
            core = (IgneousCore) Class.forName(name).newInstance();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Could not load " + name + ": " + ex);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Could not load " + name + ": " + ex);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Could not load " + name + ": " + ex);
        }
    }
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
        core.duringAutonomous = this.duringAutonomous;
        core.duringDisabled = this.duringDisabled;
        core.duringTeleop = this.duringTeleop;
        core.duringTesting = this.duringTesting;
        core.globalPeriodic = this.globalPeriodic;
        core.robotDisabled = this.robotDisabled;
        core.startedAutonomous = this.startedAutonomous;
        core.startedTeleop = this.startedTeleop;
        core.startedTesting = this.startedTesting;
        core.launcher = this;
        core.createRobotControl();
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
        globalPeriodic.produce();
    }

    /**
     * Return a FloatOutput that writes to the specified speed controller.
     *
     * @param spc the speed controller
     * @param negate if the motor direction should be negated. See MOTOR_FORWARD
     * and MOTOR_REVERSE.
     * @return the FloatOutput that writes to the controller.
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

    public ISimpleJoystick makeSimpleJoystick(int id) {
        return new CSimpleJoystick(id);
    }

    public IDispatchJoystick makeDispatchJoystick(int id, EventSource source) {
        return new CDispatchJoystick(id, source);
    }

    public FloatOutput makeJaguar(int id, boolean negate) {
        return wrapSpeedController(new Jaguar(id), negate);
    }

    public FloatOutput makeVictor(int id, boolean negate) {
        return wrapSpeedController(new Victor(id), negate);
    }

    public FloatOutput makeTalon(int id, boolean negate) {
        return wrapSpeedController(new Talon(id), negate);
    }

    public BooleanOutput makeSolenoid(int id) {
        final Solenoid sol = new Solenoid(id);
        return new BooleanOutput() {
            public void writeValue(boolean bln) {
                sol.set(bln);
            }
        };
    }

    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        final AnalogChannel chan = new AnalogChannel(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float readValue() {
                return (float) chan.getAverageVoltage();
            }
        };
    }

    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        final AnalogChannel chan = new AnalogChannel(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float readValue() {
                return (float) chan.getAverageValue();
            }
        };
    }

    public BooleanInputPoll makeDigitalInput(int id) {
        final DigitalInput dinput = new DigitalInput(id);
        return new BooleanInputPoll() {
            public boolean readValue() {
                return dinput.get();
            }
        };
    }

    public FloatOutput makeServo(int id, final float minInput, float maxInput) {
        final Servo servo = new Servo(id);
        final float deltaInput = maxInput - minInput;
        return new FloatOutput() {
            public void writeValue(float f) {
                servo.set((f - minInput) / deltaInput);
            }
        };
    }

    public FloatOutput makeDSFloatReadout(final String prefix, final int lineid) {
        final Line line;
        switch (lineid) {
            case 1:
                line = DriverStationLCD.Line.kUser1;
                break;
            case 2:
                line = DriverStationLCD.Line.kUser2;
                break;
            case 3:
                line = DriverStationLCD.Line.kUser3;
                break;
            case 4:
                line = DriverStationLCD.Line.kUser4;
                break;
            case 5:
                line = DriverStationLCD.Line.kUser5;
                break;
            case 6:
                line = DriverStationLCD.Line.kUser6;
                break;
            default:
                throw new IllegalArgumentException("Bad line number (expected 1-6): " + lineid);
        }
        return new FloatOutput() {
            public void writeValue(float f) {
                DriverStationLCD dslcd = DriverStationLCD.getInstance();
                dslcd.println(line, 1, "                    ");
                dslcd.println(line, 1, prefix + f);
                dslcd.updateLCD();
            }
        };
    }

    public BooleanInputPoll getIsDisabled() {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return DriverStation.getInstance().isDisabled();
            }
        };
    }

    public BooleanInputPoll getIsAutonomous() {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return DriverStation.getInstance().isAutonomous();
            }
        };
    }

    public void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        if (compressor == null) {
            compressor = new CCustomCompressor(shouldDisable, compressorRelayChannel);
            compressor.start();
        } else {
            throw new IllegalStateException("Compressor already started!");
        }
    }
}

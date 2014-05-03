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

import ccre.channel.*;
import ccre.cluck.CluckGlobals;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.ctrl.*;
import ccre.log.*;
import ccre.net.IgneousNetworkProvider;
import ccre.saver.IgneousStorageProvider;
import ccre.workarounds.IgneousThrowablePrinter;
import com.sun.squawk.VM;
import edu.wpi.first.wpilibj.*;

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

    IgneousLauncherImpl() {
        IgneousNetworkProvider.register();
        IgneousThrowablePrinter.register();
        IgneousStorageProvider.register();
        NetworkAutologger.register();
        BootLogger.register();
        FileLogger.register();
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
    protected EventStatus globalPeriodic = new EventStatus();

    public final void robotInit() {
        //CluckGlobals.setupServer() - No longer helpful on the robot because this port is now used by default.
        new CluckTCPServer(CluckGlobals.getNode(), 443).start();
        core.duringAutonomous = this.duringAutonomous;
        core.duringDisabled = this.duringDisabled;
        core.duringTeleop = this.duringTeleop;
        core.duringTesting = this.duringTesting;
        core.globalPeriodic = this.globalPeriodic;
        core.constantPeriodic = new Ticker(10, true);
        core.robotDisabled = this.robotDisabled;
        core.startedAutonomous = this.startedAutonomous;
        core.startedTeleop = this.startedTeleop;
        core.startedTesting = this.startedTesting;
        core.joystick1 = new CJoystick(1, globalPeriodic);
        core.joystick2 = new CJoystick(2, globalPeriodic);
        core.joystick3 = new CJoystick(3, globalPeriodic);
        core.joystick4 = new CJoystick(4, globalPeriodic);
        core.launcher = this;
        try {
            core.createRobotControl();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Disabled Init", thr);
            if (thr instanceof RuntimeException) {
                throw (RuntimeException) thr;
            } else if (thr instanceof Error) {
                throw (Error) thr;
            }
            throw new RuntimeException("Critical Code Failure: " + thr.getMessage());
        }
    }
    /**
     * Produced when the robot enters autonomous mode.
     */
    protected EventStatus startedAutonomous = new EventStatus();

    public final void autonomousInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began autonomous on FMS" : "Began autonomous mode");
            startedAutonomous.produce();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Autonomous Init", thr);
        }
    }
    /**
     * Produced during autonomous mode.
     */
    protected EventStatus duringAutonomous = new EventStatus();
    private int countFails = 0;

    public final void autonomousPeriodic() {
        try {
            if (countFails >= 50) {
                countFails--;
                if (duringAutonomous.produceWithFailureRecovery()) {
                    countFails = 0;
                }
                if (globalPeriodic.produceWithFailureRecovery()) {
                    countFails = 0;
                }
            } else {
                duringAutonomous.produce();
                globalPeriodic.produce();
                if (countFails > 0) {
                    countFails--;
                }
            }
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Autonomous Periodic", thr);
            countFails += 10;
        }
    }
    /**
     * Produced when the robot enters disabled mode.
     */
    protected EventStatus robotDisabled = new EventStatus();

    public final void disabledInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began disabled on FMS" : "Began disabled mode");
            robotDisabled.produce();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Disabled Init", thr);
        }
    }
    /**
     * Produced while the robot is disabled.
     */
    protected EventStatus duringDisabled = new EventStatus();

    public final void disabledPeriodic() {
        try {
            if (countFails >= 50) {
                countFails--;
                if (duringDisabled.produceWithFailureRecovery()) {
                    countFails = 0;
                }
                if (globalPeriodic.produceWithFailureRecovery()) {
                    countFails = 0;
                }
            } else {
                duringDisabled.produce();
                globalPeriodic.produce();
                if (countFails > 0) {
                    countFails--;
                }
            }
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Disabled Periodic", thr);
            countFails += 10;
        }
    }
    /**
     * Produced when the robot enters teleop mode.
     */
    protected EventStatus startedTeleop = new EventStatus();

    public final void teleopInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began teleop on FMS" : "Began teleop mode");
            startedTeleop.produce();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Teleop Init", thr);
        }
    }
    /**
     * Produced during teleop mode.
     */
    protected EventStatus duringTeleop = new EventStatus();

    public final void teleopPeriodic() {
        try {
            if (countFails >= 50) {
                countFails--;
                if (duringTeleop.produceWithFailureRecovery()) {
                    countFails = 0;
                }
                if (globalPeriodic.produceWithFailureRecovery()) {
                    countFails = 0;
                }
            } else {
                duringTeleop.produce();
                globalPeriodic.produce();
                if (countFails > 0) {
                    countFails--;
                }
            }
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Teleop Periodic", thr);
            countFails += 10;
        }
    }
    /**
     * Produced when the robot enters testing mode.
     */
    protected EventStatus startedTesting = new EventStatus();

    public final void testInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began testing on FMS (?????)" : "Began testing mode");
            startedTesting.produce();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Testing Init", thr);
        }
    }
    /**
     * Produced during testing mode.
     */
    protected EventStatus duringTesting = new EventStatus();

    public final void testPeriodic() {
        try {
            if (countFails >= 50) {
                countFails--;
                if (duringTesting.produceWithFailureRecovery()) {
                    countFails = 0;
                }
                if (globalPeriodic.produceWithFailureRecovery()) {
                    countFails = 0;
                }
            } else {
                duringTesting.produce();
                globalPeriodic.produce();
                if (countFails > 0) {
                    countFails--;
                }
            }
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Critical Code Failure in Testing Periodic", thr);
            countFails += 10;
        }
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
            public void set(float f) {
                if (negate) {
                    spc.set(-f);
                } else {
                    spc.set(f);
                }
            }
        };
    }

    public IJoystick getKinectJoystick(boolean isRightStick) {
        return new CJoystick(isRightStick ? 6 : 5, globalPeriodic);
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
            public void set(boolean bln) {
                sol.set(bln);
            }
        };
    }

    public BooleanOutput makeDigitalOutput(int id) {
        final DigitalOutput dout = new DigitalOutput(id);
        return new BooleanOutput() {
            public void set(boolean bln) {
                dout.set(bln);
            }
        };
    }

    public FloatInputPoll getBatteryVoltage() {
        return new FloatInputPoll() {
            DriverStation d = DriverStation.getInstance();

            public float get() {
                return (float) d.getBatteryVoltage();
            }
        };
    }

    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        final AnalogChannel chan = new AnalogChannel(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float get() {
                return (float) chan.getAverageVoltage();
            }
        };
    }

    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        final AnalogChannel chan = new AnalogChannel(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float get() {
                return (float) chan.getAverageValue();
            }
        };
    }

    public BooleanInputPoll makeDigitalInput(int id) {
        final DigitalInput dinput = new DigitalInput(id);
        return new BooleanInputPoll() {
            public boolean get() {
                return dinput.get();
            }
        };
    }

    public FloatOutput makeServo(int id, final float minInput, float maxInput) {
        final Servo servo = new Servo(id);
        final float deltaInput = maxInput - minInput;
        return new FloatOutput() {
            public void set(float f) {
                servo.set((f - minInput) / deltaInput);
            }
        };
    }

    public void sendDSUpdate(String value, int lineid) {
        final DriverStationLCD.Line line;
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
        DriverStationLCD dslcd = DriverStationLCD.getInstance();
        dslcd.println(line, 1, "                    ");
        dslcd.println(line, 1, value);
        dslcd.updateLCD();
    }

    public BooleanInputPoll getIsDisabled() {
        return new BooleanInputPoll() {
            public boolean get() {
                return DriverStation.getInstance().isDisabled();
            }
        };
    }

    public BooleanInputPoll getIsAutonomous() {
        return new BooleanInputPoll() {
            public boolean get() {
                DriverStation is = DriverStation.getInstance();
                return is.isAutonomous() && !is.isTest();
            }
        };
    }

    public BooleanInputPoll getIsTest() {
        return new BooleanInputPoll() {
            public boolean get() {
                return DriverStation.getInstance().isTest();
            }
        };
    }

    public void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        BooleanOutput relay = makeRelayForwardOutput(compressorRelayChannel);
        Mixing.pumpWhen(new Ticker(500), Mixing.invert(shouldDisable), relay); // TODO: Test this code.
    }

    public FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
        final Encoder enc = new Encoder(aChannel, bChannel, reverse);
        enc.start();
        if (resetWhen != null) {
            resetWhen.send(new EventOutput() {
                public void event() {
                    enc.reset();
                }
            });
        }
        return new FloatInputPoll() {
            public float get() {
                return enc.get();
            }
        };
    }

    public BooleanOutput makeRelayForwardOutput(int channel) {
        final Relay r = new Relay(channel, Relay.Direction.kForward);
        return new BooleanOutput() {
            public void set(boolean bln) {
                r.set(bln ? Relay.Value.kOn : Relay.Value.kOff);
            }
        };
    }

    public BooleanOutput makeRelayReverseOutput(int channel) {
        final Relay r = new Relay(channel, Relay.Direction.kReverse);
        return new BooleanOutput() {
            public void set(boolean bln) {
                r.set(bln ? Relay.Value.kOn : Relay.Value.kOff);
            }
        };
    }

    public FloatInputPoll makeGyro(int port, double sensitivity, EventInput evt) {
        final Gyro g = new Gyro(port);
        g.setSensitivity(sensitivity);
        if (evt != null) {
            evt.send(new EventOutput() {
                public void event() {
                    g.reset();
                }
            });
        }
        return new FloatInputPoll() {
            public float get() {
                return (float) g.getAngle();
            }
        };
    }

    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        final Accelerometer a = new Accelerometer(port);
        a.setSensitivity(sensitivity);
        a.setZero(zeropoint);
        return new FloatInputPoll() {
            public float get() {
                return (float) a.getAcceleration();
            }
        };
    }
}

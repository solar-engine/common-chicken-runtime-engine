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

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.IJoystick;
import ccre.ctrl.Ticker;
import ccre.log.BootLogger;
import ccre.log.FileLogger;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;
import ccre.saver.DefaultStorageProvider;
import edu.wpi.first.wpilibj.AnalogAccelerometer;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;

/**
 * The Squawk implementation of the IgneousLauncher interface. Do not use this!
 * This should only be referenced from the MANIFEST.MF file.
 *
 * @see IgneousLauncher
 * @author skeggsc
 */
public final class IgneousLauncherImpl extends IterativeRobot implements IgneousLauncher {

    /**
     * Produced during every state where the driver station is attached.
     */
    private final EventStatus globalPeriodic = new EventStatus();

    /**
     * Produced when the robot enters autonomous mode.
     */
    private final EventStatus startedAutonomous = new EventStatus();

    /**
     * Produced during autonomous mode.
     */
    private final EventStatus duringAutonomous = new EventStatus();

    /**
     * The number of recent code failures - used to determine when to get
     * annoyed and start detaching broken code modules.
     */
    private int countFails = 0;

    /**
     * Produced when the robot enters disabled mode.
     */
    private final EventStatus startDisabled = new EventStatus();

    /**
     * Produced while the robot is disabled.
     */
    private final EventStatus duringDisabled = new EventStatus();

    /**
     * Produced when the robot enters teleop mode.
     */
    private final EventStatus startedTeleop = new EventStatus();

    /**
     * Produced during teleop mode.
     */
    private final EventStatus duringTeleop = new EventStatus();

    /**
     * Produced when the robot enters testing mode.
     */
    private final EventStatus startedTesting = new EventStatus();

    /**
     * Produced during testing mode.
     */
    private final EventStatus duringTesting = new EventStatus();

    /**
     * Initialized by usePCMCompressor if needed.
     */
    private Compressor pcmCompressor;

    public IgneousLauncherImpl() {
        File rootDir = new File("/home/lvuser/ccre-storage");
        rootDir.mkdirs();
        DefaultStorageProvider.register(rootDir);
        NetworkAutologger.register();
        BootLogger.register();
        FileLogger.register();
    }

    public void robotInit() {
        IgneousLauncherHolder.setLauncher(this);
        //CluckGlobals.setupServer() - No longer helpful on the robot because this port is now used by default.
        new CluckTCPServer(Cluck.getNode(), 443).start();
        new CluckTCPServer(Cluck.getNode(), 1540).start();
        try {
            setupMain();
        } catch (RuntimeException ex) {
            Logger.severe("Critical Code Failure in Robot Init", ex);
            throw ex;
        } catch (Error err) {
            Logger.severe("Critical Code Failure in Robot Init", err);
            throw err;
        } catch (Throwable thr) {
            Logger.severe("Critical Code Failure in Robot Init", thr);
            throw new RuntimeException("Critical Code Failure: " + thr.getMessage());
        }
    }

    private void setupMain() throws Throwable {
        Enumeration<URL> resources = IgneousLauncherImpl.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        String name = null;
        while (resources != null && resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            name = manifest.getMainAttributes().getValue("CCRE-Main");
        }
        if (name == null) {
            throw new RuntimeException("Could not find MANIFEST-specified launchee!");
        }
        ((IgneousApplication) Class.forName(name).newInstance()).setupRobot();
    }

    public void autonomousInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began autonomous on FMS" : "Began autonomous mode");
            startedAutonomous.produce();
        } catch (Throwable thr) {
            Logger.severe("Critical Code Failure in Autonomous Init", thr);
        }
    }

    public void autonomousPeriodic() {
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
            Logger.severe("Critical Code Failure in Autonomous Periodic", thr);
            countFails += 10;
        }
    }

    public void disabledInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began disabled on FMS" : "Began disabled mode");
            startDisabled.produce();
        } catch (Throwable thr) {
            Logger.severe("Critical Code Failure in Disabled Init", thr);
        }
    }

    public void disabledPeriodic() {
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
            Logger.severe("Critical Code Failure in Disabled Periodic", thr);
            countFails += 10;
        }
    }

    public void teleopInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began teleop on FMS" : "Began teleop mode");
            startedTeleop.produce();
        } catch (Throwable thr) {
            Logger.severe("Critical Code Failure in Teleop Init", thr);
        }
    }

    public void teleopPeriodic() {
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
            Logger.severe("Critical Code Failure in Teleop Periodic", thr);
            countFails += 10;
        }
    }

    public void testInit() {
        try {
            Logger.fine(DriverStation.getInstance().isFMSAttached() ? "Began testing on FMS (?????)" : "Began testing mode");
            startedTesting.produce();
        } catch (Throwable thr) {
            Logger.severe("Critical Code Failure in Testing Init", thr);
        }
    }

    public void testPeriodic() {
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
            Logger.severe("Critical Code Failure in Testing Periodic", thr);
            countFails += 10;
        }
    }

    public IJoystick getKinectJoystick(boolean isRightStick) {
        return new CJoystick(isRightStick ? 6 : 5).attach(globalPeriodic);
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
            private final DriverStation d = DriverStation.getInstance();

            public float get() {
                return (float) d.getBatteryVoltage();
            }
        };
    }

    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        final AnalogInput chan = new AnalogInput(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float get() {
                return (float) chan.getAverageVoltage();
            }
        };
    }

    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        final AnalogInput chan = new AnalogInput(id);
        chan.setAverageBits(averageBits);
        return new FloatInputPoll() {
            public float get() {
                return chan.getAverageValue();
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
        Logger.warning("The Driver Station LCD no longer exists!");
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
        BooleanMixing.pumpWhen(new Ticker(500), BooleanMixing.invert(shouldDisable), relay); // TODO: Test this code.
    }

    public FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
        final Encoder enc = new Encoder(aChannel, bChannel, reverse);
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
        final AnalogAccelerometer a = new AnalogAccelerometer(port);
        a.setSensitivity(sensitivity);
        a.setZero(zeropoint);
        return new FloatInputPoll() {
            public float get() {
                return (float) a.getAcceleration();
            }
        };
    }

    public IJoystick getJoystick(int id) {
        return new CJoystick(id).attach(globalPeriodic);
    }

    public FloatOutput makeMotor(int id, int type) {
        final SpeedController spc;
        switch (type) {
        case JAGUAR:
            spc = new Jaguar(id);
            break;
        case VICTOR:
            spc = new Victor(id);
            break;
        case TALON:
            spc = new Talon(id);
            break;
        default:
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return new FloatOutput() {
            public void set(float f) {
                spc.set(f);
            }
        };
    }

    public EventInput getGlobalPeriodic() {
        return globalPeriodic;
    }

    public EventInput getStartAuto() {
        return startedAutonomous;
    }

    public EventInput getDuringAuto() {
        return duringAutonomous;
    }

    public EventInput getStartTele() {
        return startedTeleop;
    }

    public EventInput getDuringTele() {
        return duringTeleop;
    }

    public EventInput getStartTest() {
        return startedTesting;
    }

    public EventInput getDuringTest() {
        return duringTesting;
    }

    public EventInput getStartDisabled() {
        return startDisabled;
    }

    public EventInput getDuringDisabled() {
        return duringDisabled;
    }

    private synchronized Compressor getPCMCompressor() {
        if (pcmCompressor == null) {
            pcmCompressor = new Compressor();
        }
        return pcmCompressor;
    }

    public BooleanOutput usePCMCompressor() {
        getPCMCompressor().setClosedLoopControl(true);
        return new BooleanOutput() {
            public void set(boolean value) {
                getPCMCompressor().setClosedLoopControl(value);
            }
        };
    }

    public BooleanInputPoll getPCMPressureSwitch() {
        return new BooleanInputPoll() {
            public boolean get() {
                return getPCMCompressor().getPressureSwitchValue();
            }
        };
    }

    public BooleanInputPoll getPCMCompressorRunning() {
        return new BooleanInputPoll() {
            public boolean get() {
                return getPCMCompressor().enabled();
            }
        };
    }

    public FloatInputPoll getPCMCompressorCurrent() {
        return new FloatInputPoll() {
            public float get() {
                return getPCMCompressor().getCompressorCurrent();
            }
        };
    }
}

/*
 * Copyright 2013-2015 Colby Skeggs
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
 *
 *
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.frc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.jar.Manifest;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanCell;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.DerivedFloatInput;
import ccre.channel.EventInput;
import ccre.channel.EventCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.SerialIO;
import ccre.cluck.Cluck;
import ccre.concurrency.ReporterThread;
import ccre.ctrl.CommunicationFailureExtendedMotor;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.Joystick;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.log.FileLogger;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;
import ccre.storage.Storage;
import ccre.util.Version;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tInstances;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tResourceType;
import edu.wpi.first.wpilibj.communication.HALControlWord;

/**
 * The RoboRIO implementation of the FRCImplementation interface. Do not use
 * this! This should only be referenced from the build script.
 *
 * @see FRCImplementation
 * @author skeggsc
 */
public final class DirectFRCImplementation implements FRCImplementation {
    /**
     * The entry point for the Direct robot implementation.
     *
     * @param args the program arguments. ignored.
     */
    public static void main(String[] args) {
        FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationReserve();

        try {
            File version = new File("/tmp/frc_versions/FRC_Lib_Version.ini");
            if (version.exists()) {
                version.delete();
            }
            try (FileOutputStream output = new FileOutputStream(version)) {
                output.write(("CCRE " + Version.getShortVersion() + ": 2015 Java 1.0.0").getBytes("UTF-8"));
            }
        } catch (IOException ex) {
            Logger.warning("Could not write version file", ex);
        }

        try {
            runMain();
            Logger.severe("Robots don't quit!");
        } catch (Throwable t) {
            t.printStackTrace();
            Logger.severe("Uncaught exception!", t);
        }
        System.exit(1);
    }

    private static void runMain() {
        Logger.info("I am a CCRE-powered robot with version " + Version.getVersion() + "!");
        DirectFRCImplementation robot = new DirectFRCImplementation();
        FRCImplementationHolder.setImplementation(robot);
        // Cluck de-facto off-FMS port.
        Cluck.setupServer(1540);
        // SmartDashboard port, since it's unused with the CCRE
        Cluck.setupServer(1735);
        // First team-use port.
        Cluck.setupServer(5800);
        // Another team-use port.
        Cluck.setupServer(5805);
        try {
            robot.setupMain();
            Cluck.getNode().notifyNetworkModified();
        } catch (Throwable thr) {
            Logger.severe("Critical Code Failure in Robot Init", thr);
            return;
        }

        DirectDriverStation.init();

        FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationObserveUserProgramStarting();

        robot.mainloop();
    }

    private void mainloop() {
        activeMode = null;

        while (true) {
            HALControlWord controlWord = FRCNetworkCommunicationsLibrary.HALGetControlWord();
            onFMS = controlWord.getFMSAttached();
            Mode newmode = calcMode(controlWord);
            if (newmode != activeMode) {
                activeMode = newmode;
                activeMode.start(this, controlWord.getFMSAttached());
            }
            if (DirectDriverStation.isNewControlData()) {
                switch (activeMode) {
                case DISABLED:
                    FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationObserveUserProgramDisabled();
                    break;
                case AUTONOMOUS:
                    FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationObserveUserProgramAutonomous();
                    break;
                case TELEOP:
                    FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationObserveUserProgramTeleop();
                    break;
                case TEST:
                    FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationObserveUserProgramTest();
                    break;
                }
                activeMode.periodic(this);
            }
            try {
                DirectDriverStation.waitForData();
            } catch (InterruptedException e) {
                Logger.warning("Core thread interrupted... ignoring.", e);
            }
        }
    }

    private static final EventCell globalPeriodic = new EventCell();

    /**
     * Initialized by usePCMCompressor if needed.
     */
    private ByteBuffer pcmCompressor;

    private Mode activeMode = Mode.DISABLED;

    private boolean onFMS = false;

    private final EventCell[] startEvents, duringEvents;

    private final EventCell onInitComplete = new EventCell();

    private final EventCell onChangeMode = new EventCell();

    {
        int count = Mode.values().length;
        startEvents = new EventCell[count];
        duringEvents = new EventCell[count];
        for (int i = 0; i < count; i++) {
            startEvents[i] = new EventCell();
            duringEvents[i] = new EventCell();
        }
    }

    /**
     * Create and initialize a new DirectFRCImplementation.
     */
    public DirectFRCImplementation() {
        FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationUsageReportingReport((byte) tResourceType.kResourceType_Language, (byte) tInstances.kLanguage_Java, (byte) 0, "With the CCRE: the CommonChickenRuntimeEngine");
        File rootDir = new File("/home/lvuser/ccre-storage");
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            Logger.warning("Could not create rootDir! Something might break...");
        }
        Storage.setBaseDir(rootDir);
        NetworkAutologger.register();
        FileLogger.register();
    }

    private enum Mode {
        DISABLED("disabled"), AUTONOMOUS("autonomous"), TELEOP("teleop"), TEST("test");

        private Mode(String name) {
            this.name = name;
        }

        public final String name;

        private EventCell getStart(DirectFRCImplementation impl) {
            return impl.startEvents[ordinal()];
        }

        private EventCell getDuring(DirectFRCImplementation impl) {
            return impl.duringEvents[ordinal()];
        }

        private void start(DirectFRCImplementation impl, boolean onFMS) {
            try {
                Logger.fine("Began " + name + (onFMS ? " on FMS" : " mode"));
                impl.onChangeMode.event();
                getStart(impl).event();
            } catch (Throwable thr) {
                Logger.severe("Critical Code Failure in " + name + " init", thr);
            }
        }

        private void periodic(DirectFRCImplementation impl) {
            getDuring(impl).safeEvent();
            globalPeriodic.safeEvent();
        }
    }

    private Mode calcMode(HALControlWord controlWord) {
        if (!controlWord.getEnabled() || !controlWord.getDSAttached()) {
            return Mode.DISABLED;
        } else if (controlWord.getTest()) {
            return Mode.TEST;
        } else if (controlWord.getAutonomous()) {
            return Mode.AUTONOMOUS;
        } else {
            return Mode.TELEOP;
        }
    }

    private void setupMain() throws Throwable {
        Enumeration<URL> resources = DirectFRCImplementation.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        String name = null;
        while (resources != null && resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            name = manifest.getMainAttributes().getValue("CCRE-Main");
        }
        if (name == null) {
            throw new RuntimeException("Could not find MANIFEST-specified launchee!");
        }
        Logger.info("Starting application: " + name);
        ((FRCApplication) Class.forName(name).newInstance()).setupRobot();
        onInitComplete.event();
        Logger.info("Hello, " + name + "!");
    }

    @Override
    public BooleanOutput makeSolenoid(int module, int id) {
        final ByteBuffer port = DirectSolenoid.init(module, id);
        return value -> DirectSolenoid.set(port, value);
    }

    @Override
    public BooleanOutput makeDigitalOutput(int id) {
        DirectDigital.init(id, false);
        return value -> DirectDigital.set(id, value);
    }

    @Override
    public FloatInput makeAnalogInput(int id, EventInput updateOn) {
        ByteBuffer port = DirectAnalog.init(id);
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectAnalog.getAverageVoltage(port);
            }
        };
    }

    @Override
    public FloatInput makeAnalogInput(int id, int averageBits, EventInput updateOn) {
        ByteBuffer port = DirectAnalog.init(id);
        DirectAnalog.configure(port, averageBits, 0);// TODO: oversample bits
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectAnalog.getAverageVoltage(port);
            }
        };
    }

    @Override
    public BooleanInput makeDigitalInput(int id, EventInput updateOn) {
        DirectDigital.init(id, true);
        return new DerivedBooleanInput(updateOn) {
            @Override
            protected boolean apply() {
                return DirectDigital.get(id);
            }
        };
    }

    @Override
    public BooleanInput makeDigitalInputByInterrupt(int id) {
        DirectDigital.init(id, true);
        DirectDigital.initInterruptsSynchronous(id, true, true);
        BooleanCell out = new BooleanCell(DirectDigital.get(id));
        new ReporterThread("Interrupt-Handler") {
            @Override
            protected void threadBody() {
                while (true) {
                    // TODO: use this return value for optimization
                    boolean n = DirectDigital.waitForInterrupt(id, 10.0f, false);
                    out.safeSet(DirectDigital.get(id));
                }
            }
        }.start();
        return out;
    }

    @Override
    public FloatOutput makeServo(int id, final float minInput, float maxInput) {
        if (minInput == maxInput) {
            throw new IllegalArgumentException("Servos cannot have their extrema be the same!");
        }
        DirectPWM.init(id, DirectPWM.TYPE_SERVO);
        return f -> DirectPWM.set(id, (f - minInput) / (maxInput - minInput));
    }

    @Override
    public BooleanInput getIsDisabled() {
        return new DerivedBooleanInput(onChangeMode) {
            @Override
            protected boolean apply() {
                return activeMode == Mode.DISABLED;
            }
        };
    }

    @Override
    public BooleanInput getIsAutonomous() {
        return new DerivedBooleanInput(onChangeMode) {
            @Override
            protected boolean apply() {
                return activeMode == Mode.AUTONOMOUS;
            }
        };
    }

    @Override
    public BooleanInput getIsTest() {
        return new DerivedBooleanInput(onChangeMode) {
            @Override
            protected boolean apply() {
                return activeMode == Mode.TEST;
            }
        };
    }

    @Override
    public BooleanInput getIsFMS() {
        return new DerivedBooleanInput(onChangeMode) {
            @Override
            protected boolean apply() {
                return onFMS;
            }
        };
    }

    @Override
    public FloatInput makeEncoder(int channelA, int channelB, boolean reverse, EventInput resetWhen, EventInput updateOn) {
        ByteBuffer encoder = DirectEncoder.init(channelA, channelB, reverse);
        if (resetWhen != null) {
            resetWhen.send(() -> DirectEncoder.reset(encoder));
        }
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectEncoder.get(encoder);
            }
        };
    }

    @Override
    public FloatInput makeCounter(int channelUp, int channelDown, EventInput resetWhen, EventInput updateOn, int mode) {
        // unused happens to be -1, but this makes more sense than comparing with -1
        if (channelUp == FRC.UNUSED && channelDown == FRC.UNUSED) {
            Logger.warning("Neither channelUp nor channelDown was provided to makeCounter.");
            return FloatInput.zero;
        }

        if (channelUp != FRC.UNUSED && (channelUp < 0 || channelUp >= DirectDigital.DIGITAL_PINS)) {
            throw new RuntimeException("Invalid up channel: " + channelUp);
        }
        if (channelDown != FRC.UNUSED && (channelDown < 0 || channelDown >= DirectDigital.DIGITAL_PINS)) {
            throw new RuntimeException("Invalid down channel: " + channelDown);
        }

        ByteBuffer counter = DirectCounter.init(channelUp, channelDown, mode);
        if (resetWhen != null) {
            resetWhen.send(() -> {
                DirectCounter.clearDownSource(counter);
                DirectCounter.clearUpSource(counter);
            });
        }
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectCounter.get(counter);
            }
        };
    }

    @Override
    public BooleanOutput makeRelayForwardOutput(int channel) {
        ByteBuffer relay = DirectRelay.init(channel);
        return (bln) -> DirectRelay.setForward(relay, bln);
    }

    @Override
    public BooleanOutput makeRelayReverseOutput(int channel) {
        ByteBuffer relay = DirectRelay.init(channel);
        return (bln) -> DirectRelay.setReverse(relay, bln);
    }

    @Override
    public FloatInput makeGyro(int port, double sensitivity, EventInput evt, EventInput updateOn) {
        ByteBuffer gyro;
        try {
            gyro = DirectGyro.init(port);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted during Gyro calibration", e);
        }
        if (evt != null) {
            evt.send(() -> DirectGyro.reset(gyro));
        }
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectGyro.getAngle(gyro, port, sensitivity);
            }
        };
    }

    @Override
    public FloatOutput makeMotor(int id, int type) {
        switch (type) {
        case JAGUAR:
            DirectPWM.init(id, DirectPWM.TYPE_JAGUAR);
            break;
        case VICTOR:
            DirectPWM.init(id, DirectPWM.TYPE_VICTOR);
            break;
        case TALON:
            DirectPWM.init(id, DirectPWM.TYPE_TALON);
            break;
        default:
            throw new IllegalArgumentException("Unknown motor type: " + type);
        }
        return (f) -> DirectPWM.set(id, f);
    }

    @Override
    public EventInput getGlobalPeriodic() {
        return globalPeriodic;
    }

    @Override
    public EventInput getStartAuto() {
        return Mode.AUTONOMOUS.getStart(this);
    }

    @Override
    public EventInput getDuringAuto() {
        return Mode.AUTONOMOUS.getDuring(this);
    }

    @Override
    public EventInput getStartTele() {
        return Mode.TELEOP.getStart(this);
    }

    @Override
    public EventInput getDuringTele() {
        return Mode.TELEOP.getDuring(this);
    }

    @Override
    public EventInput getStartTest() {
        return Mode.TEST.getStart(this);
    }

    @Override
    public EventInput getDuringTest() {
        return Mode.TEST.getDuring(this);
    }

    @Override
    public EventInput getStartDisabled() {
        return Mode.DISABLED.getStart(this);
    }

    @Override
    public EventInput getDuringDisabled() {
        return Mode.DISABLED.getDuring(this);
    }

    private synchronized ByteBuffer getPCMCompressor() {
        if (pcmCompressor == null) {
            // TODO: Provide all PCM ids
            pcmCompressor = DirectCompressor.init(0);
        }
        return pcmCompressor;
    }

    @Override
    public BooleanOutput usePCMCompressor() {
        DirectCompressor.setClosedLoop(getPCMCompressor(), true);
        return (on) -> DirectCompressor.setClosedLoop(getPCMCompressor(), on);
    }

    @Override
    public BooleanInput getPCMPressureSwitch(EventInput updateOn) {
        getPCMCompressor();
        return new DerivedBooleanInput(updateOn) {
            @Override
            protected boolean apply() {
                return DirectCompressor.getPressureSwitch(getPCMCompressor());
            }
        };
    }

    @Override
    public BooleanInput getPCMCompressorRunning(EventInput updateOn) {
        getPCMCompressor();
        return new DerivedBooleanInput(updateOn) {
            @Override
            protected boolean apply() {
                return DirectCompressor.getCompressorRunning(getPCMCompressor());
            }
        };
    }

    @Override
    public FloatInput getPCMCompressorCurrent(EventInput updateOn) {
        getPCMCompressor();
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectCompressor.getCompressorCurrent(getPCMCompressor());
            }
        };
    }

    // TODO: Add the rest of the PCM and PDP accessors.

    @Override
    public FloatInput getPDPChannelCurrent(final int channel, EventInput updateOn) {
        DirectPDP.checkChannel(channel);
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectPDP.getCurrent(channel);
            }
        };
    }

    @Override
    public FloatInput getPDPVoltage(EventInput updateOn) {
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectPDP.getVoltage();
            }
        };
    }

    @Override
    public SerialIO makeRS232_Onboard(int baudRate, String deviceName) {
        return new SerialPortDirect(DirectRS232.PORT_ONBOARD, baudRate);
    }

    @Override
    public SerialIO makeRS232_MXP(int baudRate, String deviceName) {
        return new SerialPortDirect(DirectRS232.PORT_MXP, baudRate);
    }

    @Override
    public SerialIO makeRS232_USB(int baudRate, String deviceName) {
        return new SerialPortDirect(DirectRS232.PORT_USB, baudRate);
    }

    @Override
    public Joystick getJoystick(int id) {
        if (id < 1 || id > 6) {
            throw new IllegalArgumentException("Joystick " + id + " is not a valid joystick number.");
        }
        return new CJoystickDirect(id, globalPeriodic);
    }

    @Override
    public FloatInput getBatteryVoltage(EventInput updateOn) {
        DirectPower.init();
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectPower.getBatteryVoltage();
            }
        };
    }

    @Override
    public ExtendedMotor makeCANJaguar(int deviceNumber) {
        try {
            return new ExtendedJaguarDirect(deviceNumber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during CAN Jaguar initialization");
        } catch (ExtendedMotorFailureException ex) {
            Logger.severe("Could not connect to CAN Jaguar " + deviceNumber, ex);
            return new CommunicationFailureExtendedMotor("Could not connect to CAN Jaguar " + deviceNumber);
        }
    }

    @Override
    public ExtendedMotor makeCANTalon(int deviceNumber) {
        try {
            return new ExtendedTalonDirect(deviceNumber);
        } catch (ExtendedMotorFailureException e) {
            Logger.severe("Could not connect to CAN Talon " + deviceNumber, e);
            return new CommunicationFailureExtendedMotor("Could not connect to CAN Talon " + deviceNumber);
        }
    }

    @Override
    public FloatInput getChannelVoltage(int powerChannel, EventInput updateOn) {
        if (DirectPower.readChannelVoltage(powerChannel) == -1) {
            Logger.warning("Unknown power channel: " + powerChannel);
        }
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectPower.readChannelVoltage(powerChannel);
            }
        };
    }

    @Override
    public FloatInput getChannelCurrent(int powerChannel, EventInput updateOn) {
        if (DirectPower.readChannelCurrent(powerChannel) == -1) {
            Logger.warning("Unknown power channel: " + powerChannel);
        }
        return new DerivedFloatInput(updateOn) {
            @Override
            protected float apply() {
                return DirectPower.readChannelCurrent(powerChannel);
            }
        };
    }

    @Override
    public BooleanInput getChannelEnabled(int powerChannel, EventInput updateOn) {
        return new DerivedBooleanInput(updateOn) {
            @Override
            protected boolean apply() {
                return DirectPower.readChannelEnabled(powerChannel);
            }
        };
    }

    @Override
    public ControlBindingCreator tryMakeControlBindingCreator(String title) {
        return null;
    }

    @Override
    public EventInput getOnInitComplete() {
        return onInitComplete;
    }
}

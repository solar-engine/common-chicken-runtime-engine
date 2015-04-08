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
 *
 *
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.igneous.direct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.jar.Manifest;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.SerialIO;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.CommunicationFailureExtendedMotor;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;
import ccre.ctrl.IJoystickWithPOV;
import ccre.ctrl.Ticker;
import ccre.igneous.IgneousApplication;
import ccre.igneous.IgneousLauncher;
import ccre.igneous.IgneousLauncherHolder;
import ccre.log.BootLogger;
import ccre.log.FileLogger;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;
import ccre.saver.DefaultStorageProvider;
import ccre.util.Version;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tInstances;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tResourceType;
import edu.wpi.first.wpilibj.communication.HALControlWord;

/**
 * The RoboRIO implementation of the IgneousLauncher interface. Do not use this!
 * This should only be referenced from the build script.
 *
 * @see IgneousLauncher
 * @author skeggsc
 */
public final class DirectIgneousLauncherImpl implements IgneousLauncher {
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
            version.createNewFile();
            try (FileOutputStream output = new FileOutputStream(version)) {
                output.write(("CCRE " + Version.getShortVersion() + ": 2015 Java 1.0.0").getBytes());
            }
        } catch (IOException ex) {
            Logger.warning("Could not write version file", ex);
        }

        try {
            runMain();
            Logger.severe("Robots don't quit!");
        } catch (Throwable t) {
            t.printStackTrace();
            Logger.severe("Launcher-uncaught exception!", t);
        }
        System.exit(1);
    }

    private static void runMain() {
        DirectIgneousLauncherImpl robot = new DirectIgneousLauncherImpl();
        IgneousLauncherHolder.setLauncher(robot);
        Cluck.setupServer(1540); // Cluck de-facto off-FMS port.
        Cluck.setupServer(1735); // SmartDashboard port, since it's unused with the CCRE
        Cluck.setupServer(5800); // First team-use port.
        Cluck.setupServer(5805); // Another team-use port.
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

    private static final EventStatus globalPeriodic = new EventStatus();

    /**
     * The number of recent code failures - used to determine when to get
     * annoyed and start detaching broken code modules.
     */
    private static int countFails = 0;

    /**
     * Initialized by usePCMCompressor if needed.
     */
    private ByteBuffer pcmCompressor;

    private Mode activeMode = Mode.DISABLED;

    private boolean onFMS = false;

    private final EventStatus[] startEvents, duringEvents;

    {
        int count = Mode.values().length;
        startEvents = new EventStatus[count];
        duringEvents = new EventStatus[count];
        for (int i = 0; i < count; i++) {
            startEvents[i] = new EventStatus();
            duringEvents[i] = new EventStatus();
        }
    }

    /**
     * Create and initialize a new RawIOIgneousLauncherImpl.
     */
    @SuppressWarnings("deprecation")
    public DirectIgneousLauncherImpl() {
        FRCNetworkCommunicationsLibrary.FRCNetworkCommunicationUsageReportingReport((byte) tResourceType.kResourceType_Language, (byte) tInstances.kLanguage_Java, (byte) 0, "With the CCRE: the CommonChickenRuntimeEngine");
        File rootDir = new File("/home/lvuser/ccre-storage");
        rootDir.mkdirs();
        DefaultStorageProvider.register(rootDir);
        NetworkAutologger.register();
        BootLogger.register();
        FileLogger.register();
    }

    private enum Mode {
        DISABLED("disabled"), AUTONOMOUS("autonomous"), TELEOP("teleop"), TEST("test");

        private Mode(String name) {
            this.name = name;
        }

        public final String name;

        private EventStatus getStart(DirectIgneousLauncherImpl launcher) {
            return launcher.startEvents[ordinal()];
        }

        private EventStatus getDuring(DirectIgneousLauncherImpl launcher) {
            return launcher.duringEvents[ordinal()];
        }

        private void start(DirectIgneousLauncherImpl launcher, boolean onFMS) {
            try {
                Logger.fine("Began " + name + (onFMS ? " on FMS" : " mode"));
                getStart(launcher).produce();
            } catch (Throwable thr) {
                Logger.severe("Critical Code Failure in " + name + " init", thr);
            }
        }

        private void periodic(DirectIgneousLauncherImpl launcher) {
            try {
                if (countFails >= 50) {
                    countFails--;
                    if (getDuring(launcher).produceWithFailureRecovery()) {
                        countFails = 0;
                    }
                    if (globalPeriodic.produceWithFailureRecovery()) {
                        countFails = 0;
                    }
                } else {
                    getDuring(launcher).produce();
                    globalPeriodic.produce();
                    if (countFails > 0) {
                        countFails--;
                    }
                }
            } catch (Throwable thr) {
                Logger.severe("Critical Code Failure in " + name + " periodic", thr);
                countFails += 10;
            }
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
        Enumeration<URL> resources = DirectIgneousLauncherImpl.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        String name = null;
        while (resources != null && resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            name = manifest.getMainAttributes().getValue("CCRE-Main");
        }
        if (name == null) {
            throw new RuntimeException("Could not find MANIFEST-specified launchee!");
        }
        Logger.info("Starting application: " + name);
        ((IgneousApplication) Class.forName(name).newInstance()).setupRobot();
        Logger.info("Hello, " + name + "!");
    }

    public IJoystick getKinectJoystick(boolean isRightStick) {
        throw new RuntimeException("Kinect Joysticks are not supported by the RoboRIO.");
    }

    public BooleanOutput makeSolenoid(int module, int id) {
        final ByteBuffer port = DirectSolenoid.init(module, id);
        return value -> DirectSolenoid.set(port, value);
    }

    public BooleanOutput makeDigitalOutput(int id) {
        DirectDigital.init(id, false);
        return value -> DirectDigital.set(id, value);
    }

    public FloatInputPoll makeAnalogInput(int id) {
        ByteBuffer port = DirectAnalog.init(id);
        return () -> DirectAnalog.getAverageVoltage(port);
    }

    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        ByteBuffer port = DirectAnalog.init(id);
        DirectAnalog.configure(port, averageBits, 0); // TODO: oversample bits
        return () -> DirectAnalog.getAverageVoltage(port);
    }

    @Deprecated
    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        ByteBuffer port = DirectAnalog.init(id);
        DirectAnalog.configure(port, averageBits, 0);
        return () -> DirectAnalog.getAverageValue(port);
    }

    public BooleanInputPoll makeDigitalInput(int id) {
        DirectDigital.init(id, true);
        return () -> DirectDigital.get(id);
    }

    public FloatOutput makeServo(int id, final float minInput, float maxInput) {
        if (minInput == maxInput) {
            throw new IllegalArgumentException("Servos cannot have their extrema be the same!");
        }
        DirectPWM.init(id, DirectPWM.TYPE_SERVO);
        return f -> DirectPWM.set(id, (f - minInput) / (maxInput - minInput));
    }

    public void sendDSUpdate(String value, int lineid) {
        Logger.warning("The Driver Station LCD no longer exists!");
    }

    public BooleanInputPoll getIsDisabled() {
        return () -> activeMode == Mode.DISABLED;
    }

    public BooleanInputPoll getIsAutonomous() {
        return () -> activeMode == Mode.AUTONOMOUS;
    }

    public BooleanInputPoll getIsTest() {
        return () -> activeMode == Mode.TEST;
    }

    public BooleanInputPoll getIsFMS() {
        return () -> onFMS;
    }

    // TODO: Move this into Igneous?
    public void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        BooleanOutput relay = makeRelayForwardOutput(compressorRelayChannel);
        BooleanMixing.pumpWhen(new Ticker(500), BooleanMixing.invert(shouldDisable), relay);
    }

    public FloatInputPoll makeEncoder(int channelA, int channelB, boolean reverse, EventInput resetWhen) {
        ByteBuffer encoder = DirectEncoder.init(channelA, channelB, reverse);
        if (resetWhen != null) {
            resetWhen.send(() -> DirectEncoder.reset(encoder));
        }
        return () -> DirectEncoder.get(encoder);
    }

    public BooleanOutput makeRelayForwardOutput(int channel) {
        ByteBuffer relay = DirectRelay.init(channel);
        return (bln) -> DirectRelay.setForward(relay, bln);
    }

    public BooleanOutput makeRelayReverseOutput(int channel) {
        ByteBuffer relay = DirectRelay.init(channel);
        return (bln) -> DirectRelay.setReverse(relay, bln);
    }

    public FloatInputPoll makeGyro(int port, double sensitivity, EventInput evt) {
        ByteBuffer gyro;
        try {
            gyro = DirectGyro.init(port);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted during Gyro calibration", e);
        }
        if (evt != null) {
            evt.send(() -> DirectGyro.reset(gyro));
        }
        return () -> DirectGyro.getAngle(gyro, port, sensitivity);
    }

    @Deprecated
    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        if (sensitivity == 0) {
            throw new IllegalArgumentException("Accelerometer sensitivity cannot be zero!");
        }
        return FloatMixing.division.of(FloatMixing.subtraction.of(makeAnalogInput(port), (float) zeropoint), (float) sensitivity);
    }

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

    public EventInput getGlobalPeriodic() {
        return globalPeriodic;
    }

    public EventInput getStartAuto() {
        return Mode.AUTONOMOUS.getStart(this);
    }

    public EventInput getDuringAuto() {
        return Mode.AUTONOMOUS.getDuring(this);
    }

    public EventInput getStartTele() {
        return Mode.TELEOP.getStart(this);
    }

    public EventInput getDuringTele() {
        return Mode.TELEOP.getDuring(this);
    }

    public EventInput getStartTest() {
        return Mode.TEST.getStart(this);
    }

    public EventInput getDuringTest() {
        return Mode.TEST.getDuring(this);
    }

    public EventInput getStartDisabled() {
        return Mode.DISABLED.getStart(this);
    }

    public EventInput getDuringDisabled() {
        return Mode.DISABLED.getDuring(this);
    }

    private synchronized ByteBuffer getPCMCompressor() {
        if (pcmCompressor == null) {
            pcmCompressor = DirectCompressor.init(0); // TODO: Provide all PCM ids
        }
        return pcmCompressor;
    }

    public BooleanOutput usePCMCompressor() {
        DirectCompressor.setClosedLoop(getPCMCompressor(), true);
        return (on) -> DirectCompressor.setClosedLoop(getPCMCompressor(), on);
    }

    public BooleanInputPoll getPCMPressureSwitch() {
        getPCMCompressor();
        return () -> DirectCompressor.getPressureSwitch(getPCMCompressor());
    }

    public BooleanInputPoll getPCMCompressorRunning() {
        getPCMCompressor();
        return () -> DirectCompressor.getCompressorRunning(getPCMCompressor());
    }

    public FloatInputPoll getPCMCompressorCurrent() {
        getPCMCompressor();
        return () -> DirectCompressor.getCompressorCurrent(getPCMCompressor());
    }

    // TODO: Add the rest of the PCM and PDP accessors.

    public FloatInputPoll getPDPChannelCurrent(final int channel) {
        DirectPDP.checkChannel(channel);
        return () -> DirectPDP.getCurrent(channel);
    }

    public FloatInputPoll getPDPVoltage() {
        return () -> DirectPDP.getVoltage();
    }

    public boolean isRoboRIO() {
        return true;
    }

    public SerialIO makeRS232_Onboard(int baudRate, String deviceName) {
        return new SerialPortDirect(DirectRS232.PORT_ONBOARD, baudRate);
    }

    public SerialIO makeRS232_MXP(int baudRate, String deviceName) {
        return new SerialPortDirect(DirectRS232.PORT_MXP, baudRate);
    }

    public SerialIO makeRS232_USB(int baudRate, String deviceName) {
        return new SerialPortDirect(DirectRS232.PORT_USB, baudRate);
    }

    public IJoystickWithPOV getJoystick(int id) {
        if (id < 1 || id > 4) {
            throw new IllegalArgumentException("Joystick " + id + " is not a valid joystick number.");
        }
        return new CJoystickDirect(id, globalPeriodic);
    }

    public FloatInputPoll getBatteryVoltage() {
        DirectPower.init();
        return () -> DirectPower.getBatteryVoltage();
    }

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

    public ExtendedMotor makeCANTalon(int deviceNumber) {
        try {
            return new ExtendedTalonDirect(deviceNumber);
        } catch (ExtendedMotorFailureException e) {
            Logger.severe("Could not connect to CAN Talon " + deviceNumber, e);
            return new CommunicationFailureExtendedMotor("Could not connect to CAN Talon " + deviceNumber);
        }
    }

    public FloatInputPoll getChannelVoltage(int powerChannel) {
        if (DirectPower.readChannelVoltage(powerChannel) == -1) {
            Logger.warning("Unknown power channel: " + powerChannel);
        }
        return () -> DirectPower.readChannelVoltage(powerChannel);
    }

    public FloatInputPoll getChannelCurrent(int powerChannel) {
        if (DirectPower.readChannelCurrent(powerChannel) == -1) {
            Logger.warning("Unknown power channel: " + powerChannel);
        }
        return () -> DirectPower.readChannelCurrent(powerChannel);
    }

    public BooleanInputPoll getChannelEnabled(int powerChannel) {
        return () -> DirectPower.readChannelEnabled(powerChannel);
    }
}

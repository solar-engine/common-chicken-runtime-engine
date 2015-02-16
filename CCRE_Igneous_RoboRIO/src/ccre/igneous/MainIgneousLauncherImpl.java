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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
import ccre.ctrl.IJoystick;
import ccre.ctrl.IJoystickWithPOV;
import ccre.ctrl.Ticker;
import ccre.log.BootLogger;
import ccre.log.FileLogger;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;
import ccre.saver.DefaultStorageProvider;
import ccre.util.Version;
import edu.wpi.first.wpilibj.AnalogAccelerometer;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tInstances;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tResourceType;
import edu.wpi.first.wpilibj.communication.UsageReporting;

/**
 * The RoboRIO implementation of the IgneousLauncher interface. Do not use this!
 * This should only be referenced from the build script.
 *
 * @see IgneousLauncher
 * @author skeggsc
 */
public final class MainIgneousLauncherImpl extends RobotBase implements IgneousLauncher {

    private static final EventStatus globalPeriodic = new EventStatus();

    /**
     * The number of recent code failures - used to determine when to get
     * annoyed and start detaching broken code modules.
     */
    private static int countFails = 0;

    /**
     * Initialized by usePCMCompressor if needed.
     */
    private Compressor pcmCompressor;

    private Mode activeMode;

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
    public MainIgneousLauncherImpl() {
        UsageReporting.report(tResourceType.kResourceType_Language, tInstances.kLanguage_Java, 0, "With the CCRE: the CommonChickenRuntimeEngine");
        File rootDir = new File("/home/lvuser/ccre-storage");
        rootDir.mkdirs();
        DefaultStorageProvider.register(rootDir);
        NetworkAutologger.register();
        BootLogger.register();
        FileLogger.register();

        Logger.info("System running on " + Version.getVersion());
    }

    private enum Mode {
        DISABLED("disabled"), AUTONOMOUS("autonomous"), TELEOP("teleop"), TEST("test");

        private Mode(String name) {
            this.name = name;
        }

        public final String name;

        private EventStatus getStart(MainIgneousLauncherImpl launcher) {
            return launcher.startEvents[ordinal()];
        }

        private EventStatus getDuring(MainIgneousLauncherImpl launcher) {
            return launcher.startEvents[ordinal()];
        }

        private void start(MainIgneousLauncherImpl launcher) {
            try {
                Logger.fine("Began " + name + (DriverStation.getInstance().isFMSAttached() ? " on FMS" : " mode"));
                getStart(launcher).produce();
            } catch (Throwable thr) {
                Logger.severe("Critical Code Failure in " + name + " init", thr);
            }
        }

        private void periodic(MainIgneousLauncherImpl launcher) {
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

    @Override
    public void startCompetition() {
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

        IgneousLauncherHolder.setLauncher(this);
        Cluck.setupServer(1540); // Cluck de-facto off-FMS port.
        Cluck.setupServer(1735); // SmartDashboard port, since it's unused with the CCRE
        Cluck.setupServer(5800); // First team-use port.
        Cluck.setupServer(5805); // Another team-use port.
        try {
            setupMain();
            Cluck.getNode().notifyNetworkModified();
        } catch (RuntimeException ex) {
            Logger.severe("Critical Code Failure in Robot Init", ex);
            throw ex;
        } catch (Error err) {
            Logger.severe("Critical Code Failure in Robot Init", err);
            throw err;
        } catch (Throwable thr) {
            Logger.severe("Critical Code Failure in Robot Init", thr);
            throw new RuntimeException("Critical Code Failure", thr);
        }

        activeMode = null;

        while (true) {
            Mode newmode = calcMode();
            if (newmode != activeMode) {
                activeMode = newmode;
                activeMode.start(this);
            }
            if (m_ds.isNewControlData()) {
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
            m_ds.waitForData();
        }
    }

    private Mode calcMode() {
        if (isDisabled()) {
            return Mode.DISABLED;
        } else if (isTest()) {
            return Mode.TEST;
        } else if (isAutonomous()) {
            return Mode.AUTONOMOUS;
        } else {
            return Mode.TELEOP;
        }
    }

    private void setupMain() throws Throwable {
        Enumeration<URL> resources = MainIgneousLauncherImpl.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
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
        throw new IllegalArgumentException("Kinect Joysticks are not supported by the RoboRIO.");
    }

    public BooleanOutput makeSolenoid(int module, int id) {
        return new Solenoid(module, id)::set;
    }

    public BooleanOutput makeDigitalOutput(int id) {
        return new DigitalOutput(id)::set;
    }

    public FloatInputPoll getBatteryVoltage() {
        return () -> (float) DriverStation.getInstance().getBatteryVoltage();
    }

    public FloatInputPoll makeAnalogInput(int id) {
        final AnalogInput chan = new AnalogInput(id);
        return () -> (float) chan.getAverageVoltage();
    }

    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        final AnalogInput chan = new AnalogInput(id);
        chan.setAverageBits(averageBits);
        return () -> (float) chan.getAverageVoltage();
    }

    @Deprecated
    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        final AnalogInput chan = new AnalogInput(id);
        chan.setAverageBits(averageBits);
        return chan::getAverageValue;
    }

    public BooleanInputPoll makeDigitalInput(int id) {
        return new DigitalInput(id)::get;
    }

    public FloatOutput makeServo(int id, final float minInput, float maxInput) {
        final Servo servo = new Servo(id);
        return (f) -> servo.set((f - minInput) / (maxInput - minInput));
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

    public void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        BooleanOutput relay = makeRelayForwardOutput(compressorRelayChannel);
        BooleanMixing.pumpWhen(new Ticker(500), BooleanMixing.invert(shouldDisable), relay);
    }

    public FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
        final Encoder enc = new Encoder(aChannel, bChannel, reverse);
        if (resetWhen != null) {
            resetWhen.send(enc::reset);
        }
        return enc::get;
    }

    public BooleanOutput makeRelayForwardOutput(int channel) {
        final Relay r = new Relay(channel, Relay.Direction.kForward);
        return (bln) -> r.set(bln ? Relay.Value.kOn : Relay.Value.kOff);
    }

    public BooleanOutput makeRelayReverseOutput(int channel) {
        final Relay r = new Relay(channel, Relay.Direction.kReverse);
        return (bln) -> r.set(bln ? Relay.Value.kOn : Relay.Value.kOff);
    }

    public FloatInputPoll makeGyro(int port, double sensitivity, EventInput evt) {
        final Gyro g = new Gyro(port);
        g.setSensitivity(sensitivity);
        if (evt != null) {
            evt.send(() -> g.reset());
        }
        return () -> (float) g.getAngle();
    }

    @Deprecated
    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        final AnalogAccelerometer a = new AnalogAccelerometer(port);
        a.setSensitivity(sensitivity);
        a.setZero(zeropoint);
        return () -> (float) a.getAcceleration();
    }

    public IJoystickWithPOV getJoystick(int id) {
        if (id < 1 || id > 4) {
            throw new IllegalArgumentException("Joystick " + id + " is not a valid joystick number.");
        }
        return new CJoystick(id, globalPeriodic);
    }

    public FloatOutput makeMotor(int id, int type) {
        switch (type) {
        case JAGUAR:
            return new Jaguar(id)::set;
        case VICTOR:
            return new Victor(id)::set;
        case TALON:
            return new Talon(id)::set;
        default:
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    public ExtendedMotor makeCANJaguar(int deviceNumber) {
        try {
            return new ExtendedJaguar(deviceNumber);
        } catch (ExtendedMotorFailureException e) {
            Logger.severe("Could not connect to CAN Jaguar " + deviceNumber, e);
            return new CommunicationFailureExtendedMotor("Could not connect to CAN Jaguar " + deviceNumber);
        }
    }

    public ExtendedMotor makeCANTalon(int deviceNumber) {
        try {
            return new ExtendedTalon(deviceNumber);
        } catch (ExtendedMotorFailureException e) {
            Logger.severe("Could not connect to CAN Talon " + deviceNumber, e);
            return new CommunicationFailureExtendedMotor("Could not connect to CAN Talon " + deviceNumber);
        }
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

    private synchronized Compressor getPCMCompressor() {
        if (pcmCompressor == null) {
            pcmCompressor = new Compressor();
        }
        return pcmCompressor;
    }

    public BooleanOutput usePCMCompressor() {
        getPCMCompressor().setClosedLoopControl(true);
        return getPCMCompressor()::setClosedLoopControl;
    }

    public BooleanInputPoll getPCMPressureSwitch() {
        return getPCMCompressor()::getPressureSwitchValue;
    }

    public BooleanInputPoll getPCMCompressorRunning() {
        return getPCMCompressor()::enabled;
    }

    public FloatInputPoll getPCMCompressorCurrent() {
        return getPCMCompressor()::getCompressorCurrent;
    }

    public FloatInputPoll getPDPChannelCurrent(final int channel) {
        final PowerDistributionPanel panel = new PowerDistributionPanel();
        return () -> (float) panel.getCurrent(channel);
    }

    public FloatInputPoll getPDPVoltage() {
        final PowerDistributionPanel panel = new PowerDistributionPanel();
        return () -> (float) panel.getVoltage();
    }

    public boolean isRoboRIO() {
        return true;
    }

    public SerialIO makeRS232_Onboard(int baudRate, String deviceName) {
        return makeRS232(baudRate, SerialPort.Port.kOnboard);
    }

    public SerialIO makeRS232_MXP(int baudRate, String deviceName) {
        return makeRS232(baudRate, SerialPort.Port.kMXP);
    }

    public SerialIO makeRS232_USB(int baudRate, String deviceName) {
        return makeRS232(baudRate, SerialPort.Port.kUSB);
    }

    private SerialIO makeRS232(int baudRate, SerialPort.Port port) {
        SerialPort sp = new SerialPort(baudRate, port);
        sp.setReadBufferSize(1);
        sp.setWriteBufferSize(1);
        sp.disableTermination();
        return new SerialIO() {
            private boolean closed;

            public void setTermination(Character end) throws IOException {
                if (closed) {
                    throw new IOException("SerialIO closed.");
                }
                if (end == null) {
                    sp.disableTermination();
                } else {
                    sp.enableTermination(end);
                }
            }

            public byte[] readBlocking(int max) throws IOException {
                if (closed) {
                    throw new IOException("SerialIO closed.");
                }
                if (max <= 0) {
                    return new byte[0];
                }
                int ready = sp.getBytesReceived();
                if (ready <= 0) {
                    byte[] gotten = sp.read(1);
                    while (gotten.length == 0) {
                        gotten = sp.read(1); // block for minimal amount of time if any data has been received
                    }
                    ready = sp.getBytesReceived();
                    if (max == 1 || ready <= 0) {
                        return gotten;
                    }
                    byte[] rest = sp.read(Math.min(ready, max));
                    byte[] out = new byte[rest.length + 1];
                    out[0] = gotten[0];
                    System.arraycopy(rest, 0, out, 1, rest.length);
                    return out;
                } else {
                    return sp.read(Math.min(ready, max));
                }
            }

            public byte[] readNonblocking(int max) throws IOException {
                if (closed) {
                    throw new IOException("SerialIO closed.");
                }
                int count = Math.min(sp.getBytesReceived(), max);
                return count <= 0 ? new byte[0] : sp.read(count);
            }

            public void flush() throws IOException {
                if (closed) {
                    throw new IOException("SerialIO closed.");
                }
                sp.flush();
            }

            public void setFlushOnWrite(boolean flushOnWrite) throws IOException {
                if (closed) {
                    throw new IOException("SerialIO closed.");
                }
                sp.setWriteBufferMode(flushOnWrite ? SerialPort.WriteBufferMode.kFlushOnAccess : SerialPort.WriteBufferMode.kFlushWhenFull);
            }

            public void writeFully(byte[] bytes, int from, int to) throws IOException {
                if (closed) {
                    throw new IOException("SerialIO closed.");
                }
                if (from != 0) {
                    System.arraycopy(bytes, from, bytes, 0, to - from);
                }
                int remaining = to - from;
                while (true) {
                    int done = sp.write(bytes, remaining);
                    if (done >= remaining) {
                        break;
                    }
                    if (closed) {
                        throw new IOException("SerialIO closed.");
                    }
                    remaining -= done;
                    System.arraycopy(bytes, done, bytes, 0, remaining);
                }
            }

            public int writePartial(byte[] bytes, int from, int to) throws IOException {
                if (closed) {
                    throw new IOException("SerialIO closed.");
                }
                if (from != 0) {
                    System.arraycopy(bytes, from, bytes, 0, to - from);
                }
                return sp.write(bytes, to - from);
            }

            public void close() throws IOException {
                if (!closed) {
                    closed = true;
                    flush();
                    sp.free();
                }
            }
        };
    }
}

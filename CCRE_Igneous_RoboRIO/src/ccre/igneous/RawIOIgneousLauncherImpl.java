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
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary;

/**
 * The ADVANCED RoboRIO implementation of the IgneousLauncher interface. Do not
 * use this! This should only be referenced from the build script.
 *
 * @see IgneousLauncher
 * @author skeggsc
 */
// Note that this class uses STATIC variables in some cases! Don't instantiate it yourself.
public final class RawIOIgneousLauncherImpl extends RobotBase implements IgneousLauncher {

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

    public RawIOIgneousLauncherImpl() {
        File rootDir = new File("/home/lvuser/ccre-storage");
        rootDir.mkdirs();
        DefaultStorageProvider.register(rootDir);
        NetworkAutologger.register();
        BootLogger.register();
        FileLogger.register();
    }

    public enum Mode { // TODO: Remove static stuff.
        DISABLED("disabled"), AUTONOMOUS("autonomous"), TELEOP("teleop"), TEST("test");

        private Mode(String name) {
            this.name = name;
        }

        private final EventStatus start = new EventStatus(), during = new EventStatus();
        public final String name;

        private void start() {
            try {
                Logger.fine("Began " + name + (DriverStation.getInstance().isFMSAttached() ? " on FMS" : " mode"));
                start.produce();
            } catch (Throwable thr) {
                Logger.severe("Critical Code Failure in " + name + " init", thr);
            }
        }
        
        private void periodic() {
            try {
                if (countFails >= 50) {
                    countFails--;
                    if (during.produceWithFailureRecovery()) {
                        countFails = 0;
                    }
                    if (globalPeriodic.produceWithFailureRecovery()) {
                        countFails = 0;
                    }
                } else {
                    during.produce();
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
        IgneousLauncherHolder.setLauncher(this);
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
            throw new RuntimeException("Critical Code Failure", thr);
        }
        
        activeMode = null;

        while (true) {
            Mode newmode = calcMode();
            if (newmode != activeMode) {
                activeMode = newmode;
                activeMode.start();
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
                activeMode.periodic();
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
        Enumeration<URL> resources = RawIOIgneousLauncherImpl.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
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

    public IJoystick getKinectJoystick(boolean isRightStick) {
        return new CJoystick(isRightStick ? 6 : 5).attach(globalPeriodic);
    }

    public BooleanOutput makeSolenoid(int id) {
        return new Solenoid(id)::set;
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

    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        final AnalogAccelerometer a = new AnalogAccelerometer(port);
        a.setSensitivity(sensitivity);
        a.setZero(zeropoint);
        return () -> (float) a.getAcceleration();
    }

    public IJoystick getJoystick(int id) {
        if (id < 1 || id > 4) {
            throw new IllegalArgumentException("Joystick " + id + " is not a valid joystick number.");
        }
        return new CJoystick(id).attach(globalPeriodic);
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

    public EventInput getGlobalPeriodic() {
        return globalPeriodic;
    }

    public EventInput getStartAuto() {
        return Mode.AUTONOMOUS.start;
    }

    public EventInput getDuringAuto() {
        return Mode.AUTONOMOUS.during;
    }

    public EventInput getStartTele() {
        return Mode.TELEOP.start;
    }

    public EventInput getDuringTele() {
        return Mode.TELEOP.during;
    }

    public EventInput getStartTest() {
        return Mode.TEST.start;
    }

    public EventInput getDuringTest() {
        return Mode.TEST.during;
    }

    public EventInput getStartDisabled() {
        return Mode.DISABLED.start;
    }

    public EventInput getDuringDisabled() {
        return Mode.DISABLED.during;
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
}

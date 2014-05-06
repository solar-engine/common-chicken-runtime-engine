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

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.ctrl.IJoystick;
import ccre.log.BootLogger;
import ccre.log.FileLogger;
import ccre.log.NetworkAutologger;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarFile;

public class EmulatorLauncher implements IgneousLauncher {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (args.length != 1) {
            System.err.println("Expected arguments: <Igneous-Jar>");
            System.exit(-1);
            return;
        }
        File jarFile = new File(args[0]);
        JarFile igneousJar = new JarFile(jarFile);
        String mainClass;
        try {
            mainClass = igneousJar.getManifest().getMainAttributes().getValue("Igneous-Main");
        } finally {
            igneousJar.close();
        }
        if (mainClass == null) {
            throw new RuntimeException("Could not find MANIFEST-specified launchee!");
        }
        NetworkAutologger.register();
        BootLogger.register();
        FileLogger.register();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, EmulatorLauncher.class.getClassLoader());
        Class<? extends IgneousApplication> asSubclass = classLoader.loadClass(mainClass).asSubclass(IgneousApplication.class);
        EmulatorForm emf = new EmulatorForm();
        EmulatorLauncher main = new EmulatorLauncher(emf);
        emf.setVisible(true);
        main.start(asSubclass.getConstructor());
    }

    /**
     * The robot's core program.
     */
    protected IgneousApplication core;
    /**
     * A timer used to know when to call each periodic/during method.
     */
    protected Timer periodicTimer = new Timer();
    /**
     * The last known operating state of the virtual robot.
     */
    protected CurrentState lastState = CurrentState.DISABLED;

    protected final EmulatorForm emf;

    protected final EventStatus duringAutonomous = new EventStatus();
    protected final EventStatus duringDisabled = new EventStatus();
    protected final EventStatus duringTeleop = new EventStatus();
    protected final EventStatus duringTesting = new EventStatus();
    protected final EventStatus globalPeriodic = new EventStatus();
    protected final EventStatus startedDisabled = new EventStatus();
    protected final EventStatus startedAutonomous = new EventStatus();
    protected final EventStatus startedTeleop = new EventStatus();
    protected final EventStatus startedTesting = new EventStatus();

    public EmulatorLauncher(EmulatorForm emf) {
        this.emf = emf;
    }

    /**
     * Called to start the robot running.
     *
     * @param main The main program.
     */
    public void start(Constructor<? extends IgneousApplication> main) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        IgneousLauncherHolder.launcher = this;
        for (EmuJoystick emu : emf.joysticks) {
            globalPeriodic.send(emu);
        }
        Cluck.setupServer();
        new CluckTCPServer(Cluck.getNode(), 1540).start();
        this.core = main.newInstance();
        core.setupRobot();
        periodicTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updatePeriodic();
            }
        }, 10, 20);
    }

    /**
     * Called each ~20 ms to run the various periodic and during events.
     */
    protected void updatePeriodic() {
        CurrentState curstate = emf.getOperatingState();
        if (curstate != this.lastState) {
            this.lastState = curstate;
            switch (curstate) {
                case DISABLED:
                    this.startedDisabled.produce();
                    break;
                case AUTONOMOUS:
                    this.startedAutonomous.produce();
                    break;
                case TELEOPERATED:
                    this.startedTeleop.produce();
                    break;
                case TESTING:
                    this.startedTesting.produce();
                    break;
            }
        }
        switch (curstate) {
            case DISABLED:
                this.duringDisabled.produce();
                break;
            case TELEOPERATED:
                this.duringTeleop.produce();
                break;
            case AUTONOMOUS:
                this.duringAutonomous.produce();
                break;
            case TESTING:
                this.duringTesting.produce();
                break;
        }
        this.globalPeriodic.produce();
    }

    @Override
    public IJoystick getKinectJoystick(boolean isRightSide) {
        return emf.joysticks[isRightSide ? 5 : 4];
    }

    @Override
    public FloatOutput makeMotor(int id, int type) {
        switch (type) {
            case JAGUAR:
                return emf.getJaguar(id);
            case VICTOR:
                return emf.getVictor(id);
            case TALON:
                return emf.getTalon(id);
            default:
                throw new IllegalArgumentException("Invalid motor type: " + type);
        }
    }

    @Override
    public BooleanOutput makeSolenoid(int id) {
        BooleanOutput out = emf.getSolenoid(id);
        out.set(false);
        return out;
    }

    @Override
    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        // Average bits ignored here.
        return emf.getAnalog(id);
    }

    @Override
    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        // Average bits ignored here.
        return emf.getAnalogValue(id);
    }

    @Override
    public BooleanInputPoll makeDigitalInput(int id) {
        return emf.getDigitalInput(id);
    }

    @Override
    public BooleanOutput makeDigitalOutput(int id) {
        return emf.getDigitalOutput(id);
    }

    @Override
    public FloatOutput makeServo(int id, float minInput, float maxInput) {
        return emf.getServo(id, minInput, maxInput);
    }

    @Override
    public void sendDSUpdate(String value, int line) {
        emf.sendDSUpdate(value, line);
    }

    @Override
    public BooleanInputPoll getIsDisabled() {
        return new BooleanInputPoll() {
            @Override
            public boolean get() {
                return emf.getOperatingState() == CurrentState.DISABLED;
            }
        };
    }

    @Override
    public BooleanInputPoll getIsAutonomous() {
        return new BooleanInputPoll() {
            @Override
            public boolean get() {
                return emf.getOperatingState() == CurrentState.AUTONOMOUS;
            }
        };
    }

    @Override
    public BooleanInputPoll getIsTest() {
        return new BooleanInputPoll() {
            @Override
            public boolean get() {
                return emf.getOperatingState() == CurrentState.TESTING;
            }
        };
    }

    @Override
    public void useCustomCompressor(final BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        final BooleanOutput relay = makeRelayForwardOutput(compressorRelayChannel);
        emf.setCompressor(false);
        relay.set(false);
        globalPeriodic.send(new EventOutput() {
            @Override
            public void event() {
                boolean running = !shouldDisable.get();
                emf.setCompressor(emf.getOperatingState() != CurrentState.DISABLED && running);
                relay.set(running);
            }
        });
    }

    @Override
    public FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
        return emf.makeEncoder(aChannel, bChannel, reverse, resetWhen);
    }

    @Override
    public BooleanOutput makeRelayForwardOutput(int channel) {
        return emf.makeRelayForward(channel);
    }

    @Override
    public BooleanOutput makeRelayReverseOutput(int channel) {
        return emf.makeRelayReverse(channel);
    }

    @Override
    public FloatInputPoll makeGyro(int port, double sensitivity, EventInput source) {
        return emf.makeGyro(port, sensitivity, source);
    }

    @Override
    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        return emf.makeAccelerometerAxis(port, sensitivity, zeropoint);
    }

    @Override
    public FloatInputPoll getBatteryVoltage() {
        return makeAnalogInput(8, 8);
    }

    @Override
    public IJoystick getJoystick(int id) {
        if (id < 1 || id > 4) {
            throw new IllegalArgumentException("Expected a Joystick ID from 1 to 4!");
        }
        return emf.joysticks[id - 1];
    }

    @Override
    public EventInput getGlobalPeriodic() {
        return globalPeriodic;
    }

    @Override
    public EventInput getStartAuto() {
        return startedAutonomous;
    }

    @Override
    public EventInput getDuringAuto() {
        return duringAutonomous;
    }

    @Override
    public EventInput getStartTele() {
        return startedTeleop;
    }

    @Override
    public EventInput getDuringTele() {
        return duringTeleop;
    }

    @Override
    public EventInput getStartTest() {
        return startedTesting;
    }

    @Override
    public EventInput getDuringTest() {
        return duringTesting;
    }

    @Override
    public EventInput getStartDisabled() {
        return startedDisabled;
    }

    @Override
    public EventInput getDuringDisabled() {
        return duringDisabled;
    }
}

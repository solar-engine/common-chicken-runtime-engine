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
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.log.MultiTargetLogger;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarFile;

public class EmulatorLauncher implements IgneousLauncher {

    /**
     * The robot's core program.
     */
    public final IgneousCore core;
    /**
     * A timer used to know when to call each periodic/during method.
     */
    protected Timer periodicTimer = new Timer();
    /**
     * The last known operating state of the virtual robot.
     */
    protected CurrentState lastState = CurrentState.DISABLED;

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
        CluckGlobals.ensureInitializedCore();
        Logger.target = new MultiTargetLogger(new LoggingTarget[]{Logger.target, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger")});
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, EmulatorLauncher.class.getClassLoader());
        Class<? extends IgneousCore> asSubclass = classLoader.loadClass(mainClass).asSubclass(IgneousCore.class);
        EmulatorForm emf = new EmulatorForm();
        EmulatorLauncher main = new EmulatorLauncher(asSubclass.getConstructor().newInstance(), emf);
        emf.setVisible(true);
        main.start();
    }
    protected final EmulatorForm emf;

    public EmulatorLauncher(IgneousCore main, EmulatorForm emf) {
        this.core = main;
        this.emf = emf;
    }
    protected final Event duringAutonomous = new Event();
    protected final Event duringDisabled = new Event();
    protected final Event duringTeleop = new Event();
    protected final Event duringTesting = new Event();
    protected final Event globalPeriodic = new Event();
    protected final Event robotDisabled = new Event();
    protected final Event startedAutonomous = new Event();
    protected final Event startedTeleop = new Event();
    protected final Event startedTesting = new Event();

    public void start() {
        CluckGlobals.initializeServer(80);
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
                    this.robotDisabled.produce();
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
    public ISimpleJoystick makeSimpleJoystick(int id) {
        switch (id) {
            case 1:
                return emf.joy1;
            case 2:
                return emf.joy2;
            case 3:
                return emf.joy3;
            case 4:
                return emf.joy4;
            default:
                throw new RuntimeException("Invalid joystick ID: " + id);
        }
    }

    @Override
    public IDispatchJoystick makeDispatchJoystick(int id, EventSource source) {
        switch (id) {
            case 1:
                emf.joy1.addSource(source);
                return emf.joy1;
            case 2:
                emf.joy2.addSource(source);
                return emf.joy2;
            case 3:
                emf.joy3.addSource(source);
                return emf.joy3;
            case 4:
                emf.joy4.addSource(source);
                return emf.joy4;
            default:
                throw new RuntimeException("Invalid joystick ID: " + id);
        }
    }

    @Override
    public FloatOutput makeJaguar(int id, boolean negate) {
        return emf.getJaguar(id);
    }

    @Override
    public FloatOutput makeVictor(int id, boolean negate) {
        return emf.getVictor(id);
    }

    @Override
    public FloatOutput makeTalon(int id, boolean negate) {
        return emf.getTalon(id);
    }

    @Override
    public BooleanOutput makeSolenoid(int id) {
        return emf.getSolenoid(id);
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
        return emf.getDigital(id);
    }

    @Override
    public FloatOutput makeServo(int id, float minInput, float maxInput) {
        return emf.getServo(id, minInput, maxInput);
    }

    @Override
    public FloatOutput makeDSFloatReadout(String prefix, int line) {
        return emf.getDSReadout(prefix, line);
    }

    @Override
    public BooleanInputPoll getIsDisabled() {
        return new BooleanInputPoll() {
            @Override
            public boolean readValue() {
                return emf.getOperatingState() == CurrentState.DISABLED;
            }
        };
    }

    @Override
    public BooleanInputPoll getIsAutonomous() {
        return new BooleanInputPoll() {
            @Override
            public boolean readValue() {
                return emf.getOperatingState() == CurrentState.AUTONOMOUS;
            }
        };
    }

    @Override
    public void useCustomCompressor(final BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        emf.setCompressor(false);
        globalPeriodic.addListener(new EventConsumer() {
            @Override
            public void eventFired() {
                emf.setCompressor(emf.getOperatingState() != CurrentState.DISABLED && !shouldDisable.readValue());
            }
        });
    }
}

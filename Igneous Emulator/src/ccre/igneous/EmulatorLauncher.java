/*
 * Copyright 2013-214 Colby Skeggs
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

import ccre.chan.*;
import ccre.cluck.CluckGlobals;
import ccre.ctrl.*;
import ccre.device.DeviceException;
import ccre.device.DeviceHandle;
import ccre.device.DeviceRegistry;
import ccre.device.SimpleDeviceHandle;
import ccre.event.*;
import ccre.log.BootLogger;
import ccre.log.NetworkAutologger;
import ccre.util.LineCollectorOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
     * The cached Device Registry instance for this launcher.
     */
    private DeviceRegistry devReg;
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
        NetworkAutologger.register();
        BootLogger.register();
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

    /**
     * Called to start the robot running.
     */
    public void start() {
        CluckGlobals.setupServer();
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
        return emf.joysticks[id - 1];
    }

    @Override
    public IDispatchJoystick makeDispatchJoystick(int id, EventSource source) {
        EmuJoystick emu = emf.joysticks[id - 1];
        emu.addSource(source);
        return emu;
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
        BooleanOutput out = emf.getSolenoid(id);
        out.writeValue(false);
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
        final BooleanOutput relay = makeRelayForwardOutput(compressorRelayChannel);
        emf.setCompressor(false);
        relay.writeValue(false);
        globalPeriodic.addListener(new EventConsumer() {
            @Override
            public void eventFired() {
                boolean running = !shouldDisable.readValue();
                emf.setCompressor(emf.getOperatingState() != CurrentState.DISABLED && running);
                relay.writeValue(running);
            }
        });
    }

    @Override
    public FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventSource resetWhen) {
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
    public FloatInputPoll makeGyro(int port, double sensitivity, EventSource source) {
        return emf.makeGyro(port, sensitivity, source);
    }

    @Override
    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        return emf.makeAccelerometerAxis(port, sensitivity, zeropoint);
    }

    @Override
    public DeviceRegistry getDeviceRegistry() throws DeviceException {
        if (devReg == null) {
            devReg = new DeviceRegistry();
            devReg.putSimple("modes/auto/init", startedAutonomous, EventSource.class);
            devReg.putSimple("modes/teleop/init", startedTeleop, EventSource.class);
            devReg.putSimple("modes/test/init", startedTesting, EventSource.class);
            devReg.putSimple("modes/disabled/init", robotDisabled, EventSource.class);
            devReg.putSimple("modes/auto/during", duringAutonomous, EventSource.class);
            devReg.putSimple("modes/teleop/during", duringTeleop, EventSource.class);
            devReg.putSimple("modes/test/during", duringTesting, EventSource.class);
            devReg.putSimple("modes/disabled/during", duringDisabled, EventSource.class);
            devReg.putSimple("modes/always", globalPeriodic, EventSource.class);
            devReg.putSimple("modes/constant", core.constantPeriodic, EventSource.class);
            for (int joy = 1; joy <= 4; joy++) {
                final int cJoy = joy;
                final EmuJoystick oJoy = emf.joysticks[joy - 1];
                for (int axis = 1; axis <= 6; axis++) {
                    devReg.putSimple("joysticks/" + joy + "/axis" + axis, oJoy.getAxisChannel(axis), FloatInputPoll.class);
                }
                for (int button = 1; button <= 12; button++) {
                    devReg.putSimple("joysticks/" + joy + "/button" + button, oJoy.getButtonChannel(button), BooleanInputPoll.class);
                }
            }
            for (int pwm = 1; pwm <= 10; pwm++) {
                devReg.putHandle("pwms/victor" + pwm, new PWMHandle(emf, pwm, PWMHandle.VICTOR));
                devReg.putHandle("pwms/talon" + pwm, new PWMHandle(emf, pwm, PWMHandle.TALON));
                devReg.putHandle("pwms/jaguar" + pwm, new PWMHandle(emf, pwm, PWMHandle.JAGUAR));
                devReg.putHandle("pwms/servo" + pwm, new PWMHandle(emf, pwm, PWMHandle.SERVO));
            }
            for (int sol = 1; sol <= 8; sol++) {
                devReg.putSimple("pneumatics/solen" + sol, emf.getSolenoid(sol), BooleanOutput.class);
            }
            final BooleanStatus enableCompressor = new BooleanStatus();
            devReg.putSimple("pneumatics/compressorConf", new LineCollectorOutputStream() {
                @Override
                protected void collect(String string) {
                    int ii = string.indexOf(' ');
                    if (ii == -1) {
                        int portno = Integer.parseInt(string);
                        useCustomCompressor(enableCompressor, portno);
                    } else {
                        int portno = Integer.parseInt(string.substring(0, ii));
                        int extno = Integer.parseInt(string.substring(ii + 1));
                        enableCompressor.writeValue(true);
                        useCustomCompressor(Mixing.andBooleans(enableCompressor, makeDigitalInput(extno)), portno);
                    }
                }
            }, OutputStream.class);
            devReg.putSimple("pneumatics/compressorEnable", enableCompressor, BooleanOutput.class);
            devReg.putSimple("pneumatics/compressorEnabled", enableCompressor, BooleanInput.class);
            for (int dgt = 1; dgt <= 14; dgt++) {
                devReg.putHandle("gpios/out" + dgt, new GPOHandle(emf, dgt));
                devReg.putHandle("gpios/in" + dgt, new GPIHandle(emf, dgt));
            }
            // TODO: Implement encoders, Gyros, accelerometers.
            for (int alg = 1; alg <= 8; alg++) {
                devReg.putSimple("analogs/in" + alg, emf.getAnalog(alg), FloatInputPoll.class);
            }
            for (int lcd = 1; lcd <= 6; lcd++) {
                final int lcdI = lcd;
                devReg.putSimple("dslcd/line" + lcd, new LineCollectorOutputStream() {
                    @Override
                    protected void collect(String string) {
                        EmulatorLauncher.this.sendDSUpdate(string, lcdI);
                    }
                }, OutputStream.class);
            }
            for (int rel = 1; rel <= 8; rel++) {
                devReg.putHandle("relays/fwd" + rel, new RelayHandle(emf, rel, true));
                devReg.putHandle("relays/rev" + rel, new RelayHandle(emf, rel, false));
            }
        }
        return devReg;
    }
}

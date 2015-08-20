/*
 * Copyright 2014-2015 Colby Skeggs
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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.SerialIO;
import ccre.ctrl.DisconnectedSerialIO;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.FakeJoystick;
import ccre.ctrl.IJoystick;
import ccre.ctrl.LoopbackSerialIO;
import ccre.ctrl.Ticker;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.igneous.devices.BooleanControlDevice;
import ccre.igneous.devices.BooleanViewDevice;
import ccre.igneous.devices.CANJaguarDevice;
import ccre.igneous.devices.CANTalonDevice;
import ccre.igneous.devices.DSLCDDevice;
import ccre.igneous.devices.Disableable;
import ccre.igneous.devices.FloatControlDevice;
import ccre.igneous.devices.FloatViewDevice;
import ccre.igneous.devices.HeadingDevice;
import ccre.igneous.devices.JoystickDevice;
import ccre.igneous.devices.LoggingDevice;
import ccre.igneous.devices.RobotModeDevice;
import ccre.igneous.devices.SpinDevice;
import ccre.log.Logger;

/**
 * The IgneousLauncher provided to an emulated Igneous robot, for cRIO or
 * roboRIO.
 *
 * @author skeggsc
 */
public class DeviceBasedLauncher implements IgneousLauncher {

    /**
     * The DeviceListPanel used for all the virtual devices.
     */
    public final DeviceListPanel panel = new DeviceListPanel();
    private final boolean isRoboRIO;
    private final int baseIndex;
    private final JoystickHandler joyHandler = new JoystickHandler();
    private final EventInput onInitComplete;

    /**
     * Create a new DeviceBasedLauncher for either the cRIO or roboRIO.
     *
     * @param isRoboRIO specifies if the emulated robot should have a roboRIO
     * instead of a cRIO.
     * @param onInitComplete should be fired once the user program has
     * initialized.
     */
    public DeviceBasedLauncher(boolean isRoboRIO, EventInput onInitComplete) {
        this.isRoboRIO = isRoboRIO;
        this.onInitComplete = onInitComplete;
        baseIndex = isRoboRIO ? 0 : 1;
        joysticks = new IJoystick[isRoboRIO ? 6 : 4];
        motors = new FloatOutput[isRoboRIO ? 20 : 10];
        solenoids = new BooleanOutput[isRoboRIO ? 64 : 2][8];
        digitalOutputs = new BooleanOutput[isRoboRIO ? 26 : 14];
        digitalInputs = new BooleanInput[digitalOutputs.length];
        analogInputs = new FloatInput[isRoboRIO ? 8 : 14];
        servos = new FloatOutput[motors.length];
        relaysFwd = new BooleanOutput[isRoboRIO ? 4 : 8];
        relaysRev = new BooleanOutput[relaysFwd.length];
        mode = panel.add(new RobotModeDevice());
        mode.getIsEnabled().send(new BooleanOutput() {
            @Override
            public void set(boolean enabled) {
                for (Device d : panel) {
                    if (d instanceof Disableable) {
                        ((Disableable) d).notifyDisabled(!enabled);
                    }
                }
            }
        });
        logger = panel.add(new LoggingDevice(100));
        Logger.addTarget(logger);
    }

    /**
     * Clear out all the lines in the Emulator's logging pane.
     */
    public void clearLoggingPane() {
        logger.clearLines();
    }

    private int checkRange(String name, int id, Object[] target) {
        if (id < baseIndex || id >= target.length + baseIndex) {
            throw new IllegalArgumentException(name + " index out-of-range: " + id);
        }
        return id - baseIndex;
    }

    private final RobotModeDevice mode;
    private final LoggingDevice logger;
    private EventInput masterPeriodic = new Ticker(20);

    private IJoystick[] joysticks;

    public IJoystick getJoystick(int id) {
        if ((id == 5 || id == 6) && !isRoboRIO()) {
            return new FakeJoystick("The cRIO doesn't support Joystick #5 or #6!");
        }
        if (id < 1 || id > joysticks.length) {
            throw new IllegalArgumentException("Invalid Joystick #" + id + "!");
        }
        if (joysticks[id - 1] == null) {
            joysticks[id - 1] = new JoystickDevice(id, isRoboRIO, panel, joyHandler).getJoystick(masterPeriodic);
        }
        return joysticks[id - 1];
    }

    private IJoystick rightKinect, leftKinect;

    public IJoystick getKinectJoystick(boolean isRightArm) {
        if (isRightArm) {
            if (rightKinect == null) {
                rightKinect = new JoystickDevice("Kinect Right Arm", isRoboRIO, panel, joyHandler).addToMaster().getJoystick(masterPeriodic);
            }
            return rightKinect;
        } else {
            if (leftKinect == null) {
                leftKinect = new JoystickDevice("Kinect Left Arm", isRoboRIO, panel, joyHandler).addToMaster().getJoystick(masterPeriodic);
            }
            return leftKinect;
        }
    }

    private FloatOutput[] motors;

    public FloatOutput makeMotor(int id, int type) {
        int index = checkRange("Motor", id, motors);
        if (motors[index] == null) {
            String typename;
            switch (type) {
            case IgneousLauncher.JAGUAR:
                typename = "Jaguar";
                break;
            case IgneousLauncher.TALON:
                typename = "Talon";
                break;
            case IgneousLauncher.VICTOR:
                typename = "Victor";
                break;
            default:
                typename = "Unknown (%" + type + ")";
                break;
            }
            motors[index] = panel.add(new FloatViewDevice(typename + " " + id));
        }
        return motors[index];
    }

    public ExtendedMotor makeCANJaguar(int deviceNumber) {
        return new CANJaguarDevice(deviceNumber, panel).addToMaster().getMotor();
    }

    public ExtendedMotor makeCANTalon(int deviceNumber) {
        if (!isRoboRIO()) {
            throw new IllegalArgumentException("Cannot use a CANTalon on a cRIO!");
        }
        return new CANTalonDevice(deviceNumber, panel).addToMaster().getMotor();
    }

    private BooleanOutput[][] solenoids;

    public BooleanOutput makeSolenoid(int module, int id) {
        int moduleIndex = checkRange("Solenoid Module", module, solenoids);
        int index = checkRange("Solenoid", id, solenoids[moduleIndex]);
        if (solenoids[moduleIndex][index] == null) {
            solenoids[moduleIndex][index] = panel.add(new BooleanViewDevice("Solenoid " + module + ":" + id));
        }
        return solenoids[moduleIndex][index];
    }

    private BooleanOutput[] digitalOutputs;

    public BooleanOutput makeDigitalOutput(int id) {
        int index = checkRange("Digital Output", id, digitalOutputs);
        if (digitalOutputs[index] == null) {
            // TODO: Should this really be forced to disabled with other outputs?
            digitalOutputs[index] = panel.add(new BooleanViewDevice("Digital Output " + id));
        }
        return digitalOutputs[index];
    }

    private BooleanInput[] digitalInputs;

    public BooleanInput makeDigitalInput(int id, EventInput updateOn) {
        int index = checkRange("Digital Input", id, digitalInputs);
        if (digitalInputs[index] == null) {
            digitalInputs[index] = panel.add(new BooleanControlDevice("Digital Input " + id)).asInput();
        }
        return digitalInputs[index];
    }

    public BooleanInput makeDigitalInputByInterrupt(int id) {
        int index = checkRange("Digital Input", id, digitalInputs);
        if (digitalInputs[index] == null) {
            digitalInputs[index] = panel.add(new BooleanControlDevice("Digital Input " + id + " (Interrupt)")).asInput();
        }
        return digitalInputs[index];
    }

    private FloatInput[] analogInputs;

    public FloatInput makeAnalogInput(int id, EventInput updateOn) {
        int index = checkRange("Analog Input", id, analogInputs);
        if (analogInputs[index] == null) {
            analogInputs[index] = panel.add(new FloatControlDevice("Analog Input " + id, 0.0f, 5.0f, 1.0f, 0.0f)).asInput();
        }
        return analogInputs[index];
    }

    public FloatInput makeAnalogInput(int id, int averageBits, EventInput updateOn) {
        return makeAnalogInput(id, updateOn);
    }

    private FloatOutput[] servos;

    public FloatOutput makeServo(int id, float minInput, float maxInput) {
        int index = checkRange("Servo", id, servos);
        if (servos[index] == null) {
            servos[index] = panel.add(new FloatViewDevice("Servo " + id, minInput, maxInput));
        }
        return servos[index];
    }

    private DSLCDDevice dslcd;

    public void sendDSUpdate(String value, int lineid) {
        if (isRoboRIO()) {
            throw new IllegalStateException("DS LCD does not exist on roboRIO.");
        }
        if (dslcd == null) {
            dslcd = new DSLCDDevice();
        }
        dslcd.update(lineid, value);
    }

    public BooleanInput getIsDisabled() {
        return mode.getIsMode(RobotModeDevice.RobotMode.DISABLED);
    }

    public BooleanInput getIsAutonomous() {
        return mode.getIsMode(RobotModeDevice.RobotMode.AUTONOMOUS);
    }

    public BooleanInput getIsTest() {
        return mode.getIsMode(RobotModeDevice.RobotMode.TESTING);
    }

    private BooleanInput isFMS;

    public BooleanInput getIsFMS() {
        if (isFMS == null) {
            isFMS = panel.add(new BooleanControlDevice("On FMS")).asInput();
        }
        return isFMS;
    }

    public FloatInput makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen, EventInput updateOn) {
        return panel.add(new SpinDevice("Encoder " + aChannel + ":" + bChannel + (reverse ? " (REVERSED)" : ""), resetWhen)).asInput();
    }

    private BooleanOutput[] relaysFwd;

    public BooleanOutput makeRelayForwardOutput(int channel) {
        int index = checkRange("Relay", channel, relaysFwd);
        if (relaysFwd[index] == null) {
            relaysFwd[index] = panel.add(new BooleanViewDevice("Forward Relay " + channel));
        }
        return relaysFwd[index];
    }

    private BooleanOutput[] relaysRev;

    public BooleanOutput makeRelayReverseOutput(int channel) {
        int index = checkRange("Relay", channel, relaysRev);
        if (relaysRev[index] == null) {
            relaysRev[index] = panel.add(new BooleanViewDevice("Reverse Relay " + channel));
        }
        return relaysRev[index];
    }

    public FloatInput makeGyro(int port, double sensitivity, EventInput resetWhen, EventInput updateOn) {
        return panel.add(new SpinDevice("Gyro " + port + " (Sensitivity " + sensitivity + ")", resetWhen)).asInput();
    }

    private FloatInput batteryLevel;

    public FloatInput getBatteryVoltage(EventInput updateOn) {
        if (batteryLevel == null) {
            batteryLevel = panel.add(new FloatControlDevice("Battery Voltage (6.5V-12.5V)", 6.5f, 12.5f, 9.5f, 6.5f)).asInput();
        }
        return batteryLevel;
    }

    public EventInput getGlobalPeriodic() {
        return masterPeriodic;
    }

    private EventInput getModeBecomes(RobotModeDevice.RobotMode target) {
        return mode.getIsMode(target).onPress();
    }

    private EventInput getModeDuring(RobotModeDevice.RobotMode target) {
        return masterPeriodic.and(mode.getIsMode(target));
    }

    public EventInput getStartAuto() {
        return getModeBecomes(RobotModeDevice.RobotMode.AUTONOMOUS);
    }

    public EventInput getDuringAuto() {
        return getModeDuring(RobotModeDevice.RobotMode.AUTONOMOUS);
    }

    public EventInput getStartTele() {
        return getModeBecomes(RobotModeDevice.RobotMode.TELEOPERATED);
    }

    public EventInput getDuringTele() {
        return getModeDuring(RobotModeDevice.RobotMode.TELEOPERATED);
    }

    public EventInput getStartTest() {
        return getModeBecomes(RobotModeDevice.RobotMode.TESTING);
    }

    public EventInput getDuringTest() {
        return getModeDuring(RobotModeDevice.RobotMode.TESTING);
    }

    public EventInput getStartDisabled() {
        return getModeBecomes(RobotModeDevice.RobotMode.DISABLED);
    }

    public EventInput getDuringDisabled() {
        return getModeDuring(RobotModeDevice.RobotMode.DISABLED);
    }

    private final BooleanViewDevice pcmCompressor = new BooleanViewDevice("PCM Compressor Closed-Loop Control", true);
    private boolean pcmCompressorAdded = false;

    public synchronized BooleanOutput usePCMCompressor() {
        if (!isRoboRIO()) {
            throw new IllegalArgumentException("Cannot use a PCM on a cRIO!");
        }
        if (!pcmCompressorAdded) {
            panel.add(pcmCompressor);
            pcmCompressorAdded = true;
        }
        return pcmCompressor;
    }

    private BooleanInput pcmPressureSwitch;

    public BooleanInput getPCMPressureSwitch(EventInput updateOn) {
        if (!isRoboRIO()) {
            throw new IllegalArgumentException("Cannot use a PCM on a cRIO!");
        }
        if (pcmPressureSwitch == null) {
            pcmPressureSwitch = panel.add(new BooleanControlDevice("PCM Pressure Switch")).asInput();
        }
        return pcmPressureSwitch;
    }

    public BooleanInput getPCMCompressorRunning(EventInput updateOn) {
        if (!isRoboRIO()) {
            throw new IllegalArgumentException("Cannot use a PCM on a cRIO!");
        }
        return pcmCompressor.asInput().and(getPCMPressureSwitch(updateOn).not());
    }

    public FloatInput getPCMCompressorCurrent(EventInput updateOn) {
        return getAmperage("PCM Compressor", updateOn);
    }

    private FloatInput getAmperage(String label, EventInput updateOn) {
        return panel.add(new FloatControlDevice(label + " Current (0A-100A)", 0, 100, 0.5f, 0.0f)).asInput();
    }

    public FloatInput getPDPChannelCurrent(int channel, EventInput updateOn) {
        return getAmperage("PDP Channel " + channel, updateOn);
    }

    public FloatInput getPDPVoltage(EventInput updateOn) {
        return panel.add(new FloatControlDevice("PDP Voltage (6.5V-12.5V)", 6.5f, 12.5f, 9.5f, 6.5f)).asInput();
    }

    public boolean isRoboRIO() {
        return isRoboRIO;
    }

    public SerialIO makeRS232_Onboard(int baudRate, String deviceName) {
        return makeRS232("RS232 Onboard: " + baudRate, deviceName);
    }

    public SerialIO makeRS232_MXP(int baudRate, String deviceName) {
        return makeRS232("RS232 MXP: " + baudRate, deviceName);
    }

    public SerialIO makeRS232_USB(int baudRate, String deviceName) {
        return makeRS232("RS232 USB: " + baudRate, deviceName);
    }

    private SerialIO makeRS232(String display, String deviceName) {
        if ("loopback".equals(deviceName)) {
            panel.add(new HeadingDevice(display + ": Loopback"));
            return new LoopbackSerialIO();
        } else {
            panel.add(new HeadingDevice(display + ": Disconnected"));
            Logger.warning("Unrecognized serial device name '" + deviceName + "' on " + display + " - not emulating anything.");
            return new DisconnectedSerialIO();
        }
    }

    public FloatInput getChannelVoltage(int powerChannel, EventInput updateOn) {
        if (!isRoboRIO()) {
            if (powerChannel == Igneous.POWER_CHANNEL_BATTERY) {
                return getBatteryVoltage(updateOn);
            } else {
                Logger.warning("Voltage channels besides POWER_CHANNEL_BATTERY are not available on the cRIO!");
                return FloatInput.always(-1);
            }
        } else {
            switch (powerChannel) {
            case Igneous.POWER_CHANNEL_BATTERY:
                return getBatteryVoltage(updateOn);
            case Igneous.POWER_CHANNEL_3V3:
                return panel.add(new FloatControlDevice("Rail Voltage 3.3V (0V-4V)", 0.0f, 4.0f, 3.3f, 0.0f)).asInput();
            case Igneous.POWER_CHANNEL_5V:
                return panel.add(new FloatControlDevice("Rail Voltage 5V (0V-6V)", 0.0f, 6.0f, 5.0f, 0.0f)).asInput();
            case Igneous.POWER_CHANNEL_6V:
                return panel.add(new FloatControlDevice("Rail Voltage 6V (0V-7V)", 0.0f, 7.0f, 6.0f, 0.0f)).asInput();
            default:
                Logger.warning("Unknown power channel: " + powerChannel);
                return FloatInput.always(-1);
            }
        }
    }

    public FloatInput getChannelCurrent(int powerChannel, EventInput updateOn) {
        if (!isRoboRIO()) {
            Logger.warning("Current channels are not available on the cRIO!");
            return FloatInput.always(-1);
        } else {
            switch (powerChannel) {
            case Igneous.POWER_CHANNEL_BATTERY:
                return panel.add(new FloatControlDevice("Battery Current (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
            case Igneous.POWER_CHANNEL_3V3:
                return panel.add(new FloatControlDevice("Rail Current 3.3V (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
            case Igneous.POWER_CHANNEL_5V:
                return panel.add(new FloatControlDevice("Rail Current 5V (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
            case Igneous.POWER_CHANNEL_6V:
                return panel.add(new FloatControlDevice("Rail Current 6V (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
            default:
                Logger.warning("Unknown power channel: " + powerChannel);
                return FloatInput.always(-1);
            }
        }
    }

    public BooleanInput getChannelEnabled(int powerChannel, EventInput updateOn) {
        if (!isRoboRIO()) {
            Logger.warning("Power channel statuses are not available on the cRIO!");
            return powerChannel == Igneous.POWER_CHANNEL_BATTERY ? BooleanInput.alwaysTrue : BooleanInput.alwaysFalse;
        } else {
            switch (powerChannel) {
            case Igneous.POWER_CHANNEL_BATTERY:
                return BooleanInput.alwaysTrue;
            case Igneous.POWER_CHANNEL_3V3:
                return panel.add(new BooleanControlDevice("Rail Enabled 3.3V")).asInput();
            case Igneous.POWER_CHANNEL_5V:
                return panel.add(new BooleanControlDevice("Rail Enabled 5V")).asInput();
            case Igneous.POWER_CHANNEL_6V:
                return panel.add(new BooleanControlDevice("Rail Enabled 6V")).asInput();
            default:
                Logger.warning("Unknown power channel: " + powerChannel);
                return BooleanInput.alwaysFalse;
            }
        }
    }

    public ControlBindingCreator tryMakeControlBindingCreator(String title) {
        return new ControlBindingCreator() {
            public void addBoolean(String name, BooleanOutput output) {
                addBoolean(name).send(output);
            }

            public BooleanInput addBoolean(String name) {
                return panel.add(new BooleanControlDevice("Control: " + name)).asInput();
            }

            public void addFloat(String name, FloatOutput output) {
                addFloat(name).send(output);
            }

            public FloatInput addFloat(String name) {
                return panel.add(new FloatControlDevice("Control: " + name)).asInput();
            }
        };
    }

    @Override
    public EventInput getOnInitComplete() {
        return onInitComplete;
    }
}

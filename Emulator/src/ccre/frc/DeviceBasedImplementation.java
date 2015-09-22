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
package ccre.frc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.SerialIO;
import ccre.ctrl.DisconnectedSerialIO;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.IJoystick;
import ccre.ctrl.LoopbackSerialIO;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.devices.BooleanControlDevice;
import ccre.frc.devices.BooleanViewDevice;
import ccre.frc.devices.CANJaguarDevice;
import ccre.frc.devices.CANTalonDevice;
import ccre.frc.devices.Disableable;
import ccre.frc.devices.FloatControlDevice;
import ccre.frc.devices.FloatViewDevice;
import ccre.frc.devices.HeadingDevice;
import ccre.frc.devices.JoystickDevice;
import ccre.frc.devices.LoggingDevice;
import ccre.frc.devices.RobotModeDevice;
import ccre.frc.devices.SpinDevice;
import ccre.log.CCRELoggerFactory;
import ccre.timers.Ticker;

/**
 * The FRCImplementation provided to an emulated roboRIO robot.
 *
 * @author skeggsc
 */
public class DeviceBasedImplementation implements FRCImplementation {
    private Logger _logger = LoggerFactory.getLogger(getClass());

    /**
     * The DeviceListPanel used for all the virtual devices.
     */
    public final DeviceListPanel panel = new DeviceListPanel();
    private final JoystickHandler joyHandler = new JoystickHandler();
    private final EventInput onInitComplete;

    /**
     * Create a new DeviceBasedImplementation for the roboRIO.
     *
     * @param onInitComplete should be fired once the user program has
     * initialized.
     */
    public DeviceBasedImplementation(EventInput onInitComplete) {
        this.onInitComplete = onInitComplete;
        joysticks = new IJoystick[6];
        motors = new FloatOutput[20];
        solenoids = new BooleanOutput[64][8];
        digitalOutputs = new BooleanOutput[26];
        digitalInputs = new BooleanInput[digitalOutputs.length];
        analogInputs = new FloatInput[8];
        servos = new FloatOutput[motors.length];
        relaysFwd = new BooleanOutput[4];
        relaysRev = new BooleanOutput[relaysFwd.length];
        mode = panel.add(new RobotModeDevice());
        mode.getIsEnabled().send(enabled -> {
            for (Device d : panel) {
                if (d instanceof Disableable) {
                    ((Disableable) d).notifyDisabled(!enabled);
                }
            }
        });
        logger = panel.add(new LoggingDevice(100));
        CCRELoggerFactory.getLogger().addTarget(logger);
    }

    /**
     * Clear out all the lines in the Emulator's logging pane.
     */
    public void clearLoggingPane() {
        logger.clearLines();
    }

    private int checkRange(String name, int id, Object[] target) {
        if (id < 0 || id >= target.length) {
            throw new IllegalArgumentException(name + " index out-of-range: " + id);
        }
        return id;
    }

    private final RobotModeDevice mode;
    private final LoggingDevice logger;
    private EventInput masterPeriodic = new Ticker(20);

    private IJoystick[] joysticks;

    @Override
    public IJoystick getJoystick(int id) {
        if (id < 1 || id > joysticks.length) {
            throw new IllegalArgumentException("Invalid Joystick #" + id + "!");
        }
        if (joysticks[id - 1] == null) {
            joysticks[id - 1] = new JoystickDevice(id, true, panel, joyHandler).getJoystick(masterPeriodic);
        }
        return joysticks[id - 1];
    }

    private FloatOutput[] motors;

    @Override
    public FloatOutput makeMotor(int id, int type) {
        int index = checkRange("Motor", id, motors);
        if (motors[index] == null) {
            String typename;
            switch (type) {
            case FRCImplementation.JAGUAR:
                typename = "Jaguar";
                break;
            case FRCImplementation.TALON:
                typename = "Talon";
                break;
            case FRCImplementation.VICTOR:
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

    @Override
    public ExtendedMotor makeCANJaguar(int deviceNumber) {
        return new CANJaguarDevice(deviceNumber, panel).addToMaster().getMotor();
    }

    @Override
    public ExtendedMotor makeCANTalon(int deviceNumber) {
        return new CANTalonDevice(deviceNumber, panel).addToMaster().getMotor();
    }

    private BooleanOutput[][] solenoids;

    @Override
    public BooleanOutput makeSolenoid(int module, int id) {
        int moduleIndex = checkRange("Solenoid Module", module, solenoids);
        int index = checkRange("Solenoid", id, solenoids[moduleIndex]);
        if (solenoids[moduleIndex][index] == null) {
            solenoids[moduleIndex][index] = panel.add(new BooleanViewDevice("Solenoid " + module + ":" + id));
        }
        return solenoids[moduleIndex][index];
    }

    private BooleanOutput[] digitalOutputs;

    @Override
    public BooleanOutput makeDigitalOutput(int id) {
        int index = checkRange("Digital Output", id, digitalOutputs);
        if (digitalOutputs[index] == null) {
            digitalOutputs[index] = panel.add(new BooleanViewDevice("Digital Output " + id).setBypassDisabledMode());
        }
        return digitalOutputs[index];
    }

    private BooleanInput[] digitalInputs;

    @Override
    public BooleanInput makeDigitalInput(int id, EventInput updateOn) {
        int index = checkRange("Digital Input", id, digitalInputs);
        if (digitalInputs[index] == null) {
            digitalInputs[index] = panel.add(new BooleanControlDevice("Digital Input " + id)).asInput();
        }
        return digitalInputs[index];
    }

    @Override
    public BooleanInput makeDigitalInputByInterrupt(int id) {
        int index = checkRange("Digital Input", id, digitalInputs);
        if (digitalInputs[index] == null) {
            digitalInputs[index] = panel.add(new BooleanControlDevice("Digital Input " + id + " (Interrupt)")).asInput();
        }
        return digitalInputs[index];
    }

    private FloatInput[] analogInputs;

    @Override
    public FloatInput makeAnalogInput(int id, EventInput updateOn) {
        int index = checkRange("Analog Input", id, analogInputs);
        if (analogInputs[index] == null) {
            analogInputs[index] = panel.add(new FloatControlDevice("Analog Input " + id, 0.0f, 5.0f, 1.0f, 0.0f)).asInput();
        }
        return analogInputs[index];
    }

    @Override
    public FloatInput makeAnalogInput(int id, int averageBits, EventInput updateOn) {
        return makeAnalogInput(id, updateOn);
    }

    private FloatOutput[] servos;

    @Override
    public FloatOutput makeServo(int id, float minInput, float maxInput) {
        int index = checkRange("Servo", id, servos);
        if (servos[index] == null) {
            servos[index] = panel.add(new FloatViewDevice("Servo " + id, minInput, maxInput));
        }
        return servos[index];
    }

    @Override
    public BooleanInput getIsDisabled() {
        return mode.getIsMode(RobotModeDevice.RobotMode.DISABLED);
    }

    @Override
    public BooleanInput getIsAutonomous() {
        return mode.getIsMode(RobotModeDevice.RobotMode.AUTONOMOUS);
    }

    @Override
    public BooleanInput getIsTest() {
        return mode.getIsMode(RobotModeDevice.RobotMode.TESTING);
    }

    private BooleanInput isFMS;

    @Override
    public BooleanInput getIsFMS() {
        if (isFMS == null) {
            isFMS = panel.add(new BooleanControlDevice("On FMS")).asInput();
        }
        return isFMS;
    }

    @Override
    public FloatInput makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen, EventInput updateOn) {
        return panel.add(new SpinDevice("Encoder " + aChannel + ":" + bChannel + (reverse ? " (REVERSED)" : ""), resetWhen)).asInput();
    }

    private BooleanOutput[] relaysFwd;

    @Override
    public BooleanOutput makeRelayForwardOutput(int channel) {
        int index = checkRange("Relay", channel, relaysFwd);
        if (relaysFwd[index] == null) {
            relaysFwd[index] = panel.add(new BooleanViewDevice("Forward Relay " + channel));
        }
        return relaysFwd[index];
    }

    private BooleanOutput[] relaysRev;

    @Override
    public BooleanOutput makeRelayReverseOutput(int channel) {
        int index = checkRange("Relay", channel, relaysRev);
        if (relaysRev[index] == null) {
            relaysRev[index] = panel.add(new BooleanViewDevice("Reverse Relay " + channel));
        }
        return relaysRev[index];
    }

    @Override
    public FloatInput makeGyro(int port, double sensitivity, EventInput resetWhen, EventInput updateOn) {
        return panel.add(new SpinDevice("Gyro " + port + " (Sensitivity " + sensitivity + ")", resetWhen)).asInput();
    }

    private FloatInput batteryLevel;

    @Override
    public FloatInput getBatteryVoltage(EventInput updateOn) {
        if (batteryLevel == null) {
            batteryLevel = panel.add(new FloatControlDevice("Battery Voltage (6.5V-12.5V)", 6.5f, 12.5f, 9.5f, 6.5f)).asInput();
        }
        return batteryLevel;
    }

    @Override
    public EventInput getGlobalPeriodic() {
        return masterPeriodic;
    }

    private EventInput getModeBecomes(RobotModeDevice.RobotMode target) {
        return mode.getIsMode(target).onPress();
    }

    private EventInput getModeDuring(RobotModeDevice.RobotMode target) {
        return masterPeriodic.and(mode.getIsMode(target));
    }

    @Override
    public EventInput getStartAuto() {
        return getModeBecomes(RobotModeDevice.RobotMode.AUTONOMOUS);
    }

    @Override
    public EventInput getDuringAuto() {
        return getModeDuring(RobotModeDevice.RobotMode.AUTONOMOUS);
    }

    @Override
    public EventInput getStartTele() {
        return getModeBecomes(RobotModeDevice.RobotMode.TELEOPERATED);
    }

    @Override
    public EventInput getDuringTele() {
        return getModeDuring(RobotModeDevice.RobotMode.TELEOPERATED);
    }

    @Override
    public EventInput getStartTest() {
        return getModeBecomes(RobotModeDevice.RobotMode.TESTING);
    }

    @Override
    public EventInput getDuringTest() {
        return getModeDuring(RobotModeDevice.RobotMode.TESTING);
    }

    @Override
    public EventInput getStartDisabled() {
        return getModeBecomes(RobotModeDevice.RobotMode.DISABLED);
    }

    @Override
    public EventInput getDuringDisabled() {
        return getModeDuring(RobotModeDevice.RobotMode.DISABLED);
    }

    private final BooleanViewDevice pcmCompressor = new BooleanViewDevice("PCM Compressor Closed-Loop Control", true);
    private boolean pcmCompressorAdded = false;

    @Override
    public synchronized BooleanOutput usePCMCompressor() {
        if (!pcmCompressorAdded) {
            panel.add(pcmCompressor);
            pcmCompressorAdded = true;
        }
        return pcmCompressor;
    }

    private BooleanInput pcmPressureSwitch;

    @Override
    public BooleanInput getPCMPressureSwitch(EventInput updateOn) {
        if (pcmPressureSwitch == null) {
            pcmPressureSwitch = panel.add(new BooleanControlDevice("PCM Pressure Switch")).asInput();
        }
        return pcmPressureSwitch;
    }

    @Override
    public BooleanInput getPCMCompressorRunning(EventInput updateOn) {
        return pcmCompressor.asInput().and(getPCMPressureSwitch(updateOn).not());
    }

    @Override
    public FloatInput getPCMCompressorCurrent(EventInput updateOn) {
        return getAmperage("PCM Compressor", updateOn);
    }

    private FloatInput getAmperage(String label, EventInput updateOn) {
        return panel.add(new FloatControlDevice(label + " Current (0A-100A)", 0, 100, 0.5f, 0.0f)).asInput();
    }

    @Override
    public FloatInput getPDPChannelCurrent(int channel, EventInput updateOn) {
        return getAmperage("PDP Channel " + channel, updateOn);
    }

    @Override
    public FloatInput getPDPVoltage(EventInput updateOn) {
        return panel.add(new FloatControlDevice("PDP Voltage (6.5V-12.5V)", 6.5f, 12.5f, 9.5f, 6.5f)).asInput();
    }

    @Override
    public SerialIO makeRS232_Onboard(int baudRate, String deviceName) {
        return makeRS232("RS232 Onboard: " + baudRate, deviceName);
    }

    @Override
    public SerialIO makeRS232_MXP(int baudRate, String deviceName) {
        return makeRS232("RS232 MXP: " + baudRate, deviceName);
    }

    @Override
    public SerialIO makeRS232_USB(int baudRate, String deviceName) {
        return makeRS232("RS232 USB: " + baudRate, deviceName);
    }

    private SerialIO makeRS232(String display, String deviceName) {
        if ("loopback".equals(deviceName)) {
            panel.add(new HeadingDevice(display + ": Loopback"));
            return new LoopbackSerialIO();
        } else {
            panel.add(new HeadingDevice(display + ": Disconnected"));
            _logger.warn("Unrecognized serial device name '" + deviceName + "' on " + display + " - not emulating anything.");
            return new DisconnectedSerialIO();
        }
    }

    @Override
    public FloatInput getChannelVoltage(int powerChannel, EventInput updateOn) {
        switch (powerChannel) {
        case FRC.POWER_CHANNEL_BATTERY:
            return getBatteryVoltage(updateOn);
        case FRC.POWER_CHANNEL_3V3:
            return panel.add(new FloatControlDevice("Rail Voltage 3.3V (0V-4V)", 0.0f, 4.0f, 3.3f, 0.0f)).asInput();
        case FRC.POWER_CHANNEL_5V:
            return panel.add(new FloatControlDevice("Rail Voltage 5V (0V-6V)", 0.0f, 6.0f, 5.0f, 0.0f)).asInput();
        case FRC.POWER_CHANNEL_6V:
            return panel.add(new FloatControlDevice("Rail Voltage 6V (0V-7V)", 0.0f, 7.0f, 6.0f, 0.0f)).asInput();
        default:
            _logger.warn("Unknown power channel: " + powerChannel);
            return FloatInput.always(-1);
        }
    }

    @Override
    public FloatInput getChannelCurrent(int powerChannel, EventInput updateOn) {
        switch (powerChannel) {
        case FRC.POWER_CHANNEL_BATTERY:
            return panel.add(new FloatControlDevice("Battery Current (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
        case FRC.POWER_CHANNEL_3V3:
            return panel.add(new FloatControlDevice("Rail Current 3.3V (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
        case FRC.POWER_CHANNEL_5V:
            return panel.add(new FloatControlDevice("Rail Current 5V (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
        case FRC.POWER_CHANNEL_6V:
            return panel.add(new FloatControlDevice("Rail Current 6V (0-100A)", 0.0f, 100.0f, 5.0f, 0.0f)).asInput();
        default:
            _logger.warn("Unknown power channel: " + powerChannel);
            return FloatInput.always(-1);
        }
    }

    @Override
    public BooleanInput getChannelEnabled(int powerChannel, EventInput updateOn) {
        switch (powerChannel) {
        case FRC.POWER_CHANNEL_BATTERY:
            return BooleanInput.alwaysTrue;
        case FRC.POWER_CHANNEL_3V3:
            return panel.add(new BooleanControlDevice("Rail Enabled 3.3V")).asInput();
        case FRC.POWER_CHANNEL_5V:
            return panel.add(new BooleanControlDevice("Rail Enabled 5V")).asInput();
        case FRC.POWER_CHANNEL_6V:
            return panel.add(new BooleanControlDevice("Rail Enabled 6V")).asInput();
        default:
            _logger.warn("Unknown power channel: " + powerChannel);
            return BooleanInput.alwaysFalse;
        }
    }

    @Override
    public ControlBindingCreator tryMakeControlBindingCreator(String title) {
        return new ControlBindingCreator() {
            @Override
            public void addBoolean(String name, BooleanOutput output) {
                addBoolean(name).send(output);
            }

            @Override
            public BooleanInput addBoolean(String name) {
                return panel.add(new BooleanControlDevice("Control: " + name)).asInput();
            }

            @Override
            public void addFloat(String name, FloatOutput output) {
                addFloat(name).send(output);
            }

            @Override
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

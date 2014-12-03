package ccre.igneous.emulator;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;
import ccre.ctrl.Ticker;
import ccre.igneous.IgneousLauncher;
import ccre.igneous.emulator.devices.BooleanControlDevice;
import ccre.igneous.emulator.devices.BooleanViewDevice;
import ccre.igneous.emulator.devices.DSLCDDevice;
import ccre.igneous.emulator.devices.SpinDevice;
import ccre.igneous.emulator.devices.FloatControlDevice;
import ccre.igneous.emulator.devices.FloatViewDevice;
import ccre.igneous.emulator.devices.JoystickDevice;
import ccre.igneous.emulator.devices.RobotModeDevice;
import ccre.log.Logger;

public class DeviceBasedLauncher implements IgneousLauncher {

    private DeviceListPanel panel;
    private final boolean isRoboRIO;

    public DeviceBasedLauncher(DeviceListPanel panel) {
        this.panel = panel;
        this.isRoboRIO = false; // TODO: Full support for roboRIO.
    }

    private final RobotModeDevice mode = panel.add(new RobotModeDevice());
    private EventInput masterPeriodic = new Ticker(20);

    private IJoystick[] joysticks = new IJoystick[4];

    public IJoystick getJoystick(int id) {
        if (id < 1 || id > joysticks.length) {
            throw new IllegalArgumentException("Joystick index out-of-range: " + id);
        }
        if (joysticks[id - 1] == null) {
            joysticks[id - 1] = panel.add(new JoystickDevice(id));
        }
        return joysticks[id - 1];
    }

    private IJoystick rightKinect, leftKinect;

    public IJoystick getKinectJoystick(boolean isRightArm) {
        if (isRightArm) {
            if (rightKinect == null) {
                rightKinect = new JoystickDevice("Kinect Right Arm");
            }
            return rightKinect;
        } else {
            if (leftKinect == null) {
                leftKinect = new JoystickDevice("Kinect Left Arm");
            }
            return leftKinect;
        }
    }

    private FloatOutput[] motors = new FloatOutput[10];

    public FloatOutput makeMotor(int id, int type) {
        if (id < 1 || id > motors.length) {
            throw new IllegalArgumentException("Motor index out-of-range: " + id);
        }
        if (motors[id - 1] == null) {
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
            motors[id - 1] = panel.add(new FloatViewDevice(typename + " " + id));
        }
        return motors[id - 1];
    }

    private BooleanOutput[] solenoids = new BooleanOutput[8];

    public BooleanOutput makeSolenoid(int id) {
        if (id < 1 || id > solenoids.length) {
            throw new IllegalArgumentException("Solenoid index out-of-range: " + id);
        }
        if (solenoids[id - 1] == null) {
            solenoids[id - 1] = panel.add(new BooleanViewDevice("Solenoid " + id));
        }
        return solenoids[id - 1];
    }

    private BooleanOutput[] digitalOutputs = new BooleanOutput[14];

    public BooleanOutput makeDigitalOutput(int id) {
        if (id < 1 || id > digitalOutputs.length) {
            throw new IllegalArgumentException("Digital Output index out-of-range: " + id);
        }
        if (digitalOutputs[id - 1] == null) {
            digitalOutputs[id - 1] = panel.add(new BooleanViewDevice("Digital Output " + id));
        }
        return digitalOutputs[id - 1];
    }

    private BooleanInputPoll[] digitalInputs = new BooleanInputPoll[14];

    public BooleanInputPoll makeDigitalInput(int id) {
        if (id < 1 || id > digitalInputs.length) {
            throw new IllegalArgumentException("Digital Input index out-of-range: " + id);
        }
        if (digitalInputs[id - 1] == null) {
            digitalInputs[id - 1] = panel.add(new BooleanControlDevice("Digital Input " + id));
        }
        return digitalInputs[id - 1];
    }

    private FloatInputPoll[] analogInputs = new FloatInputPoll[14];

    public FloatInputPoll makeAnalogInput(int id) {
        if (id < 1 || id > analogInputs.length) {
            throw new IllegalArgumentException("Analog Input index out-of-range: " + id);
        }
        if (analogInputs[id - 1] == null) {
            analogInputs[id - 1] = panel.add(new FloatControlDevice("Analog Input " + id));
        }
        return analogInputs[id - 1];
    }

    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        return makeAnalogInput(id);
    }

    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        Logger.warning("ValueBased analog inputs are deprecated.");
        return FloatMixing.addition.of(FloatMixing.multiplication.of(makeAnalogInput(id), 2048), 2048);
    }

    private FloatOutput[] servos = new FloatOutput[motors.length];

    public FloatOutput makeServo(int id, float minInput, float maxInput) {
        if (id < 1 || id > servos.length) {
            throw new IllegalArgumentException("Servo index out-of-range: " + id);
        }
        if (servos[id - 1] == null) {
            servos[id - 1] = panel.add(new FloatViewDevice("Servo " + id, minInput, maxInput));
        }
        return servos[id - 1];
    }

    private DSLCDDevice dslcd;

    public void sendDSUpdate(String value, int lineid) {
        if (dslcd == null) {
            dslcd = new DSLCDDevice();
        }
        dslcd.update(lineid, value);
    }

    public BooleanInputPoll getIsDisabled() {
        return mode.getIsMode(RobotModeDevice.RobotMode.DISABLED);
    }

    public BooleanInputPoll getIsAutonomous() {
        return mode.getIsMode(RobotModeDevice.RobotMode.AUTONOMOUS);
    }

    public BooleanInputPoll getIsTest() {
        return mode.getIsMode(RobotModeDevice.RobotMode.TESTING);
    }

    public void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel) {
        BooleanOutput relay = panel.add(new BooleanViewDevice("Compressor (Forward Relay " + compressorRelayChannel + ")"));
        BooleanMixing.pumpWhen(new Ticker(500), BooleanMixing.invert(shouldDisable), relay);
    }

    public FloatInputPoll makeEncoder(int aChannel, int bChannel, boolean reverse, EventInput resetWhen) {
        return panel.add(new SpinDevice("Encoder " + aChannel + ":" + bChannel + (reverse ? " (REVERSED)" : ""), resetWhen));
    }

    private BooleanOutput[] relaysFwd = new BooleanOutput[8];

    public BooleanOutput makeRelayForwardOutput(int channel) {
        if (channel < 1 || channel > relaysFwd.length) {
            throw new IllegalArgumentException("Relay index out-of-bounds: " + channel);
        }
        if (relaysFwd[channel - 1] == null) {
            relaysFwd[channel - 1] = panel.add(new BooleanViewDevice("Forward Relay " + channel));
        }
        return relaysFwd[channel - 1];
    }

    private BooleanOutput[] relaysRev = new BooleanOutput[8];

    public BooleanOutput makeRelayReverseOutput(int channel) {
        if (channel < 1 || channel > relaysRev.length) {
            throw new IllegalArgumentException("Relay index out-of-bounds: " + channel);
        }
        if (relaysRev[channel - 1] == null) {
            relaysRev[channel - 1] = panel.add(new BooleanViewDevice("Reverse Relay " + channel));
        }
        return relaysRev[channel - 1];
    }

    public FloatInputPoll makeGyro(int port, double sensitivity, EventInput resetWhen) {
        return panel.add(new SpinDevice("Gyro " + port + " (Sensitivity " + sensitivity + ")", resetWhen));
    }

    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        return panel.add(new SpinDevice("Accelerometer " + port + " (Sensitivity " + sensitivity + ", Zero-Point " + zeropoint + ")", null));
    }

    public FloatInputPoll getBatteryVoltage() {
        return FloatMixing.addition.of(6.5f, FloatMixing.multiplication.of(3, panel.add(new FloatControlDevice("Battery Level (6.5V-12.5V)"))));
    }

    public EventInput getGlobalPeriodic() {
        return masterPeriodic;
    }

    private EventInput getModeBecomes(RobotModeDevice.RobotMode target) {
        return BooleanMixing.whenBooleanBecomes(mode.getIsMode(target), true, masterPeriodic);
    }

    private EventInput getModeDuring(RobotModeDevice.RobotMode target) {
        return EventMixing.filterEvent(mode.getIsMode(target), true, masterPeriodic);
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

    private BooleanViewDevice pcmCompressor;

    public BooleanOutput usePCMCompressor() {
        if (!isRoboRIO()) {
            throw new IllegalArgumentException("Cannot use a PCM on a cRIO!");
        }
        if (pcmCompressor == null) {
            pcmCompressor = panel.add(new BooleanViewDevice("PCM Compressor Closed-Loop Control"));
        }
        return pcmCompressor;
    }

    private BooleanInputPoll pcmPressureSwitch;

    public BooleanInputPoll getPCMPressureSwitch() {
        if (!isRoboRIO()) {
            throw new IllegalArgumentException("Cannot use a PCM on a cRIO!");
        }
        if (pcmPressureSwitch == null) {
            pcmPressureSwitch = panel.add(new BooleanControlDevice("PCM Pressure Switch"));
        }
        return pcmPressureSwitch;
    }

    public BooleanInputPoll getPCMCompressorRunning() {
        if (!isRoboRIO()) {
            throw new IllegalArgumentException("Cannot use a PCM on a cRIO!");
        }
        return BooleanMixing.andBooleans(new BooleanInputPoll() {
            public boolean get() {
                return pcmCompressor == null ? true : pcmCompressor.get();
            }
        }, BooleanMixing.invert(getPCMPressureSwitch()));
    }

    public FloatInputPoll getPCMCompressorCurrent() {
        return getAmperage("PCM Compressor");
    }

    private FloatInputPoll getAmperage(String label) {
        return FloatMixing.multiplication.of(10, FloatMixing.addition.of(1.0f, panel.add(new FloatControlDevice(label + " Current (0A-20A)"))));
    }

    public FloatInputPoll getPDPChannelCurrent(int channel) {
        return getAmperage("PDP Channel " + channel);
    }

    public FloatInputPoll getPDPVoltage() {
        return FloatMixing.addition.of(6.5f, FloatMixing.multiplication.of(3, panel.add(new FloatControlDevice("PDP Voltage (6.5V-12.5V)"))));
    }

    public boolean isRoboRIO() {
        return isRoboRIO;
    }
}

package ccre.igneous;

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
import ccre.igneous.devices.BooleanControlDevice;
import ccre.igneous.devices.BooleanViewDevice;
import ccre.igneous.devices.DSLCDDevice;
import ccre.igneous.devices.FloatControlDevice;
import ccre.igneous.devices.FloatViewDevice;
import ccre.igneous.devices.JoystickDevice;
import ccre.igneous.devices.RobotModeDevice;
import ccre.igneous.devices.SpinDevice;
import ccre.log.Logger;

public class DeviceBasedLauncher implements IgneousLauncher {

    public final DeviceListPanel panel = new DeviceListPanel();
    private final boolean isRoboRIO;
    private final int baseIndex;

    public DeviceBasedLauncher(boolean isRoboRIO) {
        this.isRoboRIO = isRoboRIO;
        baseIndex = isRoboRIO ? 0 : 1;
        joysticks = new IJoystick[isRoboRIO ? 6 : 4];
        motors = new FloatOutput[isRoboRIO ? 20 : 10];
        solenoids = new BooleanOutput[8];
        digitalOutputs = new BooleanOutput[isRoboRIO ? 26 : 14];
        digitalInputs = new BooleanInputPoll[digitalOutputs.length];
        analogInputs = new FloatInputPoll[isRoboRIO ? 8 : 14];
        servos = new FloatOutput[motors.length];
        relaysFwd = new BooleanOutput[isRoboRIO ? 4 : 8];
        relaysRev = new BooleanOutput[relaysFwd.length];
    }
    
    private int checkRange(String name, int id, Object[] target) {
        if (id < baseIndex || id >= target.length + baseIndex) {
            throw new IllegalArgumentException(name + " index out-of-range: " + id);
        }
        return id - baseIndex;
    }

    private final RobotModeDevice mode = panel.add(new RobotModeDevice());
    private EventInput masterPeriodic = new Ticker(20);

    private IJoystick[] joysticks;

    public IJoystick getJoystick(int id) {
        int index = checkRange("Joystick", id, joysticks);
        if (joysticks[index] == null) {
            joysticks[index] = new JoystickDevice(id, panel);
        }
        return joysticks[index];
    }

    private IJoystick rightKinect, leftKinect;

    public IJoystick getKinectJoystick(boolean isRightArm) {
        if (isRightArm) {
            if (rightKinect == null) {
                rightKinect = new JoystickDevice("Kinect Right Arm", panel).addToMaster();
            }
            return rightKinect;
        } else {
            if (leftKinect == null) {
                leftKinect = new JoystickDevice("Kinect Left Arm", panel).addToMaster();
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

    private BooleanOutput[] solenoids;

    public BooleanOutput makeSolenoid(int id) {
        int index = checkRange("Solenoid", id, solenoids);
        if (solenoids[index] == null) {
            solenoids[index] = panel.add(new BooleanViewDevice("Solenoid " + id));
        }
        return solenoids[index];
    }

    private BooleanOutput[] digitalOutputs;

    public BooleanOutput makeDigitalOutput(int id) {
        int index = checkRange("Digital Output", id, digitalOutputs);
        if (digitalOutputs[index] == null) {
            digitalOutputs[index] = panel.add(new BooleanViewDevice("Digital Output " + id));
        }
        return digitalOutputs[index];
    }

    private BooleanInputPoll[] digitalInputs;

    public BooleanInputPoll makeDigitalInput(int id) {
        int index = checkRange("Digital Input", id, digitalInputs);
        if (digitalInputs[index] == null) {
            digitalInputs[index] = panel.add(new BooleanControlDevice("Digital Input " + id));
        }
        return digitalInputs[index];
    }

    private FloatInputPoll[] analogInputs;

    public FloatInputPoll makeAnalogInput(int id) {
        int index = checkRange("Analog Input", id, analogInputs);
        if (analogInputs[index] == null) {
            analogInputs[index] = panel.add(new FloatControlDevice("Analog Input " + id));
        }
        return analogInputs[index];
    }

    public FloatInputPoll makeAnalogInput(int id, int averageBits) {
        return makeAnalogInput(id);
    }

    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits) {
        Logger.warning("ValueBased analog inputs are deprecated.");
        return FloatMixing.addition.of(FloatMixing.multiplication.of(makeAnalogInput(id), 2048), 2048);
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

    public FloatInputPoll makeGyro(int port, double sensitivity, EventInput resetWhen) {
        return panel.add(new SpinDevice("Gyro " + port + " (Sensitivity " + sensitivity + ")", resetWhen));
    }

    public FloatInputPoll makeAccelerometerAxis(int port, double sensitivity, double zeropoint) {
        return panel.add(new SpinDevice("Accelerometer " + port + " (Sensitivity " + sensitivity + ", Zero-Point " + zeropoint + ")", null));
    }
    
    private FloatInputPoll batteryLevel;

    public FloatInputPoll getBatteryVoltage() {
        if (batteryLevel == null) {
            batteryLevel = FloatMixing.addition.of(6.5f, FloatMixing.multiplication.of(3, panel.add(new FloatControlDevice("Battery Level (6.5V-12.5V)"))));
        }
        return batteryLevel;
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
            pcmCompressor.set(true);
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

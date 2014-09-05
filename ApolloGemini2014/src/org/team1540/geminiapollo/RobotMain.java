package org.team1540.geminiapollo;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.ctrl.Ticker;
import ccre.holders.TuningContext;
import ccre.igneous.IgneousCore;
import ccre.log.Logger;

public class RobotMain extends IgneousCore {

    public static final boolean IS_COMPETITION_ROBOT = true;
    private TestMode testing;
    private ControlInterface ui;

    public void setupRobot() {
        ui = new ControlInterface(joystick1, joystick2, globalPeriodic, startDisabled);
        ErrorMessages.setupError(constantPeriodic);
        new CluckTCPServer(Cluck.getNode(), 1180).start();
        testing = new TestMode(getIsTest());
        AutonomousController autonomous = new AutonomousController();

        BooleanStatus isKidMode = new BooleanStatus(true);
        Cluck.publish("Kid Mode", isKidMode);

        FloatInputPoll voltage = getBatteryVoltage();
        Cluck.publish("Battery Level", FloatMixing.createDispatch(voltage, globalPeriodic));
        FloatInputPoll displayReading;
        BooleanStatus safeToShoot = new BooleanStatus(), forceRunCollectorForArmAutolower = new BooleanStatus();
        BooleanInputPoll unsafeToCollect, disableSystemsForRearm;
        { // ==== SHOOTER CODE ====
            FloatOutput winchMotor = testing.testPublish("winch", makeTalonMotor(6, MOTOR_REVERSE, 1000f));
            BooleanOutput winchSolenoid = testing.testPublish("sol-winch-3", makeSolenoid(3));
            FloatInputPoll winchCurrent = makeAnalogInput(1, 8);
            Cluck.publish("Winch Current", FloatMixing.createDispatch(winchCurrent, globalPeriodic));
            EventInput fireWhen = EventMixing.combine(autonomous.getWhenToFire(), ui.getFireButton(isKidMode));
            // Global
            Shooter shooter = new Shooter(startDisabled, EventMixing.filterEvent(getIsTest(), false, globalPeriodic),
                    constantPeriodic, BooleanMixing.orBooleans(safeToShoot, getIsAutonomous()), voltage);
            EventInput rearmEvent = ui.getRearmCatapult();
            shooter.setupWinch(winchMotor, winchSolenoid, winchCurrent, getIsAutonomous());
            // Teleop
            shooter.setupRearmTimeout();
            shooter.handleShooterButtons(
                    EventMixing.combine(autonomous.getWhenToRearm(), rearmEvent),
                    fireWhen, autonomous.getNotifyRearmFinished());
            shooter.setupArmLower(ui.forceArmLower(), forceRunCollectorForArmAutolower);
            unsafeToCollect = BooleanMixing.alwaysFalse;//shooter.winchDisengaged;
            disableSystemsForRearm = shooter.rearming;
            // Autonomous
            autonomous.putCurrentActivator(shooter.shouldUseCurrent);
            // Readouts
            displayReading = Mixing.select(shooter.shouldUseCurrent, shooter.totalPowerTaken, shooter.activeAmps);
            ui.showFiring(shooter.winchDisengaged);
        }
        { // ==== ARM CODE ====
            BooleanOutput armMainSolenoid = testing.testPublish("sol-arm-2", makeSolenoid(2));
            BooleanOutput armLockSolenoid = testing.testPublish("sol-lock-8", makeSolenoid(8));
            BooleanOutput collectionSolenoids = BooleanMixing.combine(BooleanMixing.invert(testing.testPublish("sol-fingers-5", makeSolenoid(5))), testing.testPublish("sol-float-6", makeSolenoid(6)));
            collectionSolenoids.set(false);
            FloatOutput collectorMotor = testing.testPublish("collectorMotor", makeTalonMotor(7, MOTOR_REVERSE, 0.1f));
            // Teleoperated
            Actuators act = new Actuators(BooleanMixing.andBooleans(BooleanMixing.orBooleans(getIsTeleop(), getIsAutonomous()), BooleanMixing.invert(getIsDisabled())), getIsTeleop(), globalPeriodic, safeToShoot, ui.showArmDown(), ui.showArmUp(), armMainSolenoid, armLockSolenoid);
            startDisabled.send(act.armUp);
            ui.getArmLower().send(act.armDown);
            ui.getArmRaise().send(act.armUp);
            ui.getArmHold().send(act.armAlign);
            act.createCollector(duringTele, collectorMotor, collectionSolenoids,
                    ui.rollerIn(), ui.rollerOut(), unsafeToCollect, BooleanMixing.orBooleans(forceRunCollectorForArmAutolower, ui.shouldBeCollectingBecauseLoader()));
            // Autonomous
            autonomous.putArm(act.armDown, act.armUp, act.armAlign, collectorMotor, collectionSolenoids);
        }
        { // ==== KINECT CODE
            autonomous.putKinectTrigger(BooleanMixing.alwaysFalse);
            //autonomous.putKinectTrigger(KinectControl.main(globalPeriodic, getKinectJoystick(false), getKinectJoystick(true)));
        }
        { // ==== DRIVING ====
            FloatOutput leftDrive1 = makeTalonMotor(2, MOTOR_FORWARD, 0.1f), rightDrive1 = makeTalonMotor(4, MOTOR_REVERSE, 0.1f);
            FloatOutput leftDrive2 = makeTalonMotor(3, MOTOR_FORWARD, 0.1f), rightDrive2 = makeTalonMotor(5, MOTOR_REVERSE, 0.1f);
            FloatOutput leftDrive = FloatMixing.combine(leftDrive1, leftDrive2), rightDrive = FloatMixing.combine(rightDrive1, rightDrive2);
            BooleanOutput shiftSolenoid = testing.testPublish("sol-shift-1", makeSolenoid(1));
            testing.addDriveMotors(leftDrive1, leftDrive2, leftDrive, rightDrive1, rightDrive2, rightDrive);
            // Reset
            FloatMixing.setWhen(startDisabled, FloatMixing.combine(leftDrive, rightDrive), 0);
            // Teleoperated
            BooleanStatus shiftBoolean = DriveCode.createShifting(startTele, startAuto, duringTele,
                    shiftSolenoid, ui.getShiftHighButton(), ui.getShiftLowButton());
            DriveCode.createDrive(startTele, duringTele, leftDrive, rightDrive,
                    ui.getLeftDriveAxis(), ui.getRightDriveAxis(), ui.getForwardDriveAxis(),
                    shiftBoolean, ui.getToggleDisabled(), disableSystemsForRearm, isKidMode);
            // Autonomous
            autonomous.putDriveMotors(leftDrive, rightDrive);
        }
        // ==== Compressor ====
        setupCompressorAndDisplay(displayReading, disableSystemsForRearm);
        // ==== Phidget Mode Code ====
        duringTele.send(new EventOutput() {
            public void event() {
                ErrorMessages.displayError(0, "(1540) APOLLO (TELE)", 200);
            }
        });
        duringAuto.send(new EventOutput() {
            public void event() {
                ErrorMessages.displayError(0, "(AUTO) APOLLO (1540)", 200);
            }
        });
        duringTest.send(new EventOutput() {
            public void event() {
                ErrorMessages.displayError(0, "(TEST) APOLLO (TEST)", 200);
            }
        });
    }

    private void setupCompressorAndDisplay(FloatInputPoll winch, final BooleanInputPoll disableCompressor) {
        final BooleanInputPoll pressureSwitch = makeDigitalInput(1);
        final FloatStatus override = new FloatStatus();
        final FloatInputPoll pressureSensor = makeAnalogInput(2, 8);
        Cluck.publish("Compressor Override", override);
        Cluck.publish("Compressor Sensor", BooleanMixing.createDispatch(pressureSwitch, globalPeriodic));
        Cluck.publish("Pressure Sensor", FloatMixing.createDispatch(pressureSensor, globalPeriodic));
        final TuningContext tuner = new TuningContext(Cluck.getNode(), "PressureTuner");
        tuner.publishSavingEvent("Pressure");
        final FloatInputPoll zeroP = tuner.getFloat("LowPressure", 0.494f);
        final FloatInputPoll oneP = tuner.getFloat("HighPressure", 2.9f);
        final FloatInputPoll percentPressure = new FloatInputPoll() {
            public float get() {
                return 100 * ControlInterface.normalize(zeroP.get(), oneP.get(), pressureSensor.get());
            }
        };
        EventOutput report = new EventOutput() {
            private float last;

            public void event() {
                float cur = percentPressure.get();
                if (Math.abs(last - cur) > 0.05) {
                    last = cur;
                    Logger.fine("Pressure: " + cur);
                }
            }
        };
        startAuto.send(report);
        startTele.send(report);
        startDisabled.send(report);
        new Ticker(10000).send(report);
        ui.displayPressureAndWinch(percentPressure, globalPeriodic, pressureSwitch, winch);
        constantPeriodic.send(new EventOutput() {
            public void event() {
                float value = override.get();
                if (value > 0) {
                    override.set(Math.max(0, Math.min(10, value) - 0.01f));
                }
            }
        });
        usePCMCompressor();
        useCustomCompressor(new BooleanInputPoll() {
            BooleanStatus bypass = new BooleanStatus(false);

            {
                Cluck.publish("CP Bypass", bypass);
            }
            boolean pressureInRange = false;

            public boolean get() {
                float value = override.get(), pct = percentPressure.get();
                boolean byp = this.bypass.get(), pswit = pressureSwitch.get();
                if (pct >= 100) {
                    pressureInRange = true;
                } else if (pct < 97) {
                    pressureInRange = false;
                }
                boolean auto = byp ? (pressureInRange || pct < -1) : (pswit || pct >= 105);
                return value < 0 || ((auto || disableCompressor.get()) && value == 0);
            }
        }, 1);
    }
}

package org.team1540.apollogemini2;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.ctrl.PauseTimer;
import ccre.holders.TuningContext;
import ccre.igneous.Igneous;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;
import ccre.log.Logger;

public class Actuators {

    public static final int STATE_UP = 0, STATE_DOWN = 1, STATE_ALIGN = 2;

    private static final TuningContext actuatorTuningContext = new TuningContext("Actuators").publishSavingEvent();

    private static final BooleanStatus isSafeToShootStatus = new BooleanStatus();
    public static final BooleanInputPoll isSafeToShoot = isSafeToShootStatus;
    public static final EventStatus armLowerForShooter = new EventStatus();

    public static void setup() {
        final BooleanOutput armMainSolenoid;
        final BooleanOutput armLockSolenoid;
        FloatOutput armCollectorMotor;
        if (Igneous.isRoboRIO()) {
            armMainSolenoid = Igneous.makeSolenoid(6);
            armLockSolenoid = Igneous.makeSolenoid(1);
            armCollectorMotor = Igneous.makeTalonMotor(7, Igneous.MOTOR_REVERSE, 0.1f);
        } else {
            armMainSolenoid = Igneous.makeSolenoid(2);
            armLockSolenoid = Igneous.makeSolenoid(8);
            armCollectorMotor = Igneous.makeVictorMotor(6, Igneous.MOTOR_REVERSE, 0.1f);
        }
        BooleanOutput armFingerSolenoids = BooleanMixing.combine(
                BooleanMixing.invert(Igneous.makeSolenoid(5)),
                Igneous.makeSolenoid(Igneous.isRoboRIO() ? 0 : 6));
        armFingerSolenoids.set(false);

        BooleanInputPoll runActuatorControlLoop =
                BooleanMixing.andBooleans(
                        BooleanMixing.orBooleans(Igneous.getIsTeleop(), Igneous.getIsAutonomous()),
                        BooleanMixing.invert(Igneous.getIsDisabled()));
        AutonomousFramework.addArmActuators(armCollectorMotor, armFingerSolenoids);
        final PauseTimer runCollectorsWhileArmAligns = new PauseTimer(1000);
        final PauseTimer runCollectorsWhileArmLowers = new PauseTimer(500);
        final BooleanInputPoll runCollectorWhileArmMoves = BooleanMixing.orBooleans(runCollectorsWhileArmAligns, runCollectorsWhileArmLowers);
        new InstinctModule(runActuatorControlLoop) {
            final BooleanStatus hasPressedUp = new BooleanStatus(),
                    hasPressedDown = new BooleanStatus(),
                    hasPressedAlign = new BooleanStatus();
            {
                hasPressedUp.setTrueWhen(UserInterface.getArmRaise());
                hasPressedDown.setTrueWhen(armLowerForShooter);
                hasPressedDown.setTrueWhen(UserInterface.getArmLower());
                hasPressedAlign.setTrueWhen(UserInterface.getArmAlign());
                AutonomousFramework.addArmPositions(hasPressedDown.getSetTrueEvent(), hasPressedUp.getSetTrueEvent(), hasPressedAlign.getSetTrueEvent());
            }
            final BooleanOutput armDownLight = UserInterface.getArmDownLight(),
                    armUpLight = UserInterface.getArmUpLight();
            final FloatStatus movementUpDelay = actuatorTuningContext.getFloat("arm-up-delay", 0.3f);
            final FloatStatus movementDownDelay = actuatorTuningContext.getFloat("arm-hover-delay", 0.8f);

            @Override
            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                int current_state = STATE_UP;
                while (true) {
                    hasPressedUp.set(false);
                    hasPressedDown.set(false);
                    hasPressedAlign.set(false);
                    isSafeToShootStatus.set(current_state != STATE_UP);
                    switch (current_state) {
                    case STATE_UP:
                        Logger.fine("Actuator state: UP");
                        armMainSolenoid.set(false);
                        armLockSolenoid.set(false);
                        armUpLight.set(true);
                        armDownLight.set(false);
                        if (waitUntilOneOf(new BooleanInputPoll[] { hasPressedDown, hasPressedAlign }) == 0) {
                            runCollectorsWhileArmLowers.event();
                            current_state = STATE_DOWN;
                        } else {
                            armUpLight.set(true);
                            armDownLight.set(true);
                            armMainSolenoid.set(true);
                            armLockSolenoid.set(false);
                            runCollectorsWhileArmAligns.event();
                            waitForTime((long) (movementDownDelay.get() * 1000L));
                            current_state = STATE_ALIGN;
                        }
                        break;
                    case STATE_DOWN:
                        Logger.fine("Actuator state: DOWN");
                        armMainSolenoid.set(true);
                        armLockSolenoid.set(false);
                        armUpLight.set(false);
                        armDownLight.set(true);
                        if (waitUntilOneOf(new BooleanInputPoll[] { hasPressedUp, hasPressedAlign }) == 0) {
                            current_state = STATE_UP;
                        } else {
                            runCollectorsWhileArmAligns.event();
                            current_state = STATE_ALIGN;
                        }
                        break;
                    case STATE_ALIGN:
                        Logger.fine("Actuator state: ALIGN");
                        armMainSolenoid.set(false);
                        armLockSolenoid.set(true);
                        armUpLight.set(false);
                        armDownLight.set(false);
                        if (waitUntilOneOf(new BooleanInputPoll[] { hasPressedDown, hasPressedUp }) == 0) {
                            runCollectorsWhileArmLowers.event();
                            current_state = STATE_DOWN;
                        } else {
                            isSafeToShootStatus.set(false);
                            armUpLight.set(true);
                            armDownLight.set(true);
                            armMainSolenoid.set(true);
                            armLockSolenoid.set(true);
                            waitForTime((long) (movementUpDelay.get() * 1000L));
                            current_state = STATE_UP;
                        }
                        break;
                    default:
                        current_state = STATE_UP;
                        Logger.warning("Bad Actuator state! Resetting to up.");
                        break;
                    }
                }
            }
        }.updateWhen(Igneous.globalPeriodic);
        final BooleanInputPoll switchRollersIn = UserInterface.getRollersInSwitch();
        BooleanInputPoll switchRollersOut = UserInterface.getRollersOutSwitch();
        
        FloatInputPoll forwardCollectorSpeed = actuatorTuningContext.getFloat("forward-collector-speed", 1.0f);
        FloatInputPoll reverseCollectorSpeed = actuatorTuningContext.getFloat("reverse-collector-speed", -1.0f);
        
        FloatInputPoll collectorSpeedFromArm = Mixing.select(runCollectorWhileArmMoves, reverseCollectorSpeed, FloatMixing.always(0f));
        
        FloatMixing.pumpWhen(Igneous.duringTele, Mixing.quadSelect(switchRollersIn, switchRollersOut,
                collectorSpeedFromArm, reverseCollectorSpeed,
                forwardCollectorSpeed, FloatMixing.always(0f)),
                armCollectorMotor);
        BooleanMixing.pumpWhen(Igneous.duringTele, BooleanMixing.orBooleans(switchRollersIn, switchRollersOut), armFingerSolenoids);

        Cluck.publish("arm-main-solenoid", armMainSolenoid);
        Cluck.publish("arm-lock-solenoid", armLockSolenoid);
        Cluck.publish("arm-collector-motor", armCollectorMotor);
        Cluck.publish("arm-finger-solenoids", armFingerSolenoids);
    }
}

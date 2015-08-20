/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 *
 * This file is part of the Revised ApolloGemini2014 project.
 *
 * ApolloGemini2014 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * ApolloGemini2014 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ApolloGemini2014.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.apollogemini2;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
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
    public static final BooleanInput isSafeToShoot = isSafeToShootStatus;
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
        BooleanOutput armFingerSolenoids = Igneous.makeSolenoid(5).invert().combine(Igneous.makeSolenoid(Igneous.isRoboRIO() ? 0 : 6));
        armFingerSolenoids.set(false);

        BooleanInput runActuatorControlLoop = Igneous.getIsEnabled().and(Igneous.getIsTeleop().or(Igneous.getIsAutonomous()));
        AutonomousModeBase.addArmActuators(armCollectorMotor, armFingerSolenoids);
        final PauseTimer runCollectorsWhileArmAligns = new PauseTimer(1000);
        final PauseTimer runCollectorsWhileArmLowers = new PauseTimer(500);
        final BooleanInput runCollectorWhileArmMoves = runCollectorsWhileArmAligns.or(runCollectorsWhileArmLowers);
        new InstinctModule(runActuatorControlLoop) {
            final BooleanStatus hasPressedUp = new BooleanStatus(),
                    hasPressedDown = new BooleanStatus(),
                    hasPressedAlign = new BooleanStatus();

            {
                hasPressedUp.setTrueWhen(UserInterface.getArmRaise());
                hasPressedDown.setTrueWhen(armLowerForShooter);
                hasPressedDown.setTrueWhen(UserInterface.getArmLower());
                hasPressedAlign.setTrueWhen(UserInterface.getArmAlign());
                AutonomousModeBase.addArmPositions(hasPressedDown.getSetTrueEvent(), hasPressedUp.getSetTrueEvent(), hasPressedAlign.getSetTrueEvent());
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
                        if (waitUntilOneOf(hasPressedDown, hasPressedAlign) == 0) {
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
                        if (waitUntilOneOf(hasPressedUp, hasPressedAlign) == 0) {
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
                        if (waitUntilOneOf(hasPressedDown, hasPressedUp) == 0) {
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

            protected String getTypeName() {
                return "actuator control loop";
            }
        };
        final BooleanInput switchRollersIn = UserInterface.getRollersInSwitch();
        BooleanInput switchRollersOut = UserInterface.getRollersOutSwitch();

        FloatInput forwardCollectorSpeed = actuatorTuningContext.getFloat("forward-collector-speed", 1.0f);
        FloatInput reverseCollectorSpeed = actuatorTuningContext.getFloat("reverse-collector-speed", -1.0f);

        FloatInput collectorSpeedFromArm = Mixing.select(runCollectorWhileArmMoves, FloatInput.always(0f), reverseCollectorSpeed);

        Mixing.quadSelect(switchRollersIn, switchRollersOut, collectorSpeedFromArm, reverseCollectorSpeed, forwardCollectorSpeed, FloatInput.always(0f)).send(armCollectorMotor);
        switchRollersIn.or(switchRollersOut).send(armFingerSolenoids);

        Cluck.publish("Arm Main Solenoid", armMainSolenoid);
        Cluck.publish("Arm Lock Solenoid", armLockSolenoid);
        Cluck.publish("Arm Collector Motor", armCollectorMotor);
        Cluck.publish("Arm Finger Solenoids", armFingerSolenoids);
    }
}

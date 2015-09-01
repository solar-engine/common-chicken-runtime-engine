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
import ccre.channel.DerivedFloatInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.ctrl.ExpirationTimer;
import ccre.frc.FRC;
import ccre.holders.TuningContext;
import ccre.log.Logger;

public class Shooter {
    private static final TuningContext shooterTuningContext = new TuningContext("ShooterValues").publishSavingEvent();

    public static final BooleanStatus winchDisengaged = new BooleanStatus();
    public static final BooleanStatus rearming = new BooleanStatus();

    private static final FloatStatus joules = new FloatStatus();

    private static final FloatInput drawBackThreshold = shooterTuningContext.getFloat("Draw Back", 650);
    private static final FloatInput winchSpeedSetting = shooterTuningContext.getFloat("Winch Speed", 1f);
    private static final FloatInput rearmTimeout = shooterTuningContext.getFloat("Winch Rearm Timeout", 5f);

    private static final BooleanInput isArmInTheWay = Actuators.isSafeToShoot.not();
    private static final BooleanInput winchPastThreshold = joules.atLeast(drawBackThreshold);

    private static final FloatOutput winchMotor;
    private static final FloatInput winchCurrent;
    private static final BooleanOutput winchSolenoidDisengage;

    static {
        if (FRC.isRoboRIO()) {
            winchMotor = FRC.makeTalonMotor(6, FRC.MOTOR_REVERSE, FRC.NO_RAMPING);
            winchSolenoidDisengage = FRC.makeSolenoid(4);
            winchCurrent = FRC.getPDPChannelCurrent(12);
        } else {
            winchMotor = FRC.makeVictorMotor(5, FRC.MOTOR_REVERSE, FRC.NO_RAMPING);
            winchSolenoidDisengage = FRC.makeSolenoid(3);
            winchCurrent = FRC.makeAnalogInput(1);
        }
    }

    private static final FloatInput ampThreshold = shooterTuningContext.getFloat("Amp Threshold", 5f);

    private static final FloatInput amps = new DerivedFloatInput(winchCurrent, ampThreshold) {
        private final float tare = winchCurrent.get();

        protected float apply() {
            float o;
            if (FRC.isRoboRIO()) {
                o = winchCurrent.get() - tare;
            } else {
                o = (winchCurrent.get() - 0.60f) / 0.04f;
            }
            return o >= ampThreshold.get() ? o : 0;
        }
    };

    private static final FloatInput watts = amps.multipliedBy(FRC.getBatteryVoltage());

    static {
        // Update wattage total
        FRC.constantPeriodic.send(new EventOutput() {
            private long lastReadingAt = System.currentTimeMillis();

            public void event() {
                long now = System.currentTimeMillis();
                joules.set(joules.get() + watts.get() * (now - lastReadingAt) / 1000f);
                lastReadingAt = now;
            }
        });
    }

    // shouldUseCurrent = false

    public static void setup() {
        Cluck.publish("Winch Motor", winchMotor);
        Cluck.publish("Winch Solenoid", winchSolenoidDisengage);
        Cluck.publish("Winch Current", winchCurrent);
        EventInput fireWhen = AutonomousModeBase.getWhenToFire().or(UserInterface.getFireButton());

        winchDisengaged.setFalseWhen(FRC.startDisabled);
        rearming.setFalseWhen(FRC.startDisabled);

        winchDisengaged.send(winchSolenoidDisengage);
        Cluck.publish("Winch Disengaged", winchDisengaged);

        rearming.send(winchMotor.fromBoolean(0f, winchSpeedSetting));

        Cluck.publish("Winch Amps", amps);
        Cluck.publish("Winch Watts", watts);
        Cluck.publish("Winch Joules", joules);

        EventInput rearmEvent = UserInterface.getRearmCatapult();

        final FloatStatus resetRearm = new FloatStatus();
        resetRearm.setWhen(0, rearming.onRelease());
        Cluck.publish("Winch Rearm Timeout Status", (FloatInput) resetRearm);
        FRC.constantPeriodic.send(new EventOutput() {
            public void event() {
                float val = resetRearm.get();
                if (val > 0) {
                    val -= 0.01f;
                    if (val <= 0 && rearming.get()) {
                        Logger.info("Rearm Timeout");
                        rearming.set(false);
                    }
                    resetRearm.set(val);
                } else if (rearming.get()) {
                    resetRearm.set(rearmTimeout.get());
                }
            }
        });

        final EventOutput realFire = new EventOutput() {
            public void event() {
                ReadoutDisplay.displayAndLogError(1, "Firing", 500);
                winchDisengaged.set(true);
            }
        };
        Cluck.publish("Winch Force Fire", realFire);
        final ExpirationTimer fireAfterLower = new ExpirationTimer();

        EventOutput guardedFire = new EventOutput() {
            public void event() {
                if (rearming.get()) {
                    rearming.set(false);
                    ReadoutDisplay.displayAndLogError(5, "Cancelled rearm.", 1000);
                } else if (winchDisengaged.get()) {
                    ReadoutDisplay.displayAndLogError(3, "Winch not armed.", 2000);
                } else if (isArmInTheWay.get()) {
                    if (!fireAfterLower.isRunning()) { // We don't want to be trapped in an infinite loop if the arm doesn't lower.
                        ReadoutDisplay.displayAndLogError(4, "Autolowering arm.", 1000);
                        fireAfterLower.start();
                    } else {
                        ReadoutDisplay.displayAndLogError(4, "Autolower failed.", 1000);
                    }
                } else {
                    realFire.event();
                }
            }
        };

        fireAfterLower.schedule(50, Actuators.armLowerForShooter);
        fireAfterLower.schedule(1200, guardedFire);
        fireAfterLower.schedule(1300, fireAfterLower.getStopEvent());

        fireWhen.send(guardedFire);

        AutonomousModeBase.getWhenToRearm().or(rearmEvent).send(new EventOutput() {
            public void event() {
                if (rearming.get()) {
                    rearming.set(false);
                    ReadoutDisplay.displayAndLogError(5, "Cancelled rearm.", 1000);
                } else if (isArmInTheWay.get()) {
                    ReadoutDisplay.displayAndLogError(4, "Arm isn't down.", 500);
                } else {
                    winchDisengaged.set(false);
                    ReadoutDisplay.displayAndLogError(1, "Started rearming.", 500);
                    rearming.set(true);
                    joules.set(0);
                }
            }
        });
        FRC.globalPeriodic.send(new EventOutput() {
            public void event() {
                if (rearming.get() && winchPastThreshold.get()) {
                    rearming.set(false);
                    ReadoutDisplay.displayAndLogError(2, "Hit current limit.", 1000);
                    AutonomousModeBase.notifyRearmFinished();
                }
            }
        });

        ReadoutDisplay.showWinchStatus(joules);
        UserInterface.showFiring(winchDisengaged);
    }

    public static BooleanInput getShouldDisableDrivingAndCompressor() {
        return rearming;
    }
}

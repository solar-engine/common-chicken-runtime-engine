/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 * 
 * This file is part of the ApolloGemini2014 project.
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
package org.team1540.apollogemini;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventLogger;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.holders.TuningContext;
import ccre.igneous.Igneous;
import ccre.log.LogLevel;
import ccre.log.Logger;

public class Shooter {

    private final EventInput periodic, constantPeriodic;
    private final TuningContext tuner = new TuningContext(Cluck.getNode(), "ShooterValues");
    public final BooleanStatus winchDisengaged = new BooleanStatus();
    public final BooleanStatus rearming = new BooleanStatus();
    private BooleanInputPoll winchPastThreshold;
    private final BooleanInputPoll isArmInTheWay;
    private final FloatInputPoll batteryLevel;
    private FloatInputPoll sensor;
    private EventOutput lowerArm, guardedFire;
    public final FloatStatus totalPowerTaken = new FloatStatus();
    public final BooleanStatus shouldUseCurrent = new BooleanStatus();

    private final FloatInput winchSpeed = tuner.getFloat("Winch Speed", 1f);
    private final FloatInput drawBack = tuner.getFloat("Draw Back", 650);
    private final FloatInput drawBackExtra = tuner.getFloat("Extra Draw Back in Auto", 0);
    private final FloatInput drawBackCurrent = tuner.getFloat("Draw Back Current", 50);
    private final FloatInput rearmTimeout = tuner.getFloat("Winch Rearm Timeout", 5f);
    private final FloatInput ampThreshold = tuner.getFloat("Amp Threshold", 5f);

    public final FloatInputPoll activeAmps = new FloatInputPoll() {
        private Float tare = null;

        public float get() {
            if (sensor == null) {
                return -100; // Hacky solution. Fixed in rewrite.
            }
            float o;
            if (Igneous.isRoboRIO()) {
                o = sensor.get();
                if (tare == null) {
                    tare = o;
                }
                o -= tare;
            } else {
                o = (sensor.get() - 0.60f) / 0.04f;
            }
            return o >= ampThreshold.get() ? o : 0;
        }
    };

    private final FloatInputPoll activeWatts = new FloatInputPoll() {
        public float get() {
            return activeAmps.get() * batteryLevel.get();
        }
    };

    private final EventOutput updateTotal = new EventOutput() {
        public void event() {
            totalPowerTaken.set(totalPowerTaken.get() + activeWatts.get() / 100);
        }
    };

    public Shooter(EventInput resetModule, EventInput periodic, EventInput constantPeriodic, final BooleanInputPoll isArmNotInTheWay, final FloatInputPoll batteryLevel) {
        this.periodic = periodic;
        this.constantPeriodic = constantPeriodic;
        this.batteryLevel = batteryLevel;
        constantPeriodic.send(updateTotal);
        winchDisengaged.setFalseWhen(resetModule);
        rearming.setFalseWhen(resetModule);
        tuner.publishSavingEvent("Shooter");
        this.isArmInTheWay = BooleanMixing.invert(isArmNotInTheWay);
        Cluck.publish("Constant", constantPeriodic);
        Cluck.publish("ActiveAmps", FloatMixing.createDispatch(activeAmps, constantPeriodic));
        Cluck.publish("ActiveWatts", FloatMixing.createDispatch(activeWatts, constantPeriodic));
        Cluck.publish("TotalWatts", totalPowerTaken);
        Cluck.publish("ShouldUseCurrent", shouldUseCurrent);
    }

    public void setupArmLower(EventOutput enc, BooleanOutput runCollector) {
        ExpirationTimer fireAfterLower = new ExpirationTimer();
        fireAfterLower.schedule(50, enc);
        fireAfterLower.scheduleBooleanPeriod(40, 1100, runCollector, true);
        if (this.guardedFire == null || enc == null) {
            throw new NullPointerException();
        }
        fireAfterLower.schedule(1200, this.guardedFire);
        fireAfterLower.schedule(1300, fireAfterLower.getStopEvent());
        lowerArm = fireAfterLower.getStartEvent();
    }

    public void setupWinch(final FloatOutput winchMotor, final BooleanOutput winchSolenoid,
            final FloatInputPoll winchCurrent, final BooleanInputPoll isAutonomous) {
        sensor = winchCurrent;
        winchDisengaged.send(winchSolenoid);
        Cluck.publish("Winch Disengaged", winchDisengaged);
        winchPastThreshold = new BooleanInputPoll() {
            public boolean get() {
                return shouldUseCurrent.get() ? activeAmps.get() >= drawBackCurrent.get() : totalPowerTaken.get() >= (isAutonomous.get() ? drawBackExtra.get() : 0) + drawBack.get();
            }
        };
        rearming.send(Mixing.select(winchMotor, FloatMixing.always(0), winchSpeed));
    }

    public void setupRearmTimeout() {
        final FloatStatus resetRearm = new FloatStatus();
        resetRearm.setWhen(0, BooleanMixing.whenBooleanBecomes(rearming, false));
        Cluck.publish("Winch Rearm Timeout Status", (FloatInput) resetRearm);
        constantPeriodic.send(new EventOutput() {
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
    }

    private void autolowerArm() {
        Logger.info("Autolower!");
        lowerArm.event();
    }

    public void handleShooterButtons(
            final EventInput rearmTrigger, EventInput fireButton, final EventOutput finishedRearm) {
        final EventOutput realFire = EventMixing.combine(
                new EventLogger(LogLevel.INFO, "Fire Begin"),
                winchDisengaged.getSetTrueEvent());
        Cluck.publish("Force Fire", realFire);
        fireButton.send(this.guardedFire = new EventOutput() {
            public void event() {
                if (rearming.get()) {
                    Logger.info("fire button: stop rearm");
                    rearming.set(false);
                    ErrorMessages.displayError(5, "Cancelled rearm.", 1000);
                } else if (winchDisengaged.get()) {
                    Logger.info("no fire: run the winch!");
                    ErrorMessages.displayError(3, "Winch not armed.", 2000);
                } else if (isArmInTheWay.get()) {
                    Logger.info("no fire: autolowering the arm.");
                    ErrorMessages.displayError(4, "Autolowering arm.", 1000);
                    autolowerArm();
                } else {
                    realFire.event();
                    ErrorMessages.displayError(1, "Firing", 500);
                }
            }
        });
        rearmTrigger.send(new EventOutput() {
            public void event() {
                if (rearming.get()) {
                    Logger.info("stop rearm");
                    rearming.set(false);
                    ErrorMessages.displayError(5, "Cancelled rearm.", 1000);
                } else if (isArmInTheWay.get()) {
                    Logger.info("no rearm: lower the arm!");
                    ErrorMessages.displayError(4, "Arm isn't down.", 500);
                } else {
                    winchDisengaged.set(false);
                    Logger.info("rearm");
                    ErrorMessages.displayError(1, "Started rearming.", 500);
                    rearming.set(true);
                    totalPowerTaken.set(0);
                }
            }
        });
        periodic.send(new EventOutput() {
            public void event() {
                if (rearming.get() && winchPastThreshold.get()) {
                    rearming.set(false);
                    Logger.info("drawback current stop rearm");
                    ErrorMessages.displayError(2, "Hit current limit.", 1000);
                    finishedRearm.event();
                }
            }
        });
    }
}

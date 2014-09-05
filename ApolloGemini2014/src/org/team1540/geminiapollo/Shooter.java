package org.team1540.geminiapollo;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.ctrl.*;
import ccre.holders.TuningContext;
import ccre.log.*;

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
    private final FloatInput drawBack = tuner.getFloat("Draw Back", 605);
    private final FloatInput drawBackExtra = tuner.getFloat("Extra Draw Back in Auto", 0);
    private final FloatInput drawBackCurrent = tuner.getFloat("Draw Back Current", 50);
    private final FloatInput rearmTimeout = tuner.getFloat("Winch Rearm Timeout", 5f);
    private final FloatInput ampThreshold = tuner.getFloat("Amp Threshold", 5f);

    public final FloatInputPoll activeAmps = new FloatInputPoll() {
        public float get() {
            if (sensor == null) {
                return -100; // TODO: Remove this later.
            }
            float o = (sensor.get() - 0.60f) / 0.04f;
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
        constantPeriodic.send(updateTotal); // TODO Move this after the shooter is registered.
        winchDisengaged.setFalseWhen(resetModule);
        rearming.setFalseWhen(resetModule);
        tuner.publishSavingEvent("Shooter");
        this.isArmInTheWay = BooleanMixing.invert(isArmNotInTheWay);
        this.batteryLevel = batteryLevel;
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

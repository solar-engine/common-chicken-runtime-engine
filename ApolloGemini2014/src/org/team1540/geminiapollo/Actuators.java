package org.team1540.geminiapollo;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.holders.TuningContext;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;
import ccre.log.Logger;

public class Actuators {

    public final EventOutput armUp, armDown, armAlign;
    private final TuningContext actuatorContext = new TuningContext(Cluck.getNode(), "Actuators").publishSavingEvent("Actuators");
    private final FloatStatus movementUpDelay = actuatorContext.getFloat("arm-up-delay", 0.3f);
    private final FloatStatus movementDownDelay = actuatorContext.getFloat("arm-hover-delay", 0.8f);
    private final FloatStatus collectorSpeed = actuatorContext.getFloat("collector-speed", 1f);
    public static final int STATE_UP = 0, STATE_DOWN = 1, STATE_ALIGN = 2;

    public Actuators(BooleanInputPoll shouldBeRunning, final BooleanInputPoll isTeleop, EventInput updateDuring, final BooleanOutput isSafeToShoot, final BooleanOutput isArmLower,
            final BooleanOutput isArmRaise, final BooleanOutput armMain, final BooleanOutput armLock) {
        final BooleanStatus pressedUp = new BooleanStatus(), pressedDown = new BooleanStatus(), pressedAlign = new BooleanStatus();
        armUp = pressedUp.getSetTrueEvent();
        armDown = pressedDown.getSetTrueEvent();
        armAlign = pressedAlign.getSetTrueEvent();
        new InstinctModule(shouldBeRunning) {
            private void resetInputs() {
                pressedUp.set(false);
                pressedDown.set(false);
                pressedAlign.set(false);
            }

            protected String getTypeName() {
                return "actuator control loop";
            }

            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                if (isTeleop.get()) {
                    isArmLower.set(false);
                    isArmRaise.set(false);
                    waitForTime(80);
                    isArmLower.set(true);
                    isArmRaise.set(false);
                    waitForTime(80);
                    isArmLower.set(false);
                    isArmRaise.set(true);
                    waitForTime(80);
                    isArmLower.set(false);
                    isArmRaise.set(false);
                    waitForTime(80);
                }
                int next = 0;
                while (true) {
                    resetInputs();
                    isSafeToShoot.set(next != STATE_UP);
                    if (next == STATE_UP) { // Up
                        Logger.fine("Actuator state: UP");
                        armLock.set(false);
                        armMain.set(false);
                        isArmLower.set(true);
                        isArmRaise.set(false);
                        if (waitUntilOneOf(new BooleanInputPoll[]{pressedDown, pressedAlign}) == 0) {
                            next = STATE_DOWN;
                        } else {
                            isArmLower.set(true);
                            isArmRaise.set(true);
                            armMain.set(true);
                            waitForTime((long) (movementDownDelay.get() * 1000L));
                            next = STATE_ALIGN;
                        }
                    } else if (next == 1) { // Down
                        Logger.fine("Actuator state: DOWN");
                        armLock.set(false);
                        armMain.set(true);
                        isArmLower.set(false);
                        isArmRaise.set(true);
                        if (waitUntilOneOf(new BooleanInputPoll[]{pressedUp, pressedAlign}) == 0) {
                            next = STATE_UP;
                        } else {
                            next = STATE_ALIGN;
                        }
                    } else if (next == 2) { // Align
                        Logger.fine("Actuator state: ALIGN");
                        armLock.set(true);
                        armMain.set(false);
                        isArmLower.set(false);
                        isArmRaise.set(false);
                        if (waitUntilOneOf(new BooleanInputPoll[]{pressedDown, pressedUp}) == 0) {
                            next = STATE_DOWN;
                        } else {
                            isSafeToShoot.set(false);
                            isArmLower.set(true);
                            isArmRaise.set(true);
                            armMain.set(true);
                            waitForTime((long) (movementUpDelay.get() * 1000L));
                            next = STATE_UP;
                        }
                    } else {
                        next = STATE_UP;
                        Logger.warning("Bad Actuator state! Resetting to up.");
                    }
                }
            }
        }.updateWhen(updateDuring);
    }

    public void createCollector(EventInput during, FloatOutput collectorMotor, BooleanOutput openFingers,
            final BooleanInputPoll rollersIn, final BooleanInputPoll rollersOut, BooleanInputPoll disableCollector, BooleanInputPoll overrideRoll) {
        during.send(EventMixing.filterEvent(disableCollector, true, EventMixing.filterEvent(rollersIn, true, new EventOutput() {
            public void event() {
                ErrorMessages.displayError(2, "Collect: not winch!", 200);
            }
        })));
        FloatMixing.pumpWhen(during, Mixing.quadSelect(BooleanMixing.orBooleans(rollersIn, overrideRoll), rollersOut,
                FloatMixing.always(0f), FloatMixing.negate((FloatInputPoll) collectorSpeed),
                Mixing.select(disableCollector, collectorSpeed, FloatMixing.always(0)), FloatMixing.negate((FloatInputPoll) collectorSpeed)),
                collectorMotor);
        BooleanMixing.pumpWhen(during, BooleanMixing.orBooleans(rollersIn, rollersOut), openFingers);
    }
}

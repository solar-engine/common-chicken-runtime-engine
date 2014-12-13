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

import ccre.channel.BooleanInputPoll;
import ccre.channel.FloatInput;
import ccre.ctrl.FloatMixing;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;
import ccre.util.Utils;

public class AutonomousModes extends AutonomousFramework {
    public AutonomousModes() {
        super("none", "forward", "hotcheck", "double"); // Must match switch statement in runAutonomousMode.
    }

    @Override
    protected void runAutonomousMode(int modeId, String modeName) throws InterruptedException, AutonomousModeOverException {
        switch (modeId) { // Must match array initializer in construct.
        case 0: // none
            Logger.info("I'm a sitting chicken!");
            break;
        case 1: // forward
            modeForward();
            break;
        case 2: // hotcheck
            modeHotcheck();
            break;
        case 3: // double
            modeDouble();
            break;
        }
    }

    private final FloatInput forwardMovement = autonomousTuningContext.getFloat("autom-forward-speed", -1f);
    private final FloatInput forwardDelay = autonomousTuningContext.getFloat("autom-forward-delay", 0.5f);

    private void modeForward() throws InterruptedException, AutonomousModeOverException {
        driveFor(forwardMovement, forwardDelay);
    }

    private final FloatInput hotcheckAlignDuration = autonomousTuningContext.getFloat("autom-hotcheck-align-duration", 0.3f);
    private final FloatInput hotcheckAlignSpeed = autonomousTuningContext.getFloat("autom-hotcheck-align-speed", -1f);
    private final FloatInput hotcheckTimeoutAfter = autonomousTuningContext.getFloat("autom-hotcheck-timeout", 4);
    private final FloatInput hotcheckPostFirePause = autonomousTuningContext.getFloat("autom-hotcheck-postfire-pause", 0.5f);
    private final FloatInput hotcheckPostFireSpeed = autonomousTuningContext.getFloat("autom-hotcheck-postfire-speed", -0.7f);
    private final FloatInput hotcheckPostFireMove = autonomousTuningContext.getFloat("autom-hotcheck-postfire-move", 0);
    private final FloatInput hotcheckCollectorSpeed = autonomousTuningContext.getFloat("autom-hotcheck-collector", 0.5f);
    private final FloatInput hotcheckPreFirePause = autonomousTuningContext.getFloat("autom-hotcheck-prefire-pause", 1);
    private final FloatInput hotcheckArmMoveTime = autonomousTuningContext.getFloat("autom-hotcheck-armmove-time", .6f);

    private void modeHotcheck() throws InterruptedException, AutonomousModeOverException {
        driveFor(hotcheckAlignSpeed, hotcheckAlignDuration);
        if (hotcheckArmMoveTime.get() < 0.02f) { // Is the arm movement time insignificant?
            Logger.fine("Skip arm movement");
        } else {
            startCollection(hotcheckCollectorSpeed);
            raiseArm(hotcheckArmMoveTime);
            lowerArm(hotcheckArmMoveTime);
            stopCollection();
        }
        startAligningArm();
        waitForHotZoneOrTimeout(hotcheckTimeoutAfter);

        waitForTime(hotcheckPreFirePause);
        fireShooter();
        waitForTime(hotcheckPostFirePause);

        driveFor(hotcheckPostFireSpeed, hotcheckPostFireMove);
    }

    private void waitForHotZoneOrTimeout(FloatInput hotcheckTimeoutAfter2) throws AutonomousModeOverException, InterruptedException {
        float timeoutAt = Utils.getCurrentTimeSeconds() + hotcheckTimeoutAfter.get();
        BooleanInputPoll hasTimedOut = FloatMixing.floatIsAtLeast(Utils.currentTimeSeconds, timeoutAt);
        if (waitUntilOneOf(getHotZoneActive(), hasTimedOut) != 0) {
            Logger.warning("Cancelled HotZone wait after " + hotcheckTimeoutAfter.get() + " secs: " + Utils.getCurrentTimeSeconds() + "," + timeoutAt);
        } else {
            Logger.fine("Found HotZone");
        }
    }

    private final FloatInput doubleArmMoveTime = autonomousTuningContext.getFloat("autom-double-armmove-time", 0.9f);
    private final FloatInput halfDoubleArmMoveTime = FloatMixing.multiplication.of(doubleArmMoveTime, 0.5f);
    private final FloatInput doubleFireTime = autonomousTuningContext.getFloat("autom-double-fire-time", 0.7f);
    private final FloatInput doubleCollectorHoldSpeed = autonomousTuningContext.getFloat("autom-double-collector-hold-speed", 1f);
    private final FloatInput doubleCollectSpeed = autonomousTuningContext.getFloat("autom-double-collect-speed", 1f);
    private final FloatInput doubleCollectTime = autonomousTuningContext.getFloat("autom-double-collect-time", 0.9f);
    private final FloatInput doubleAlignSpeed1 = autonomousTuningContext.getFloat("autom-double-align1-speed", -1.0f);
    private final FloatInput doubleAlignTime1 = autonomousTuningContext.getFloat("autom-double-align1-time", 0.4f);
    private final FloatInput doubleAlignSpeed2 = autonomousTuningContext.getFloat("autom-double-align2-speed", 1.0f);
    private final FloatInput doubleAlignTime2 = autonomousTuningContext.getFloat("autom-double-align2-time", 0.5f);

    private void modeDouble() throws InterruptedException, AutonomousModeOverException {
        startRearming();
        if (doubleAlignTime1.get() > 0.02) { // Is the alignment time significant?
            Logger.fine("Aligning...");
            driveFor(doubleAlignSpeed1, doubleAlignTime1);
            Logger.fine("Aligned!");
        }

        startCollection(doubleCollectSpeed);
        lowerArm(halfDoubleArmMoveTime); // cut the time into two parts so that we can stop collection in the middle.
        stopCollection();
        waitForTime(halfDoubleArmMoveTime);
        waitUntilRearmed();

        Logger.fine("Arm moved and shooter rearmed - firing!");
        fireShooter();
        waitForTime(doubleFireTime);

        startLoweringArm();
        startRearming();
        openFingers();

        Logger.fine("Rearming... (and driving)");

        startCollection(doubleCollectorHoldSpeed);
        driveFor(doubleAlignSpeed2, doubleAlignTime2);
        Logger.fine("Drove!");
        stopCollection();
        waitUntilRearmed();

        Logger.fine("Rearmed!");

        startCollection(doubleCollectSpeed);
        waitForTime(doubleCollectTime);
        stopCollection();

        startLoweringArm();
        closeFingers();

        if (doubleAlignTime2.get() > 0.02) { // Is the alignment time significant?
            driveFor(doubleAlignSpeed2, doubleAlignTime2);
        }

        waitForTime(doubleArmMoveTime);

        fireShooter();
        waitForTime(doubleFireTime);
        startRaisingArm();
    }
}

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

import ccre.channel.FloatInput;
import ccre.holders.TuningContext;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public class AutonomousModeHotcheck extends AutonomousModeBase {

    public AutonomousModeHotcheck() {
        super("hotcheck");
    }

    private FloatInput collectorSpeed, alignDuration, postFirePause,
            postFireSpeed, postFireDuration, timeoutAfter, preFirePause,
            armMoveTime, alignSpeed;

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveFor(alignSpeed, alignDuration);
        if (armMoveTime.get() < 0.02f) { // Is the arm movement time insignificant?
            Logger.fine("Skip arm movement");
        } else {
            startCollection(collectorSpeed);
            raiseArm(armMoveTime);
            lowerArm(armMoveTime);
            stopCollection();
        }
        startAligningArm();
        waitForHotZoneOrTimeout(timeoutAfter);

        waitForTime(preFirePause);
        fireShooter();
        waitForTime(postFirePause);

        driveFor(postFireSpeed, postFireDuration);
    }

    private void waitForHotZoneOrTimeout(FloatInput timeout) throws AutonomousModeOverException, InterruptedException {
        if (waitUntil((long) (timeout.get() * 1000), getHotZoneActive())) {
            Logger.fine("Found HotZone");
        } else {
            Logger.warning("Cancelled HotZone wait after " + timeout.get());
        }
    }

    @Override
    public void loadSettings(TuningContext context) {
        alignDuration = context.getFloat("autom-hotcheck-align-duration", 0.3f);
        alignSpeed = context.getFloat("autom-hotcheck-align-speed", -1f);
        timeoutAfter = context.getFloat("autom-hotcheck-timeout", 4);
        postFirePause = context.getFloat("autom-hotcheck-postfire-pause", 0.5f);
        postFireSpeed = context.getFloat("autom-hotcheck-postfire-speed", -0.7f);
        postFireDuration = context.getFloat("autom-hotcheck-postfire-move", 0);
        collectorSpeed = context.getFloat("autom-hotcheck-collector", 0.5f);
        preFirePause = context.getFloat("autom-hotcheck-prefire-pause", 1);
        armMoveTime = context.getFloat("autom-hotcheck-armmove-time", .6f);
    }
}

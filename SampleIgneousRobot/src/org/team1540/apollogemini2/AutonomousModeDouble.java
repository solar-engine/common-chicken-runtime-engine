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
import ccre.ctrl.FloatMixing;
import ccre.holders.TuningContext;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public class AutonomousModeDouble extends AutonomousModeBase {

    public AutonomousModeDouble() {
        super("double");
    }

    private FloatInput armMoveTime, fireTime, collectorSpeed, collectTime,
            collectorHoldSpeed, alignSpeed1, alignTime1, alignSpeed2,
            alignTime2, halfArmMoveTime;

    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        startRearming();
        if (alignTime1.get() > 0.02) { // Only if the alignment time is significant
            Logger.fine("Aligning...");
            driveFor(alignSpeed1, alignTime1);
            Logger.fine("Aligned!");
        }

        startCollection(collectorSpeed);
        lowerArm(halfArmMoveTime); // cut the time into two parts so that we can stop collection in the middle.
        stopCollection();
        waitForTime(halfArmMoveTime);
        waitUntilRearmed();

        Logger.fine("Arm moved and shooter rearmed - firing!");
        fireShooter();
        waitForTime(fireTime);

        startLoweringArm();
        startRearming();
        openFingers();

        Logger.fine("Rearming... (and driving)");

        startCollection(collectorHoldSpeed);
        driveFor(alignSpeed2, alignTime2);
        Logger.fine("Drove!");
        stopCollection();
        waitUntilRearmed();

        Logger.fine("Rearmed!");

        startCollection(collectorSpeed);
        waitForTime(collectTime);
        stopCollection();

        startLoweringArm();
        closeFingers();

        if (alignTime2.get() > 0.02) { // Only if the alignment time is significant
            driveFor(alignSpeed2, alignTime2);
        }

        waitForTime(armMoveTime);

        fireShooter();
        waitForTime(fireTime);
        startRaisingArm();
    }

    @Override
    public void loadSettings(TuningContext context) {
        armMoveTime = context.getFloat("autom-double-armmove-time", 0.9f);
        halfArmMoveTime = FloatMixing.multiplication.of(armMoveTime, 0.5f);
        fireTime = context.getFloat("autom-double-fire-time", 0.7f);
        collectorHoldSpeed = context.getFloat("autom-double-collector-hold-speed", 1f);
        collectorSpeed = context.getFloat("autom-double-collect-speed", 1f);
        collectTime = context.getFloat("autom-double-collect-time", 0.9f);
        alignSpeed1 = context.getFloat("autom-double-align1-speed", -1.0f);
        alignTime1 = context.getFloat("autom-double-align1-time", 0.4f);
        alignSpeed2 = context.getFloat("autom-double-align2-speed", 1.0f);
        alignTime2 = context.getFloat("autom-double-align2-time", 0.5f);
    }
}

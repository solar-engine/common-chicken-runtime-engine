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
import ccre.channel.EventOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.holders.TuningContext;

public class DriveCode {

    public static void createDrive(EventInput begin, EventInput during,
            final FloatOutput leftDrive, final FloatOutput rightDrive,
            final FloatInputPoll leftDriveAxis, final FloatInputPoll rightDriveAxis,
            final FloatInputPoll forwardDriveAxis, final BooleanStatus notShifted,
            BooleanInputPoll overrideDisabled, BooleanInputPoll disableDriving,
            final BooleanInputPoll isKidMode) {

        final BooleanInputPoll actuallyDisable = BooleanMixing.andBooleans(disableDriving, BooleanMixing.invert(overrideDisabled));

        TuningContext wheelTuner = new TuningContext(Cluck.getNode(), "DriveTuning");
        wheelTuner.publishSavingEvent("Drive Tuning");
        final FloatStatus hfLeft = wheelTuner.getFloat("High Left Fwd", 1f), hfRight = wheelTuner.getFloat("High Right Fwd", 1f);
        final FloatStatus hbLeft = wheelTuner.getFloat("High Left Bck", 1f), hbRight = wheelTuner.getFloat("High Right Bck", 1f);
        final FloatStatus lfLeft = wheelTuner.getFloat("Low Left Fwd", 1f), lfRight = wheelTuner.getFloat("Low Right Fwd", 1f);
        final FloatStatus lbLeft = wheelTuner.getFloat("Low Left Bck", 1f), lbRight = wheelTuner.getFloat("Low Right Bck", 1f);

        during.send(new EventOutput() {
            public void event() {
                float leftDriveValue = leftDriveAxis.get() + forwardDriveAxis.get();
                float rightDriveValue = rightDriveAxis.get() + forwardDriveAxis.get();
                if (actuallyDisable.get()) {
                    leftDriveValue = rightDriveValue = 0;
                } else if (!notShifted.get()) {
                    leftDriveValue *= leftDriveValue > 0 ? hfLeft.get() : hbLeft.get();
                    rightDriveValue *= rightDriveValue > 0 ? hfRight.get() : hbRight.get();
                } else {
                    leftDriveValue *= leftDriveValue > 0 ? lfLeft.get() : lbLeft.get();
                    rightDriveValue *= rightDriveValue > 0 ? lfRight.get() : lbRight.get();
                }
                if (isKidMode.get()) {
                    leftDriveValue /= 2;
                    rightDriveValue /= 2;
                }
                leftDrive.set(leftDriveValue);
                rightDrive.set(rightDriveValue);
            }
        });
    }

    public static BooleanStatus createShifting(EventInput begin, EventInput beginAuto, EventInput during, BooleanOutput shiftSolenoid, EventInput shiftHighButton, EventInput shiftLowButton) {
        final BooleanStatus shifted = new BooleanStatus(shiftSolenoid);
        shifted.setTrueWhen(begin); // begin
        shifted.setFalseWhen(beginAuto);
        shifted.setTrueWhen(shiftHighButton); // high
        shifted.setFalseWhen(shiftLowButton); // low
        return shifted;
    }
}

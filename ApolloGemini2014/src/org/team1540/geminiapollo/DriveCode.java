package org.team1540.geminiapollo;

import ccre.channel.*;
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

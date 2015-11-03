package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.instinct.InstinctModule;

public class Presentation12 implements FRCApplication {

    public void setupRobot() {
        FloatOutput left = FRC.talon(0, FRC.MOTOR_FORWARD);
        FloatOutput right = FRC.talon(1, FRC.MOTOR_REVERSE);
        BooleanInput bumper = FRC.digitalInput(0);

        // Eclipse will autocomplete all of this.
        // You don't need much typing.
        FRC.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain() throws Throwable {
                left.set(+1.0f);
                right.set(+1.0f);
                waitUntil(bumper);
                left.set(0);
                right.set(0);
            }
        });
    }
}

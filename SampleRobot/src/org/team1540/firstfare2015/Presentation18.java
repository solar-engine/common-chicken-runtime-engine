package org.team1540.firstfare2015;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.Joystick;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.instinct.InstinctModule;

public class Presentation18 implements FRCApplication {

    public void setupRobot() {
        FloatOutput leftMotors = FRC.talon(0, FRC.MOTOR_FORWARD).combine(FRC.talon(1, FRC.MOTOR_FORWARD));
        FloatOutput rightMotors = FRC.talon(2, FRC.MOTOR_REVERSE).combine(FRC.talon(3, FRC.MOTOR_REVERSE));

        Joystick drive = FRC.joystick1, copilot = FRC.joystick2;

        DriverImpls.arcadeDrive(drive, leftMotors, rightMotors);

        BooleanOutput shifting = FRC.solenoid(0);
        shifting.setFalseWhen(FRC.startTele);
        shifting.setTrueWhen(drive.onPress(1));
        shifting.setFalseWhen(drive.onPress(2));

        FloatOutput collector = FRC.talon(4, FRC.MOTOR_FORWARD);
        copilot.axis(4).send(collector);

        BooleanCell arm = new BooleanCell(FRC.solenoid(1));
        arm.toggleWhen(copilot.onPress(1));

        FRC.registerAutonomous(new InstinctModule() {
            FloatOutput allMotors = leftMotors.combine(rightMotors);

            protected void autonomousMain() throws Throwable {
                // Put arm down.
                arm.set(true);
                waitForTime(300);

                // Drive forward.
                allMotors.set(0.6f);
                waitForTime(500);
                allMotors.set(0.0f);

                // Arm up; collect.
                arm.set(false);
                collector.set(-1);
                waitForTime(300);
                collector.set(0);
            }
        });
    }
}

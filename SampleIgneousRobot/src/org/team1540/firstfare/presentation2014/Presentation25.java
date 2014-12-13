/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

public class Presentation25 implements IgneousApplication {

    public void setupRobot() {
        final FloatOutput leftMotors = FloatMixing.combine(
                Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f),
                Igneous.makeTalonMotor(2, Igneous.MOTOR_FORWARD, 0.1f));
        final FloatOutput rightMotors = FloatMixing.combine(
                Igneous.makeTalonMotor(1, Igneous.MOTOR_REVERSE, 0.1f),
                Igneous.makeTalonMotor(3, Igneous.MOTOR_REVERSE, 0.1f));
        final FloatOutput collector =
                Igneous.makeTalonMotor(4, Igneous.MOTOR_FORWARD, 0.1f);
        BooleanStatus shifting = new BooleanStatus(Igneous.makeSolenoid(0));
        final BooleanStatus arm = new BooleanStatus(Igneous.makeSolenoid(1));
        IJoystick drive = Igneous.joystick1, copilot = Igneous.joystick2;

        // Driving
        DriverImpls.createSynchSingleJoystickDriver(Igneous.duringTele,
                drive.getXChannel(), drive.getYChannel(),
                leftMotors, rightMotors);

        // Shifting
        shifting.setFalseWhen(Igneous.startTele);
        shifting.setTrueWhen(drive.getButtonSource(1));
        shifting.setFalseWhen(drive.getButtonSource(2));

        // Collector
        FloatMixing.pumpWhen(Igneous.duringTele,
                copilot.getAxisChannel(4), collector); // Throttle

        // Arm
        arm.toggleWhen(copilot.getButtonSource(1));

        // Autonomous
        Igneous.registerAutonomous(new InstinctModule() {
            FloatOutput allMotors = FloatMixing.combine(leftMotors, rightMotors);

            @Override
            protected void autonomousMain()
                    throws AutonomousModeOverException, InterruptedException {
                arm.set(true);
                waitForTime(300);

                allMotors.set(0.6f);
                waitForTime(500);
                allMotors.set(0);

                arm.set(false);
                collector.set(-1);
                waitForTime(300);
                collector.set(0);
            }
        });
    }
}

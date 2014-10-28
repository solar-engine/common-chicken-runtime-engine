package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

public class Presentation12 implements IgneousApplication {

    public void setupRobot() {
        FloatOutput leftMotor =
                Igneous.makeTalonMotor(1, Igneous.MOTOR_FORWARD, 0.1f);
        FloatOutput rightMotor =
                Igneous.makeTalonMotor(2, Igneous.MOTOR_REVERSE, 0.1f);
        final FloatOutput bothMotors =
                FloatMixing.combine(leftMotor, rightMotor);
        final BooleanInputPoll bumper = Igneous.makeDigitalInput(1), shooterArmed = null;
        final EventInput cancelEvent = null;
        final FloatInputPoll airPressure = null, speed = null;
        final EventOutput launchBall = EventMixing.ignored;
        Igneous.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain()
                    throws AutonomousModeOverException, InterruptedException {
                bothMotors.set(0.5f);
                waitForTime(500); // half of a second
                bothMotors.set(-0.5f);
                waitUntil(bumper); // BooleanInputPoll
                bothMotors.set(0);
                waitUntilAtLeast(airPressure, 0.5f); // FloatInputPoll
                launchBall.event();
                bothMotors.set(1.0f);
                waitUntilOneOf(bumper, BooleanMixing.invert(shooterArmed));
                // BooleanInputPolls
                bothMotors.set(0);
                waitUntilAtMost(speed, 0.05f); // FloatInputPoll
                bothMotors.set(-0.5f);
                waitForEvent(cancelEvent); // EventInput
            }
        });
    }
}

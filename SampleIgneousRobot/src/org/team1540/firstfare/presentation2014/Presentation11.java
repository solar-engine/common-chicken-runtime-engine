/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.FloatOutput;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

public class Presentation11 implements IgneousApplication {

    public void setupRobot() {
        FloatOutput leftMotor =
                Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f);
        FloatOutput rightMotor =
                Igneous.makeTalonMotor(1, Igneous.MOTOR_REVERSE, 0.1f);
        final FloatOutput bothMotors =
                FloatMixing.combine(leftMotor, rightMotor);
        Igneous.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain()
                    throws AutonomousModeOverException, InterruptedException {
                bothMotors.set(0.5f);
                waitForTime(5000); // five seconds
                bothMotors.set(0);
            }
        });
    }
}

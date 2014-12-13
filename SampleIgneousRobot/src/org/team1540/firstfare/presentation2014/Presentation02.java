/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation02 implements IgneousApplication {

    public void setupRobot() {
        FloatOutput motor =
                Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f);
        FloatInput axis = Igneous.joystick1.getAxisSource(2);
        axis.send(motor);
    }
}

/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.FloatStatus;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation16 implements IgneousApplication {

    public void setupRobot() {
        FloatStatus status = new FloatStatus(
                Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f));
        status.setWhen(0.0f, Igneous.startDisabled);
        status.setWhen(0.5f, Igneous.startTele);
    }
}

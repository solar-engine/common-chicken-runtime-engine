/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.FloatOutput;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation07 implements IgneousApplication {

    public void setupRobot() {
        FloatOutput motorTalon =
                Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f);
        FloatOutput motorVictor =
                Igneous.makeVictorMotor(1, Igneous.MOTOR_REVERSE, 0.05f);
        FloatOutput motorJaguar =
                Igneous.makeJaguarMotor(2, Igneous.MOTOR_FORWARD, 0);

        FloatOutput combined =
                FloatMixing.combine(motorTalon, motorVictor, motorJaguar);

        combined.set(0.5f);

        // change per ten milliseconds
        // 0.01f = 2 seconds to go from full forward to full reverse.
        // 0.02f = 1 second.
        // 0.1f = 200 milliseconds. <<-- good default
        // 1.0f = 20 milliseconds.
    }
}

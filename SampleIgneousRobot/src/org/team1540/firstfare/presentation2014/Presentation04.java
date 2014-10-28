package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation04 implements IgneousApplication {

    public void setupRobot() {
        FloatOutput motor =
                Igneous.makeVictorMotor(1, Igneous.MOTOR_FORWARD, 0.1f);
        BooleanOutput solenoid =
                Igneous.makeSolenoid(0);
        EventOutput actuate =
                BooleanMixing.getSetEvent(solenoid, true);

        motor.set(1.0f);
        solenoid.set(false);
        actuate.event();
    }
}

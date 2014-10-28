package org.team1540.firstfare.presentation2014;

import ccre.ctrl.DriverImpls;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation20 implements IgneousApplication {

    public void setupRobot() {
        DriverImpls.createSynchTankDriver(Igneous.duringTele,
                Igneous.joystick1.getAxisChannel(2),
                Igneous.joystick1.getAxisChannel(3),
                Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f),
                Igneous.makeTalonMotor(1, Igneous.MOTOR_REVERSE, 0.1f));
    }
}

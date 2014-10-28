package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanStatus;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation15 implements IgneousApplication {

    public void setupRobot() {
        BooleanStatus status = new BooleanStatus(Igneous.makeSolenoid(0));
        status.set(true);
        status.setFalseWhen(Igneous.joystick1.getButtonSource(1));
        status.toggleWhen(Igneous.joystick1.getButtonSource(2));
        status.setTrueWhen(Igneous.joystick1.getButtonSource(3));
    }
}

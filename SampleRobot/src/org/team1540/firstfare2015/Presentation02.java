package org.team1540.firstfare2015;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation02 implements FRCApplication {

    public void setupRobot() {
        FloatOutput motor = FRC.talon(0, FRC.MOTOR_FORWARD, 0.1f);
        FloatInput axis = FRC.joystick1.axisY();
        axis.send(motor);
    }
}

package org.team1540.firstfare2015;

import ccre.ctrl.Drive;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation15 implements FRCApplication {

    public void setupRobot() {
        Drive.arcade(FRC.joystick1.axisX(), FRC.joystick1.axisY(), FRC.talon(0, FRC.MOTOR_FORWARD), FRC.talon(1, FRC.MOTOR_REVERSE));
    }
}

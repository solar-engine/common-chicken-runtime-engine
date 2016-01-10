package org.team1540.valkyrie;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.ctrl.Drive;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Robot implements FRCApplication {

    @Override
    public void setupRobot() throws Throwable {
        FloatOutput left = FRC.talon(4, FRC.MOTOR_FORWARD).combine(FRC.talon(5, FRC.MOTOR_REVERSE)).combine(FRC.talon(6, FRC.MOTOR_FORWARD));
        FloatOutput right = FRC.talon(1, FRC.MOTOR_REVERSE).combine(FRC.talon(2, FRC.MOTOR_REVERSE)).combine(FRC.talon(3, FRC.MOTOR_REVERSE));
        BooleanOutput solenoid = FRC.solenoid(1);
        Drive.tank(FRC.joystick1.axisY().negated(), FRC.joystick1.axis(6).negated(), left, right);
        new BooleanCell(solenoid).toggleWhen(FRC.joystick1.onPress(1));
    }
}

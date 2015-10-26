package org.team1540.firstfare2015;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatCell;
import ccre.ctrl.PIDController;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation16 implements FRCApplication {

    public void setupRobot() {
        FloatInput potentiometer = FRC.analogInput(0);
        FloatOutput motor = FRC.talon(0, FRC.MOTOR_FORWARD);

        FloatCell setpoint = new FloatCell();
        setpoint.setWhen(0.0f, FRC.joystick1.onPress(3));
        setpoint.setWhen(0.4f, FRC.joystick1.onPress(4));
        setpoint.setWhen(1.0f, FRC.joystick1.onPress(5));

        PIDController.createFixed(
                FRC.globalPeriodic, potentiometer, setpoint,
                0.7f, 0.01f, 0.0003f)
            .send(motor);
    }
}

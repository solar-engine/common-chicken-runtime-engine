package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation14 implements FRCApplication {

    public void setupRobot() {
        FloatOutput motor = FRC.talon(0, FRC.MOTOR_FORWARD);
        BooleanOutput solenoid = FRC.solenoid(0);

        FloatInput axis = FRC.joystick1.axisY();
        // You can chain transformations:
        axis.deadzone(0.2f).multipliedBy(7).send(motor);

        BooleanInput button = FRC.joystick1.button(1);
        BooleanInput other = FRC.joystick1.button(2);
        // You can chain transformations:
        solenoid.setTrueWhen(button.onPress().and(other).debounced(100));
    }
}

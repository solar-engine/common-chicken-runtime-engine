package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

@SuppressWarnings("unused")
public class Presentation04 implements FRCApplication {

    public void setupRobot() {
        FloatInput axis = FRC.joystick1.axisY();
        FloatOutput motor = FRC.talon(0, FRC.MOTOR_FORWARD);

        BooleanInput bumper = FRC.digitalInput(0);
        BooleanOutput solenoid = FRC.solenoid(3);

        EventInput button = FRC.joystick1.onPress(1);
        EventOutput actuate = solenoid.eventSetTrue();
    }
}

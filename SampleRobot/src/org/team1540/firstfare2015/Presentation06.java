package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation06 implements FRCApplication {

    public void setupRobot() {
        FloatOutput motor = FRC.talon(0, FRC.MOTOR_FORWARD);
        BooleanOutput solenoid = FRC.solenoid(3);
        EventOutput actuate = solenoid.eventSetTrue();
        
        // An input channel can be sent to an output.
        FloatInput axis = FRC.joystick1.axisY();
        BooleanInput bumper = FRC.digitalInput(0);
        EventInput button = FRC.joystick1.onPress(1);
        
        axis.send(motor);
        bumper.send(solenoid);
        button.send(actuate);
        
        // Or, you can read it directly, if it has a value.
        float float_value = axis.get();
        boolean boolean_value = bumper.get();
    }
}

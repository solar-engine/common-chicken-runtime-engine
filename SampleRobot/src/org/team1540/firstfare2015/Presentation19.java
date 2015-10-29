package org.team1540.firstfare2015;

import ccre.channel.BooleanCell;
import ccre.channel.EventCell;
import ccre.channel.FloatCell;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation19 implements FRCApplication {

    public void setupRobot() {
        // Cells are essentially like variables.
        BooleanCell state = new BooleanCell();
        state.send(FRC.solenoid(1));
        state.toggleWhen(FRC.joystick1.onPress(1));

        FloatCell setpoint = new FloatCell();
        // ... use setpoint in PID controller ...
        setpoint.setWhen(0, FRC.joystick1.onPress(1));
        setpoint.setWhen(0.4f, FRC.joystick1.onPress(2));
        setpoint.setWhen(1.0f, FRC.joystick1.onPress(3));

        EventCell link = new EventCell();
        state.setTrueWhen(link);
        FRC.joystick1.onPress(4).debounced(1000).send(link);
    }
}

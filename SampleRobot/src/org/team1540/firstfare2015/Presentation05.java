package org.team1540.firstfare2015;

import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation05 implements FRCApplication {

    public void setupRobot() {
        // An output channel can receive signals.
        FloatOutput motor = FRC.talon(0, FRC.MOTOR_FORWARD);
        BooleanOutput solenoid = FRC.solenoid(3);
        EventOutput actuate = solenoid.eventSetTrue();
        
        motor.set(+1.0f);
        solenoid.set(true);
        actuate.event();
    }
}

package org.team1540.firstfare2015;

import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

@SuppressWarnings("unused")
public class Presentation08 implements FRCApplication {

    public void setupRobot() {
        // with default ramping
        FloatOutput motor0 = FRC.talon(0, FRC.MOTOR_FORWARD);
        // with slow ramping
        FloatOutput motor1 = FRC.talon(1, FRC.MOTOR_FORWARD, 0.001f);
        // with fast ramping
        FloatOutput motor2 = FRC.talon(2, FRC.MOTOR_FORWARD, 0.5f);
        // with no ramping
        FloatOutput motor3 = FRC.talon(3, FRC.MOTOR_FORWARD, FRC.NO_RAMPING);
        // with manual ramping
        FloatOutput ramped3 = motor3.addRamping(0.1f, FRC.constantPeriodic);
    }
}

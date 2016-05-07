package org.team1540.firstfare2015;

import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

@SuppressWarnings("unused")
public class Presentation10 implements FRCApplication {

    public void setupRobot() throws ExtendedMotorFailureException {
        // With CAN:
        FloatOutput motorA = FRC.talonCANControl(0, FRC.MOTOR_FORWARD);
        // Without CAN:
        FloatOutput motorB = FRC.talon(0, FRC.MOTOR_FORWARD);

        // used exactly the same!

        // there is a more complex interface for power users of CAN.
    }
}

package org.team1540.firstfare2015;

import ccre.channel.BooleanOutput;
import ccre.ctrl.Drive;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation20 implements FRCApplication {

    public void setupRobot() {
        ControlBindingCreator bindings = FRC.controlBinding("Robot", true);

        BooleanOutput shifting = FRC.solenoid(0);
        shifting.setTrueWhen(bindings.addBoolean("Shift High Gear").onPress());
        shifting.setFalseWhen(bindings.addBoolean("Shift Low Gear").onPress());

        Drive.tank(bindings.addFloat("Left Drive Axis"), bindings.addFloat("Right Drive Axis"), FRC.talon(0, FRC.MOTOR_FORWARD), FRC.talon(1, FRC.MOTOR_REVERSE));
    }
}

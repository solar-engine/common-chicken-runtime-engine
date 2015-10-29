package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.timers.Ticker;

public class Presentation11 implements FRCApplication {

    public void setupRobot() {
        // main 10 millisecond loop
        EventInput constant = FRC.constantPeriodic;
        // when new joystick data arrives
        EventInput joystickControl = FRC.globalPeriodic;
        // every 300 milliseconds
        EventInput customConstant = new Ticker(300);

        BooleanInput input0 = FRC.digitalInput(0, constant);
        BooleanInput input1 = FRC.digitalInput(1, joystickControl);
        BooleanInput input2 = FRC.digitalInput(2, customConstant);
    }
}

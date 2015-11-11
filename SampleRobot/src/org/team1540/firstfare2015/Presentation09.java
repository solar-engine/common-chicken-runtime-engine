package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.timers.Ticker;

@SuppressWarnings("unused")
public class Presentation09 implements FRCApplication {

    public void setupRobot() {
        // with automatic polling
        BooleanInput polled = FRC.digitalInput(0);
        // with manual polling
        BooleanInput slow = FRC.digitalInput(1, new Ticker(1000));
        // with automatic interrupts
        BooleanInput instant = FRC.digitalInputByInterrupt(2);

        // all used the same.
        // compare THAT with WPILib!
    }
}

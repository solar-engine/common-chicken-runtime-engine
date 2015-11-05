package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation03 implements FRCApplication {

    public void setupRobot() {
        BooleanInput red_button = FRC.joystick1.button(1);
        BooleanInput key_turned = FRC.joystick1.button(2);
        EventOutput blow_up_world = WorldDestruction.eventBlowUp();
        // and then the actual work:
        red_button.onPress().and(key_turned).send(blow_up_world);
    }
}

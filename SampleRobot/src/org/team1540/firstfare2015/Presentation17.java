package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation17 implements FRCApplication {

    public void setupRobot() {
        BooleanInput red_button = FRC.joystick1.button(1);
        BooleanInput key_turned = FRC.joystick1.button(2);
        EventOutput blow_up_world = Cluck.subscribeEO("evilbot/blow-up");
        red_button.onPress().and(key_turned).send(blow_up_world);
    }

    // ...

    public void setupRobotAlt() {
        EventOutput blow_up_world = WorldDestruction.eventBlowUp();
        Cluck.publish("blow-up", blow_up_world);
    }
    
    // This actually works! Well, except for the explosive part.
}

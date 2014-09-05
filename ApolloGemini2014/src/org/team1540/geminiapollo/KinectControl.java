package org.team1540.geminiapollo;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;

public class KinectControl {

    public static BooleanInputPoll main(EventInput globalPeriodic, IJoystick disp1, IJoystick disp2) {
        Cluck.publish("stick-1", disp1.getAxisSource(2));
        Cluck.publish("stick-2", disp2.getAxisSource(2));
        BooleanInputPoll pressed = BooleanMixing.andBooleans(
                FloatMixing.floatIsAtMost(disp1.getAxisChannel(2), -0.1f),
                FloatMixing.floatIsAtMost(disp2.getAxisChannel(2), -0.1f));
        Cluck.publish("stick-pressed", BooleanMixing.createDispatch(pressed, globalPeriodic));
        return pressed;
    }
}

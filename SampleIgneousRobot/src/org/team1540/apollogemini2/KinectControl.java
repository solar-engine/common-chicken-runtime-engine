package org.team1540.apollogemini2;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;

public class KinectControl {

    public static BooleanInput main(EventInput globalPeriodic, IJoystick disp1, IJoystick disp2) {
        Cluck.publish("kinect-axis-1-2", disp1.getAxisSource(2));
        Cluck.publish("kinect-axis-2-2", disp2.getAxisSource(2));
        BooleanInput pressed = BooleanMixing.andBooleans(
                FloatMixing.floatIsAtMost(disp1.getAxisSource(2), -0.1f),
                FloatMixing.floatIsAtMost(disp2.getAxisSource(2), -0.1f));
        Cluck.publish("kinect-causing-activation", pressed);
        return pressed;
    }
}

package org.team1540.apollogemini2;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;

public class KinectControl {

    public static BooleanInput main(EventInput globalPeriodic, IJoystick left, IJoystick right) {
        Cluck.publish("Kinect Axis Left Arm", left.getAxisSource(2));
        Cluck.publish("Kinect Axis Right Arm", right.getAxisSource(2));
        BooleanInput pressed = BooleanMixing.andBooleans(
                FloatMixing.floatIsAtMost(left.getAxisSource(2), -0.1f),
                FloatMixing.floatIsAtMost(right.getAxisSource(2), -0.1f));
        Cluck.publish("Kinect Activation", pressed);
        return pressed;
    }
}

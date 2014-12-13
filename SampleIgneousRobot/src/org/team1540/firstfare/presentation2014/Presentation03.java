/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation03 implements IgneousApplication {

    public void setupRobot() {
        FloatInputPoll axis1 =
                Igneous.joystick1.getAxisChannel(1);
        FloatInput axis2 =
                Igneous.joystick1.getAxisSource(2);
        FloatOutput motor =
                Igneous.makeVictorMotor(1, Igneous.MOTOR_FORWARD, 0.1f);

        BooleanInputPoll bumper =
                Igneous.makeDigitalInput(0);
        BooleanInput bumperInp =
                BooleanMixing.createDispatch(bumper, Igneous.globalPeriodic);
        BooleanOutput solenoid =
                Igneous.makeSolenoid(0);

        EventInput button =
                Igneous.joystick1.getButtonSource(1);
        EventOutput actuate =
                BooleanMixing.getSetEvent(solenoid, true);

        Object[] o = new Object[] { axis1, axis2, motor, bumper, bumperInp, button, actuate };
        // To prevent warnings from showing up in the screenshot.
    }
}

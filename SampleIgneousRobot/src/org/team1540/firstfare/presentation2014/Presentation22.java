/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation22 implements IgneousApplication {

    public void setupRobot() {
        // Can't publish InputPolls
        FloatInput pressure = FloatMixing.createDispatch(
                Igneous.makeAnalogInput(1, 8), Igneous.globalPeriodic);
        Cluck.publish("pressure-raw", pressure);
        Cluck.publish("spinner",
                Igneous.makeTalonMotor(1, Igneous.MOTOR_FORWARD, 0.1f));
        EventStatus loop = new EventStatus();
        Cluck.publish("button", (EventOutput) loop);
        Cluck.publish("alert", (EventInput) loop);
        BooleanInput bumper = BooleanMixing.createDispatch(
                Igneous.makeDigitalInput(1), Igneous.globalPeriodic);
        Cluck.publish("bumper", bumper);
        Cluck.publish("light", Igneous.makeDigitalOutput(1));
    }
}

package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.igneous.IgneousApplication;

public class Presentation23 implements IgneousApplication {

    public void setupRobot() {
        Cluck.setupClient("10.15.40.2", "robot", "remote-example");
        FloatInput pressure = Cluck.subscribeFI("robot/pressure-raw", false);
        FloatOutput spinner = Cluck.subscribeFO("robot/spinner");
        EventInput alert = Cluck.subscribeEI("robot/alert");
        EventOutput button = Cluck.subscribeEO("robot/button");
        BooleanInput bumper = Cluck.subscribeBI("robot/bumper", false);
        BooleanOutput light = Cluck.subscribeBO("robot/light");
        // And do something with all of that.

        // Just to prevent warnings in the screenshot:
        Object[] o = new Object[] { pressure, spinner, alert, button, bumper, light };
    }
}

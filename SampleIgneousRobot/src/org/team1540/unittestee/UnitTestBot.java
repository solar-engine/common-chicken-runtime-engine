package org.team1540.unittestee;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.igneous.IgneousApplication;
import ccre.log.LogLevel;

/**
 * A deployable robot used for Cluck autotesting. Works with the associated PoultryInspector component.
 * 
 * @author skeggsc
 */
public class UnitTestBot implements IgneousApplication {

    public void setupRobot() {
        String targetRoot = "phidget/";
        // Note: currently incomplete.
        BooleanStatus b = new BooleanStatus();
        FloatStatus f = new FloatStatus();
        EventStatus e = new EventStatus();
        Cluck.publish("utest-os0", Cluck.subscribeOS(targetRoot + "utest-os1"));
        Cluck.publish("utest-lt0", Cluck.subscribeLT(targetRoot + "utest-lt1", LogLevel.FINEST));
        Cluck.publish("utest-fo0", (FloatOutput) f);
        Cluck.publish("utest-fi0", (FloatInput) f);
        Cluck.publish("utest-bo0", (BooleanOutput) b);
        Cluck.publish("utest-bi0", (BooleanInput) b);
        Cluck.publish("utest-eo0", (EventOutput) e);
        Cluck.publish("utest-ei0", (EventInput) e);
    }
}

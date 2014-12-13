/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation09 implements IgneousApplication {

    public void setupRobot() {
        EventInput everyTenMilliseconds = Igneous.constantPeriodic;
        EventInput duringAlways = Igneous.globalPeriodic;
        EventInput duringAutonomousMode = Igneous.duringAuto;
        EventInput duringTeleoperatedMode = Igneous.duringTele;
        EventInput duringTestingMode = Igneous.duringTest;
        EventInput whileRobotIsDisabled = Igneous.duringDisabled;
        EventInput startOfAutonomousMode = Igneous.startAuto;
        EventInput startOfTeleoperatedMode = Igneous.startTele;
        EventInput startOfTestingMode = Igneous.startTest;
        EventInput whenRobotIsDisabled = Igneous.startDisabled;

        Igneous.duringTele.send(new EventOutput() {
            public void event() {
                do_something_often_only_in_teleoperated_mode();
            }
        });

        EventInput[] out = new EventInput[] {
                everyTenMilliseconds, duringAlways, duringAutonomousMode,
                duringTeleoperatedMode, duringTestingMode, whileRobotIsDisabled,
                startOfAutonomousMode, startOfTeleoperatedMode, startOfTestingMode,
                whenRobotIsDisabled
        };
        // To prevent unnecessary warnings in the screenshot.
    }

    protected void do_something_often_only_in_teleoperated_mode() {

    }
}

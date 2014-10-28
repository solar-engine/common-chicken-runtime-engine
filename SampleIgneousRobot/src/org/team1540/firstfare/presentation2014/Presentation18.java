package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventLogger;
import ccre.channel.EventOutput;
import ccre.ctrl.ExpirationTimer;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.log.LogLevel;

public class Presentation18 implements IgneousApplication {

    public void setupRobot() {
        BooleanOutput yellowFieldLights1 = null, yellowFieldLights2 = null, matchRunning = null;

        final ExpirationTimer timer = new ExpirationTimer();
        timer.schedule(0000, new EventLogger(LogLevel.INFO, "FIVE!"));
        timer.schedule(1000, new EventLogger(LogLevel.INFO, "FOUR!"));
        timer.schedule(2000, new EventLogger(LogLevel.INFO, "THREE!"));
        timer.schedule(3000, new EventLogger(LogLevel.INFO, "TWO!"));
        timer.schedule(4000, new EventLogger(LogLevel.INFO, "ONE!"));
        EventInput matchStart = timer.schedule(5000);
        timer.scheduleEnable(5000, yellowFieldLights1);
        timer.scheduleDisable(10000, yellowFieldLights1);
        timer.scheduleBooleanPeriod(10000, 15000, yellowFieldLights2, true);
        timer.scheduleSet(15000, matchRunning, true);
        timer.scheduleSet(155000, matchRunning, false); // 2:30 later

        Igneous.joystick1.getButtonSource(1).send(new EventOutput() {
            public void event() {
                if (timer.isRunning()) {
                    timer.stop();
                } else {
                    timer.start();
                }
            }
        });
        Igneous.joystick1.getButtonSource(2).send(new EventOutput() {
            public void event() {
                timer.startOrFeed();
                // timer.feed();
            }
        });
        timer.startOrFeedWhen(Igneous.joystick1.getButtonSource(1));
        // same for startWhen, stopWhen, feedWhen
        BooleanStatus sequenceRunning = new BooleanStatus();
        sequenceRunning.send(timer.getRunningControl());
    }
}

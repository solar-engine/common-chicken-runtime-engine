package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanInput;
import ccre.channel.EventLogger;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.PauseTimer;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.log.LogLevel;

public class Presentation19 implements IgneousApplication {

    public void setupRobot() {
        PauseTimer timer = new PauseTimer(1500);
        Igneous.joystick1.getButtonSource(1).send(timer);
        BooleanInput asInput = timer;
        // we could use timer instead of asInput here:
        EventLogger.log(
                BooleanMixing.whenBooleanBecomes(asInput, true),
                LogLevel.FINE, "Timer started (1)");
        EventLogger.log(
                BooleanMixing.whenBooleanBecomes(asInput, false),
                LogLevel.FINE, "Timer ended (1)");
        timer.triggerAtStart(
                new EventLogger(LogLevel.FINE, "Timer started (2)"));
        timer.triggerAtEnd(
                new EventLogger(LogLevel.FINE, "Timer ended (2)"));
        timer.triggerAtChanges(
                new EventLogger(LogLevel.FINE, "Timer started (3)"),
                new EventLogger(LogLevel.FINE, "Timer ended (3)"));
    }
}

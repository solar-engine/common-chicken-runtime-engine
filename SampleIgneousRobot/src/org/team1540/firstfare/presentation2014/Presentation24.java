/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.holders.TuningContext;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation24 implements IgneousApplication {

    public void setupRobot() {
        TuningContext context =
                new TuningContext(Cluck.getNode(), "everything");
        context.publishSavingEvent("Everything");

        FloatStatus speed = context.getFloat("collector-speed", 0.7f);

        FloatOutput collector =
                Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f);
        BooleanOutput collectorOnOff =
                Mixing.select(collector, FloatMixing.always(0), speed);

        BooleanMixing.pumpWhen(Igneous.duringTele,
                Igneous.joystick1.getButtonChannel(1), collectorOnOff);
    }
}

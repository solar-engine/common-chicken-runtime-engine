/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.ctrl.BooleanMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation05 implements IgneousApplication {

    public void setupRobot() {
        BooleanOutput solenoid = Igneous.makeSolenoid(1);

        EventInput input =
                Igneous.joystick1.getButtonSource(1);
        final EventOutput actuate =
                BooleanMixing.getSetEvent(solenoid, true);

        input.send(actuate);

        input.send(new EventOutput() {
            public void event() {
                actuate.event();
            }
        });
    }
}

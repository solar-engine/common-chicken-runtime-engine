/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanStatus;
import ccre.channel.EventOutput;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation14 implements IgneousApplication {

    public void setupRobot() {
        final BooleanStatus status = new BooleanStatus();
        status.send(Igneous.makeSolenoid(0));
        status.set(true);
        Igneous.joystick1.getButtonSource(1).send(new EventOutput() {
            public void event() {
                status.set(false);
            }
        });
        Igneous.joystick1.getButtonSource(2).send(new EventOutput() {
            public void event() {
                status.set(!status.get());
            }
        });
        Igneous.joystick1.getButtonSource(3).send(new EventOutput() {
            public void event() {
                status.set(true);
            }
        });
    }
}

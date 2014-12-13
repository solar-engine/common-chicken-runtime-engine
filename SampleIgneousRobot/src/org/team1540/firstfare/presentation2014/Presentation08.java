/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanOutput;
import ccre.ctrl.BooleanMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation08 implements IgneousApplication {

    public void setupRobot() {
        BooleanOutput solenoid = Igneous.makeSolenoid(0);

        solenoid.set(false);

        BooleanOutput double1 = Igneous.makeSolenoid(1);
        BooleanOutput double2 = Igneous.makeSolenoid(2);

        BooleanOutput doubleWhole = BooleanMixing.combine(
                double1,
                BooleanMixing.invert(double2));

        doubleWhole.set(true);

        // On cRIO
        Igneous.useCompressor(1, 1); // pressure switch, relay

        // On roboRIO, don't have to control compressor!
    }
}

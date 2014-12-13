/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.ctrl.MultipleSourceBooleanController;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.log.Logger;

public class Presentation21 implements IgneousApplication {

    public void setupRobot() {
        MultipleSourceBooleanController orGate =
                new MultipleSourceBooleanController(
                        MultipleSourceBooleanController.OR);
        Igneous.globalPeriodic.send(orGate);

        BooleanInputPoll bip = Igneous.getIsTeleop();
        orGate.addInput(bip);
        BooleanOutput out = orGate.getOutput(false);

        orGate.send(new BooleanOutput() {
            public void set(boolean b) {
                Logger.fine("Status: " + b);
            }
        });

        // ...
        out.set(true);
        // ...
    }
}

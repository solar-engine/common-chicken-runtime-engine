/**
 * This file is free and unencumbered software released into the public domain.
 * See the README.txt file.
 */
package org.team1540.firstfare.presentation2014;

import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

public class Presentation10 implements IgneousApplication {

    public void setupRobot() {
        Igneous.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain()
                    throws AutonomousModeOverException, InterruptedException {
                // Do some events in sequence!
            }
        });
    }
}

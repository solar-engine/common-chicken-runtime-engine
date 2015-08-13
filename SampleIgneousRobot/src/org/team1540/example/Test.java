/*
 * Copyright 2013-2014 Colby Skeggs
 *
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 *
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.example;

import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.DriverImpls;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousCore;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

/**
 * A slightly-more-complex Test program. This handles driving, shifting, the
 * compressor, and autonomous.
 *
 * @author skeggsc
 */
public class Test extends IgneousCore {

    /**
     * Set up the test robot. This includes tank drive, high gear/low gear, a
     * compressor, and a simple autonomous.
     */
    public void setupRobot() {
        // Driving
        FloatInput leftAxis = joystick1.axis(2);
        FloatInput rightAxis = joystick1.axis(5);
        final FloatOutput leftOut = makeTalonMotor(2, MOTOR_FORWARD, 0.1f);
        final FloatOutput rightOut = makeTalonMotor(1, MOTOR_REVERSE, 0.1f);
        DriverImpls.createSynchTankDriver(duringTele, leftAxis, rightAxis, leftOut, rightOut);
        // Shifting
        BooleanStatus shifter = new BooleanStatus(makeSolenoid(2));
        shifter.setFalseWhen(startTele);
        shifter.setTrueWhen(joystick1.onPress(3));
        shifter.setFalseWhen(joystick1.onPress(1));
        // Compressor
        useCompressor(1, 1);
        // Autonomous
        Igneous.registerAutonomous(new InstinctModule() {
            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                leftOut.set(-1);
                rightOut.set(-1);
                waitForTime(5000);
                leftOut.set(0);
                rightOut.set(0);
            }
        });
    }
}

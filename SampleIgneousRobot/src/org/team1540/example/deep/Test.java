/*
 * Copyright 2013-2015 Colby Skeggs
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
package org.team1540.example.deep;

import ccre.channel.BooleanStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.DriverImpls;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.igneous.IgneousCore;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

/**
 * This is like org.team1540.example.Test, but it has it dressed up with many of
 * the CCRE's features.
 *
 * @author skeggsc
 */
public class Test implements IgneousApplication {

    /**
     * Set up the test robot. This includes tank drive, high gear/low gear, a
     * compressor, and a simple autonomous.
     */
    public void setupRobot() {
        Cluck.publishRConf("drive-code", new DriveCode());
        // Shifting
        BooleanStatus shifter = new BooleanStatus(Igneous.makeSolenoid(2));
        Cluck.publishRConf("shifter", shifter);
        shifter.setFalseWhen(Igneous.startTele);
        shifter.setTrueWhen(Igneous.joystick1.getButtonSource(3));
        shifter.setFalseWhen(Igneous.joystick1.getButtonSource(1));
        // Compressor
        Igneous.useCompressor(1, 1);
        // Autonomous
        /*Igneous.registerAutonomous(new InstinctModule() {
            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                leftOut.set(-1);
                rightOut.set(-1);
                waitForTime(5000);
                leftOut.set(0);
                rightOut.set(0);
            }
        });*/
    }
}

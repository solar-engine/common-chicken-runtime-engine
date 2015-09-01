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
import ccre.cluck.Cluck;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

/**
 * This is like org.team1540.example.Test, but it has it dressed up with many of
 * the CCRE's features.
 *
 * Not yet complete!
 *
 * @author skeggsc
 */
public class Test implements FRCApplication {

    public static final ControlBindingCreator driveControls = FRC.makeControlBindingCreator("Drive Code");

    /**
     * Set up the test robot. This includes tank drive, high gear/low gear, a
     * compressor, and a simple autonomous.
     */
    public void setupRobot() {
        Cluck.publishRConf("drive-code", new DriveCode());
        // Shifting
        BooleanStatus shifter = new BooleanStatus(FRC.makeSolenoid(3));
        shifter.setFalseWhen(FRC.startTele);
        shifter.setTrueWhen(driveControls.addBoolean("High Gear").onPress());
        shifter.setFalseWhen(driveControls.addBoolean("Low Gear").onPress());
        // Compressor
        FRC.useCompressor(1, 1);
        // Autonomous
        /* FRC.registerAutonomous(new InstinctModule() {
            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                leftOut.set(-1);
                rightOut.set(-1);
                waitForTime(5000);
                leftOut.set(0);
                rightOut.set(0);
            }
        }); */
    }
}

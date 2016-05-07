/*
 * Copyright 2013-2016 Cel Skeggs
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

import ccre.channel.*;
import ccre.ctrl.*;
import ccre.frc.*;
import ccre.instinct.*;

/**
 * A slightly-more-complex Test program. This handles driving, shifting, the
 * compressor, and autonomous.
 */
public class Test implements FRCApplication {
    public void setupRobot() {
        FloatInput leftAxis = FRC.joystick1.axis(2);
        FloatInput rightAxis = FRC.joystick1.axis(5);
        FloatOutput leftOut = FRC.talon(2, FRC.MOTOR_FORWARD);
        FloatOutput rightOut = FRC.talon(1, FRC.MOTOR_REVERSE);

        Drive.tank(leftAxis, rightAxis, leftOut, rightOut);

        BooleanOutput shifter = FRC.solenoid(2);
        shifter.setFalseWhen(FRC.startTele);
        shifter.setTrueWhen(FRC.joystick1.onPress(3));
        shifter.setFalseWhen(FRC.joystick1.onPress(1));

        FRC.registerAutonomous(new InstinctModule() {
            @Override
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

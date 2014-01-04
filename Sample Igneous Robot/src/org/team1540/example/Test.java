/*
 * Copyright 2013 Colby Skeggs
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

import ccre.chan.*;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.Mixing;
import ccre.igneous.SimpleCore;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

public class Test extends SimpleCore {

    protected void createSimpleControl() {
        // Driving
        FloatInputPoll leftAxis = joystick1.getAxisChannel(2);
        FloatInputPoll rightAxis = joystick1.getAxisChannel(5);
        final FloatOutput leftOut = makeTalonMotor(2, MOTOR_FORWARD, 0.1f);
        final FloatOutput rightOut = makeTalonMotor(1, MOTOR_REVERSE, 0.1f);
        DriverImpls.createSynchTankDriver(duringTeleop, leftAxis, rightAxis, leftOut, rightOut);
        // Shifting
        //BooleanOutput o = Mixing.select(makeServo(1, 0, 180), 45, 135);
        BooleanStatus shifter = new BooleanStatus(makeSolenoid(2));
        shifter.setFalseWhen(startedTeleop);
        shifter.setTrueWhen(joystick1.getButtonSource(3));
        shifter.setFalseWhen(joystick1.getButtonSource(1));
        // Compressor
        useCompressor(1, 1);
        // Autonomous
        new InstinctModule() {
            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                leftOut.writeValue(-1);
                rightOut.writeValue(-1);
                waitForTime(5000);
                leftOut.writeValue(0);
                rightOut.writeValue(0);
            }
        }.register(this);
    }
}

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
import ccre.ctrl.MultipleSourceBooleanController;
import ccre.event.EventLogger;
import ccre.event.EventSource;
import ccre.igneous.SimpleCore;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;

public class Test extends SimpleCore {

    protected void createSimpleControl() {
        // Driving
        FloatInputPoll leftAxis = joystick1.getAxisChannel(2);
        FloatInputPoll forwardAxis = joystick1.getAxisChannel(3);
        FloatInputPoll rightAxis = joystick1.getAxisChannel(5);
        this.makeDSFloatReadout("Left", 1, leftAxis, duringTeleop);
        this.makeDSFloatReadout("Right", 2, rightAxis, duringTeleop);
        this.makeDSFloatReadout("Forward", 3, forwardAxis, duringTeleop);
        final FloatOutput leftOut = makeTalonMotor(2, MOTOR_FORWARD);
        final FloatOutput rightOut = makeTalonMotor(1, MOTOR_REVERSE);
        DriverImpls.createExtendedSynchTankDriver(duringTeleop, leftAxis, rightAxis, forwardAxis, leftOut, rightOut);
        // Shifting
        EventSource shiftHighBtn = joystick1.getButtonSource(1);
        EventSource shiftLowBtn = joystick1.getButtonSource(3);
        BooleanStatus shifter = new BooleanStatus(makeSolenoid(2));
        shifter.setFalseWhen(startedTeleop);
        shifter.setTrueWhen(shiftLowBtn);
        shifter.setFalseWhen(shiftHighBtn);
        // Compressor
        useCompressor(1, 1);
        new InstinctModule() {
            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                leftOut.writeValue(1.0f);
                rightOut.writeValue(1.0f);
                waitForTime(500);
                leftOut.writeValue(0.5f);
                rightOut.writeValue(0.5f);
                waitForTime(1000);
                leftOut.writeValue(0);
                rightOut.writeValue(0);
            }
        }.register(this);
    }
}

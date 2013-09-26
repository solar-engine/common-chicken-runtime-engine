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
import ccre.event.EventSource;
import ccre.igneous.SimpleCore;

public class Test extends SimpleCore {
    protected void createSimpleControl() {
        // Driving
        FloatInputPoll leftAxis = joystick1.getAxisChannel(2);
        FloatInputPoll forwardAxis = joystick1.getAxisChannel(3);
        FloatInputPoll rightAxis = joystick1.getAxisChannel(5);
        this.makeDSFloatReadout("Left", 1, leftAxis, duringTeleop);
        this.makeDSFloatReadout("Right", 2, rightAxis, duringTeleop);
        this.makeDSFloatReadout("Forward", 3, forwardAxis, duringTeleop);
        FloatOutput leftOut = makeTalonMotor(2, MOTOR_FORWARD);
        FloatOutput rightOut = makeTalonMotor(1, MOTOR_REVERSE);
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
    }
}

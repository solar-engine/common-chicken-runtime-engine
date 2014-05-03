/*
 * Copyright 2013-2014 Colby Skeggs and Vincent Miller
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
import ccre.ctrl.Mixing;
import ccre.obsidian.ObsidianCore;
import ccre.obsidian.PWMPin;

public class Example extends ObsidianCore {

    @Override
    protected void createRobotControl() {
        final FloatInput xAxis = launcher.getJoystickAxis((byte) 1);
        final FloatInput yAxis = Mixing.negate.wrap(launcher.getJoystickAxis((byte) 2));
        
        final FloatOutput leftMotor = makePWMOutput(PWMPin.P8_13, 0, 0.333f, 0.666f, 333f, true);
        final FloatOutput rightMotor = makePWMOutput(PWMPin.P9_14, 0, 0.333f, 0.666f, 333f, true);
        
        periodic.send(new EventOutput() {
            @Override
            public void event() {
                //Logger.finest("Vals: " + xAxis.readValue() + ", " + yAxis.readValue());
                leftMotor.set(yAxis.get() + xAxis.get());
                rightMotor.set(yAxis.get() - xAxis.get());
                //rightMotor.writeValue(1.0f);                //leftMotor.writeValue(1.0f);
            }
        });
    }
}

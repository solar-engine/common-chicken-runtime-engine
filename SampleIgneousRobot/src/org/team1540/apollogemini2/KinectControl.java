/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 * 
 * This file is part of the Revised ApolloGemini2014 project.
 * 
 * ApolloGemini2014 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * ApolloGemini2014 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ApolloGemini2014.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.apollogemini2;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;

public class KinectControl {

    public static BooleanInput main(EventInput globalPeriodic, IJoystick left, IJoystick right) {
        Cluck.publish("Kinect Axis Left Arm", left.getAxisSource(2));
        Cluck.publish("Kinect Axis Right Arm", right.getAxisSource(2));
        BooleanInput pressed = BooleanMixing.andBooleans(
                FloatMixing.floatIsAtMost(left.getAxisSource(2), -0.1f),
                FloatMixing.floatIsAtMost(right.getAxisSource(2), -0.1f));
        Cluck.publish("Kinect Activation", pressed);
        return pressed;
    }
}

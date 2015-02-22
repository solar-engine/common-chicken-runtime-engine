/*
 * Copyright 2015 Colby Skeggs
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
package org.team1540.joysticks;

import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.IJoystickWithPOV;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

/**
 * An example program that just publishes all of the Joystick data.
 *
 * @author skeggsc
 */
public class Joysticks implements IgneousApplication {

    /**
     * The number of POVs to publish information on.
     */
    public static int POV_NUMBER = 1;

    /**
     * Set up the robot.
     */
    public void setupRobot() {
        publishJoystick(1, Igneous.joystick1);
        publishJoystick(2, Igneous.joystick2);
        publishJoystick(3, Igneous.joystick3);
        publishJoystick(4, Igneous.joystick4);
    }

    private void publishJoystick(int id, IJoystickWithPOV joy) {
        for (int axis = 1; axis <= 6; axis++) {
            Cluck.publish("Joystick " + id + " Axis " + axis, joy.getAxisSource(axis));
        }
        for (int button = 1; button <= 12; button++) {
            Cluck.publish("Joystick " + id + " Button " + button, BooleanMixing.createDispatch(joy.getButtonChannel(button), Igneous.globalPeriodic));
        }
        if (Igneous.isRoboRIO() && POV_NUMBER > 0) {
            for (int pov = 1; pov <= POV_NUMBER; pov++) {
                Cluck.publish("Joystick " + id + " POV Angle ", joy.getPOVAngleSource(pov));
                Cluck.publish("Joystick " + id + " POV Pressed ", joy.isPOVPressedSource(pov));
            }
        }
    }
}

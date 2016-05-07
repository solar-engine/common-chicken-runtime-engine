/*
 * Copyright 2015 Cel Skeggs
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
import ccre.ctrl.Joystick;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.verifier.SetupPhase;

/**
 * An example program that just publishes all of the Joystick data.
 *
 * @author skeggsc
 */
public class Joysticks implements FRCApplication {

    /**
     * Set up the robot.
     */
    @Override
    public void setupRobot() {
        publishJoystick(1, FRC.joystick1);
        publishJoystick(2, FRC.joystick2);
        publishJoystick(3, FRC.joystick3);
        publishJoystick(4, FRC.joystick4);
    }

    @SetupPhase
    private void publishJoystick(int id, Joystick joy) {
        for (int axis = 1; axis <= 6; axis++) {
            Cluck.publish("Joystick " + id + " Axis " + axis, joy.axis(axis));
        }
        for (int button = 1; button <= 12; button++) {
            Cluck.publish("Joystick " + id + " Button " + button, joy.button(button));
        }
        for (int direction : Joystick.POV_DIRECTIONS) {
            Cluck.publish("Joystick " + id + " POV Pressed (" + direction + ")", joy.isPOV(direction));
        }
    }
}

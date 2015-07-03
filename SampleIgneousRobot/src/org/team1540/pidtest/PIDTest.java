/*
 * Copyright 2013-2014 Colby Skeggs
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
package org.team1540.pidtest;

import ccre.ctrl.PIDController;
import ccre.holders.TuningContext;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

/**
 * A test for PID controllers.
 *
 * @author skeggsc
 */
public class PIDTest implements IgneousApplication {

    /**
     * Set up the robot. For the minimal robot, this only means printing a
     * message.
     */
    public void setupRobot() {
        TuningContext context = new TuningContext("pid_control").publishSavingEvent();
        PIDController controller = new PIDController(Igneous.joystick1.getYChannel(),
                context.getFloat("P", 1), context.getFloat("I", 0), context.getFloat("D", 0));
        controller.send(Igneous.makeTalonMotor(1, false, 0));
        controller.updateWhen(Igneous.duringTele);
    }
}

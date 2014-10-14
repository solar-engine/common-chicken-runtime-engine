/*
 * Copyright 2014 Colby Skeggs
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
package org.team1540.tester;

import ccre.cluck.Cluck;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

/**
 * An example program that simply shares all the motors over the network.
 * 
 * Surprisingly useful for an example! Make sure to change which types the
 * motors are.
 *
 * @author skeggsc
 */
public class Tester implements IgneousApplication {

    /**
     * Set up the robot. For the testing robot, this means publishing all the
     * motors.
     */
    public void setupRobot() {
        int base = Igneous.isRoboRIO() ? 0 : 1;
        for (int i = base; i < (Igneous.isRoboRIO() ? 20 : 10) + base; i++) {
            Cluck.publish("talon-" + i, Igneous.makeTalonMotor(i, false, 0.1f));
        }
        for (int i = base; i < 8 + base; i++) {
            Cluck.publish("solenoid-" + i, Igneous.makeSolenoid(i));
        }
        for (int i = base; i < 4 + base; i++) {
            Cluck.publish("analog-" + i, FloatMixing.createDispatch(Igneous.makeAnalogInput(i, 8), Igneous.globalPeriodic));
        }
        if (Igneous.isRoboRIO()) {
            for (int i = base; i < 16 + base; i++) {
                Cluck.publish("current-" + i, FloatMixing.createDispatch(Igneous.getPDPChannelCurrent(i), Igneous.globalPeriodic));
            }
            Cluck.publish("compressor", Igneous.usePCMCompressor());
            Cluck.publish("pdp-voltage", FloatMixing.createDispatch(Igneous.getPDPVoltage(), Igneous.globalPeriodic));
        }
    }
}

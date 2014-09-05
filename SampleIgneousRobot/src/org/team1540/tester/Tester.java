package org.team1540.tester;

import ccre.cluck.Cluck;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

/**
 * An example program that simply shares all the motors over the network.
 * 
 * Surprisingly useful for an example! Make sure to change which types the motors are.
 *
 * @author skeggsc
 */
public class Tester implements IgneousApplication {

    /**
     * Set up the robot. For the testing robot, this means publishing all the motors.
     */
    public void setupRobot() {
        for (int i=0; i<20; i++) {
            Cluck.publish("talon-" + i, Igneous.makeTalonMotor(i, false, 0.1f));
        }
    }
}

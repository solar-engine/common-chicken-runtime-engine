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
        for (int i = base; i < 20 + base; i++) {
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

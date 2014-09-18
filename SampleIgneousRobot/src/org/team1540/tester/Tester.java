package org.team1540.tester;

import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;
import ccre.log.Logger;
import edu.wpi.first.wpilibj.PowerDistributionPanel;

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
        for (int i=0; i<8; i++) {
            Cluck.publish("solenoid-" + i, Igneous.makeSolenoid(i));
        }
        for (int i=0; i<4; i++) {
            Cluck.publish("analog-" + i, FloatMixing.createDispatch(Igneous.makeAnalogInput(i, 8), Igneous.globalPeriodic));
        }
        for (int i=0; i<16; i++) {
            Cluck.publish("current-" + i, FloatMixing.createDispatch(Igneous.getPDPChannelCurrent(i), Igneous.globalPeriodic));
        }
        Cluck.publish("compressor", Igneous.usePCMCompressor());
        Cluck.publish("pdp-voltage", FloatMixing.createDispatch(Igneous.getPDPVoltage(), Igneous.globalPeriodic));
    }
}

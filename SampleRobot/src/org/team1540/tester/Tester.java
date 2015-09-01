/*
 * Copyright 2014-2015 Colby Skeggs
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

import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

/**
 * An example program that simply shares all the motors over the network.
 *
 * Surprisingly useful for an example! Make sure to change which types the
 * motors are.
 *
 * @author skeggsc
 */
public class Tester implements FRCApplication {

    /**
     * Set up the robot. For the testing robot, this means publishing all the
     * motors.
     */
    public void setupRobot() {
        int base = FRC.isRoboRIO() ? 0 : 1;
        final FloatOutput[] outs = new FloatOutput[FRC.isRoboRIO() ? 20 : 10];
        for (int i = base; i < (FRC.isRoboRIO() ? 20 : 10) + base; i++) {
            Cluck.publish("talon-" + i, outs[i - base] = FRC.makeTalonMotor(i, false, 0.1f));
        }
        Cluck.publish("talon-all", new FloatOutput() {
            public void set(float value) {
                for (FloatOutput out : outs) {
                    out.set(value);
                }
            }
        });
        for (int i = base; i < (FRC.isRoboRIO() ? 4 : 8) + base; i++) {
            Cluck.publish("relay-" + i + "-fwd", FRC.makeForwardRelay(i));
            Cluck.publish("relay-" + i + "-rev", FRC.makeReverseRelay(i));
        }
        for (int i = base; i < 8 + base; i++) {
            Cluck.publish("solenoid-" + i, FRC.makeSolenoid(i));
        }
        for (int i = base; i < 4 + base; i++) {
            Cluck.publish("analog-" + i, FRC.makeAnalogInput(i, 8));
        }
        Cluck.publish("input-voltage", FRC.getChannelVoltage(FRC.POWER_CHANNEL_BATTERY));
        if (FRC.isRoboRIO()) {
            Cluck.publish("input-current", FRC.getChannelCurrent(FRC.POWER_CHANNEL_BATTERY));
            Cluck.publish("6v-voltage", FRC.getChannelVoltage(FRC.POWER_CHANNEL_6V));
            Cluck.publish("6v-current", FRC.getChannelCurrent(FRC.POWER_CHANNEL_6V));
            Cluck.publish("5v-voltage", FRC.getChannelVoltage(FRC.POWER_CHANNEL_5V));
            Cluck.publish("5v-current", FRC.getChannelCurrent(FRC.POWER_CHANNEL_5V));
            Cluck.publish("3.3v-voltage", FRC.getChannelVoltage(FRC.POWER_CHANNEL_3V3));
            Cluck.publish("3.3v-current", FRC.getChannelCurrent(FRC.POWER_CHANNEL_3V3));
            for (int i = base; i < 16 + base; i++) {
                Cluck.publish("current-" + i, FRC.getPDPChannelCurrent(i));
            }
            Cluck.publish("compressor", FRC.usePCMCompressor());
            Cluck.publish("pdp-voltage", FRC.getPDPVoltage());
        }
    }
}

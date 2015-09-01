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
package org.team1540.example.deep;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.ctrl.DriverImpls;
import ccre.frc.FRC;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;

public class DriveCode implements RConfable {

    private final FloatInput leftAxis = Test.driveControls.addFloat("Drive Axis Left").deadzone(0.1f);
    private final FloatInput rightAxis = Test.driveControls.addFloat("Drive Axis Right").deadzone(0.1f);
    private final FloatOutput leftOut = FRC.makeTalonMotor(1, FRC.MOTOR_REVERSE, 0.1f);
    private final FloatOutput rightOut = FRC.makeTalonMotor(2, FRC.MOTOR_FORWARD, 0.1f);
    private final BooleanStatus allowToRun = new BooleanStatus(true),
            forceEnabled = new BooleanStatus();

    private final BooleanInput shouldBeRunning = allowToRun.and(FRC.getIsTeleop().or(forceEnabled));

    public DriveCode() {
        DriverImpls.tankDrive(leftAxis, rightAxis, leftOut.filter(shouldBeRunning), rightOut.filter(shouldBeRunning));
    }

    public Entry[] queryRConf() throws InterruptedException {
        return new Entry[] { RConf.title("Drive Code"), RConf.string("Axes:"), RConf.fieldFloat(leftAxis.get()), RConf.fieldFloat(rightAxis.get()), RConf.string("Allow To Run:"), RConf.fieldBoolean(allowToRun.get()), RConf.string("Force To Run:"), RConf.fieldBoolean(forceEnabled.get()) };
    }

    public boolean signalRConf(int field, byte[] data) throws InterruptedException {

        switch (field) {
        case 2:
            if (data.length >= 4) {
                leftOut.set(RConf.bytesToFloat(data));
            }
            return true;
        case 3:
            if (data.length >= 4) {
                rightOut.set(RConf.bytesToFloat(data));
            }
            return true;
        case 5:
            allowToRun.set(data.length > 0 && data[0] != 0);
            return true;
        case 7:
            forceEnabled.set(data.length > 0 && data[0] != 0);
            return true;
        default:
            return false;
        }
    }
}

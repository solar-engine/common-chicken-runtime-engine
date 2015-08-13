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

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.EventMixing;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;

public class DriveCode implements RConfable {

    private final FloatInputPoll forwardAxis = FloatMixing.deadzone(Test.driveControls.addFloat("Drive Axis Forward"), 0.1f);
    private final FloatInputPoll strafeAxis = FloatMixing.deadzone(Test.driveControls.addFloat("Drive Axis Strafe"), 0.1f);
    private final FloatInputPoll rotateAxis = FloatMixing.deadzone(Test.driveControls.addFloat("Drive Axis Rotate"), 0.1f);
    private final FloatOutput leftFront = Igneous.makeTalonMotor(9, Igneous.MOTOR_REVERSE, 0.1f);
    private final FloatOutput rightFront = Igneous.makeTalonMotor(10, Igneous.MOTOR_FORWARD, 0.1f);
    private final FloatOutput leftBack = Igneous.makeTalonMotor(8, Igneous.MOTOR_REVERSE, 0.1f);
    private final FloatOutput rightBack = Igneous.makeTalonMotor(1, Igneous.MOTOR_FORWARD, 0.1f);
    private final BooleanStatus allowToRun = new BooleanStatus(true),
            forceEnabled = new BooleanStatus();

    //    private final BooleanInputPoll shouldBeRunning = BooleanMixing.andBooleans(allowToRun, BooleanMixing.orBooleans(Igneous.getIsTeleop(), forceEnabled));

    public DriveCode() {
        DriverImpls.createSynchMecanumDriver(Igneous.globalPeriodic, forwardAxis, strafeAxis, rotateAxis, leftFront, leftBack, rightFront, rightBack);
        /*
         * DriverImpls.createSynchTankDriver(EventMixing.filterEvent(shouldBeRunning
         * , true, Igneous.globalPeriodic), leftAxis, rightAxis, leftOut,
         * rightOut);
         */
    }

    public Entry[] queryRConf() throws InterruptedException {
        return new Entry[] { RConf.title("Drive Code"),
                //RConf.string("Axes:"), RConf.fieldFloat(leftAxis.get()), RConf.fieldFloat(rightAxis.get()),
                RConf.string("Allow To Run:"), RConf.fieldBoolean(allowToRun.get()),
                RConf.string("Force To Run:"), RConf.fieldBoolean(forceEnabled.get()) };
    }

    public boolean signalRConf(int field, byte[] data) throws InterruptedException {
        /*
         * switch (field) { case 2: if (data.length >= 4) {
         * leftOut.set(RConf.bytesToFloat(data)); } return true; case 3: if
         * (data.length >= 4) { rightOut.set(RConf.bytesToFloat(data)); } return
         * true; case 5: allowToRun.set(data.length > 0 && data[0] != 0); return
         * true; case 7: forceEnabled.set(data.length > 0 && data[0] != 0);
         * return true; default: return false; }
         */
        return false;
    }
}

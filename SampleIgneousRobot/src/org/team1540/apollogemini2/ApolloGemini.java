/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 * 
 * This file is part of the Revised ApolloGemini2014 project.
 * 
 * ApolloGemini2014 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * ApolloGemini2014 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ApolloGemini2014.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.apollogemini2;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanStatus;
import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class ApolloGemini implements IgneousApplication {

    public static final BooleanInput isKidMode;
    static {
        BooleanStatus kidModeStatus = new BooleanStatus();
        isKidMode = kidModeStatus;
        Cluck.publish("Kid Mode", kidModeStatus);
    }

    public void setupRobot() {
        if (Igneous.isRoboRIO()) {
            Cluck.setupServer(443);
            Cluck.setupServer(1540);
        }
        Cluck.setupServer(1180);

        displayBatteryLevel();

        Actuators.setup();
        DriveCode.setup();
        ReadoutDisplay.setupErrors();
        ReadoutDisplay.setupReadout();
        CompressorHandler.setup();
        Shooter.setup();
        if (Igneous.isRoboRIO()) {
            AutonomousFramework.setHotZoneTrigger(BooleanMixing.alwaysFalse);
        } else {
            AutonomousFramework.setHotZoneTrigger(KinectControl.main(
                    Igneous.globalPeriodic,
                    Igneous.getKinectJoystick(false),
                    Igneous.getKinectJoystick(true)));
        }
        AutonomousFramework.setup(); // Should go last because it needs help from everything else.
    }

    private void displayBatteryLevel() {
        Cluck.publish("Battery Level", FloatMixing.createDispatch(Igneous.getBatteryVoltage(), Igneous.globalPeriodic));
    }
}

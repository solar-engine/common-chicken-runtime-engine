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
            new CluckTCPServer(Cluck.getNode(), 443).start();
            new CluckTCPServer(Cluck.getNode(), 1540).start();
        }
        new CluckTCPServer(Cluck.getNode(), 1180).start();

        displayBatteryLevel();

        // TODO: Go through and normalize cluck channel names
        Actuators.setup();
        DriveCode.setup();
        ReadoutDisplay.setupErrors();
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

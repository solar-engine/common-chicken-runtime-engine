package org.team1540.valkyrie;

import ccre.cluck.Cluck;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Robot implements FRCApplication {

    @Override
    public void setupRobot() throws Throwable {
        FRC.controlBinding().addToggleButton("Solenoid").send(FRC.solenoid(1));
        DriveCode.setup();
        Cluck.publish("Fake Current Fault", DriveCode.disableMotorsForCurrentFault);
    }
}

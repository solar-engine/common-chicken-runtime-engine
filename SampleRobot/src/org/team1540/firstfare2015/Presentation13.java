package org.team1540.firstfare2015;

import ccre.frc.FRCApplication;
import ccre.log.Logger;

public class Presentation13 implements FRCApplication {

    public void setupRobot() {
        Logger.severe("The robot just broke!");
        Logger.warning("You can't fire it if it's not armed!");
        Logger.info("The match ended at 3:40 PM.");
        Logger.config("Kid mode is enabled.");
        Logger.fine("Started raising the elevator.");
        Logger.finer("Deactuated solenoid 7.");
        Logger.finest("Invoked SolenoidJNI.setSolenoid(0x7, false).");
        
        // automatically saved on the robot
        // and move up to your laptop when you next deploy code!
    }
}

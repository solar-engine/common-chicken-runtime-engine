package org.team1540.firstfare.presentation2014;

import ccre.igneous.IgneousApplication;
import ccre.log.Logger;

public class Presentation13 implements IgneousApplication {

    public void setupRobot() {
        Logger.severe("The robot just broke.");
        Logger.warning("You can't fire it if it's not armed!");
        Logger.info("The match ended at 3:40 PM.");
        Logger.config("Kid mode is now on.");
        Logger.fine("Started rearming the catapult.");
        Logger.finer("Deactuated solenoid 7.");
        Logger.finest("Invoked native setSolenoid(0x7, false)");
    }
}

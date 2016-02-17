package org.team1540.valkyrie;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.cluck.Cluck;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.recording.Recorder;

public class Robot implements FRCApplication {

    @Override
    public void setupRobot() throws Throwable {
        Recorder rc = FRC.getRecorder();
        ControlBindingCreator cb = FRC.controlBinding();

        BooleanIO out = new BooleanCell();
        cb.addEvent("Solenoid" + " Set", out.eventSet(true).combine(rc.createEventOutput("Solenoid-1-Set")));
        cb.addEvent("Solenoid" + " Reset", out.eventSet(false).combine(rc.createEventOutput("Solenoid-1-Reset")));
        cb.addEvent("Solenoid" + " Toggle", out.eventToggle().combine(rc.createEventOutput("Solenoid-1-Toggle")));
        out.send(FRC.solenoid(1).combine(rc.createBooleanOutput("Solenoid-1")));
        out.send(rc.createFloatOutput("Example-Float").addRamping(0.2f, FRC.constantPeriodic).fromBoolean(0.0f, 1.0f));

        DriveCode.setup(rc);
        Cluck.publish("Fake Current Fault", DriveCode.disableMotorsForCurrentFault);
    }
}

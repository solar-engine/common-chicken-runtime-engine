package org.team1540.cantest;

import ccre.ctrl.BooleanMixing;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotor.DiagnosticType;
import ccre.ctrl.ExtendedMotor.OutputControlMode;
import ccre.cluck.Cluck;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class CANTest implements IgneousApplication {

    // WARNING: This has never actually been tested on a real robot.
    
    public void setupRobot() {
        ExtendedMotor motor = Igneous.makeCANJaguar(0);
        Igneous.joystick1.getAxisSource(2).send(motor.asMode(OutputControlMode.VOLTAGE_FIXED));
        BooleanMixing.pumpWhen(Igneous.globalPeriodic, Igneous.getIsTeleop(), motor.asEnable());
        motor.setInternalPID(1, 0.1f, 0.01f);
        Cluck.publish("CAN Jaguar Bus Fault", BooleanMixing.createDispatch(motor.getDiagnosticChannel(DiagnosticType.BUS_VOLTAGE_FAULT), Igneous.globalPeriodic));
    }
}

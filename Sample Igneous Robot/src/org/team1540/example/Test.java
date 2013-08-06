package org.team1540.example;

import ccre.chan.*;
import ccre.ctrl.DriverImpls;
import ccre.event.EventSource;
import ccre.igneous.SimpleCore;

public class Test extends SimpleCore {
    protected void createSimpleControl() {
        // Driving
        FloatInputPoll leftAxis = joystick1.getAxisChannel(2);
        FloatInputPoll forwardAxis = joystick1.getAxisChannel(3);
        FloatInputPoll rightAxis = joystick1.getAxisChannel(5);
        FloatOutput leftOut = makeTalonMotor(2, MOTOR_FORWARD);
        FloatOutput rightOut = makeTalonMotor(1, MOTOR_REVERSE);
        DriverImpls.createExtendedSynchTankDriver(duringTeleop, leftAxis, rightAxis, forwardAxis, leftOut, rightOut);
        // Shifting
        EventSource shiftHighBtn = joystick1.getButtonSource(1);
        EventSource shiftLowBtn = joystick1.getButtonSource(3);
        BooleanStatus shifter = new BooleanStatus(makeSolenoid(2));
        shifter.setFalseWhen(startedTeleop);
        shifter.setTrueWhen(shiftLowBtn);
        shifter.setFalseWhen(shiftHighBtn);
        // Compressor
        useCompressor(1, 1);
    }
}

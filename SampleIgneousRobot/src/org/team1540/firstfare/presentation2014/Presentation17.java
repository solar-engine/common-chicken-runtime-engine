package org.team1540.firstfare.presentation2014;

import ccre.channel.BooleanFilter;
import ccre.channel.BooleanInputPoll;
import ccre.channel.FloatFilter;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation17 implements IgneousApplication {

    public void setupRobot() {
        FloatFilter negate = FloatMixing.negate;
        FloatFilter deadzone = FloatMixing.deadzone(0.1f);
        FloatFilter limit = FloatMixing.limit(-0.5f, 0.5f);
        FloatFilter shift = new FloatFilter() {
            @Override
            public float filter(float input) {
                return input + 0.05f;
            }
        };
        FloatOutput motor = Igneous.makeTalonMotor(0, Igneous.MOTOR_FORWARD, 0.1f);
        FloatOutput negatedMotor1 = FloatMixing.negate.wrap(motor);
        FloatOutput negatedMotor2 = FloatMixing.negate(motor);
        FloatOutput negatedMotor3 = negate.wrap(motor);

        FloatInputPoll input = Igneous.getBatteryVoltage();
        FloatInputPoll input2 = shift.wrap(input);

        FloatInput joystick = Igneous.joystick1.getAxisSource(1);
        FloatInput joystick2 = deadzone.wrap(joystick);

        BooleanFilter invert = BooleanMixing.invert;
        BooleanInputPoll isNotAutonomous = invert.wrap(Igneous.getIsAutonomous());
    }
}

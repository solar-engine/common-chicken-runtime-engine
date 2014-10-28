package org.team1540.firstfare.presentation2014;

import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousApplication;

public class Presentation06 implements IgneousApplication {

    public void setupRobot() {
        FloatInput input =
                Igneous.joystick1.getAxisSource(1);
        FloatInputPoll inputPoll =
                Igneous.joystick1.getAxisChannel(1);
        final FloatOutput motor =
                Igneous.makeJaguarMotor(0, Igneous.MOTOR_FORWARD, 0.1f);

        input.send(motor);

        input.send(new FloatOutput() {
            public void set(float value) {
                motor.set(value);
            }
        });

        float current_value1 = inputPoll.get();
        float current_value2 = input.get();

        FloatInputPoll converted = input;
        float current_value3 = converted.get();

        float out = current_value1 + current_value2 + current_value3;
        // Make warnings disappear in the screenshot.
    }
}

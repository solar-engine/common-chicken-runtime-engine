package org.team1540.firstfare2015;

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

public class Presentation07 implements FRCApplication {

    public void setupRobot() {
        // An input channel can be sent to an anonymous output.
        FloatInput axis = FRC.joystick1.axisY();
        BooleanInput bumper = FRC.digitalInput(0);
        EventInput button = FRC.joystick1.onPress(1);

        axis.send((float_value) -> doSomethingWith(float_value));
        bumper.send((boolean_value) -> doSomethingWith(boolean_value));
        button.send(() -> doSomething());
    }

    private Object doSomething() {
        // TODO Auto-generated method stub
        return null;
    }

    private Object doSomethingWith(Object value) {
        // TODO Auto-generated method stub
        return null;
    }
}

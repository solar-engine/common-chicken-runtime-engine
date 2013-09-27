package test;

import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.event.EventConsumer;
import ccre.igneous.SimpleCore;

// Most main Igneous applications will extend SimpleCore.
// Don't change this unless you know what you are doing.
public class Test extends SimpleCore {

    protected void createSimpleControl() {


        final FloatOutput leftMotorOutput = makeTalonMotor(1, MOTOR_FORWARD);
        final FloatOutput rightMotorOutput = makeTalonMotor(2, MOTOR_REVERSE);

        final FloatInputPoll joystickXAxis = joystick1.getXChannel();
        final FloatInputPoll joystickYAxis = joystick1.getYChannel();

        duringTeleop.addListener(new EventConsumer() {
            public void eventFired() {
                leftMotorOutput.writeValue(joystickXAxis.readValue() + joystickYAxis.readValue());
                rightMotorOutput.writeValue(-joystickXAxis.readValue() + joystickYAxis.readValue());
            }
        });
    }
}
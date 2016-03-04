package org.team1540.valkyrie;

import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.behaviors.ArbitratedFloat;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.recording.Recorder;

public class DriveCode {
    private static final ControlBindingCreator controls = FRC.controlBinding();

    public static final BooleanCell disableMotorsForCurrentFault = new BooleanCell(false);

    private static final BehaviorArbitrator driveControl = new BehaviorArbitrator("Drive Base").publish();
    private static final ArbitratedFloat leftMotors = driveControl.addFloat(), rightMotors = driveControl.addFloat();

    public static void setup(Recorder rc) {
        rc.recordBehaviors(driveControl);
        driveCode();
        pitMode();
        currentFault();

        leftMotors.send(FloatOutput.combine(FRC.talon(4, FRC.MOTOR_FORWARD), FRC.talon(5, FRC.MOTOR_REVERSE), FRC.talon(6, FRC.MOTOR_FORWARD)));
        rightMotors.send(FloatOutput.combine(FRC.talon(1, FRC.MOTOR_REVERSE), FRC.talon(2, FRC.MOTOR_REVERSE), FRC.talon(3, FRC.MOTOR_REVERSE)));
    }

    private static void driveCode() {
        Behavior defaultBehavior = driveControl.addBehavior("Teleop", FRC.inTeleopMode());
        BooleanInput shifted = controls.addToggleButton("Drive Shift Low", "Drive Shift High", "Drive Shift Toggle");
        FloatInput shiftMul = shifted.toFloat(1.0f, 0.5f);
        FloatInput triggerTerm = controls.addFloat("Drive Reverse").minus(controls.addFloat("Drive Forward"));
        leftMotors.attach(defaultBehavior, controls.addFloat("Drive Left").multipliedBy(shiftMul).plus(triggerTerm));
        rightMotors.attach(defaultBehavior, controls.addFloat("Drive Right").multipliedBy(shiftMul).plus(triggerTerm));
    }

    private static void pitMode() {
        BooleanCell pitMode = new BooleanCell();
        Cluck.publish("(PIT) Pit Mode", pitMode);
        pitMode.setFalseWhen(FRC.startTele.and(FRC.isOnFMS()));

        Behavior pitBehavior = driveControl.addBehavior("Pit Mode", pitMode);
        leftMotors.attach(pitBehavior, FloatInput.zero);
        rightMotors.attach(pitBehavior, FloatInput.zero);
    }

    private static void currentFault() {
        Behavior disableBehavior = driveControl.addBehavior("Current Fault", disableMotorsForCurrentFault);
        leftMotors.attach(disableBehavior, FloatInput.zero);
        rightMotors.attach(disableBehavior, FloatInput.zero);
    }
}

package org.team1540.apollogemini2;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.igneous.Igneous;

public class DriveCode {

    public static void setup() {
        FloatOutput leftDrive1, leftDrive2, rightDrive1, rightDrive2;
        BooleanOutput shiftSolenoid;
        if (Igneous.isRoboRIO()) {
            leftDrive1 = Igneous.makeTalonMotor(2, Igneous.MOTOR_REVERSE, 0.1f);
            leftDrive2 = Igneous.makeTalonMotor(3, Igneous.MOTOR_REVERSE, 0.1f);
            rightDrive1 = Igneous.makeTalonMotor(4, Igneous.MOTOR_FORWARD, 0.1f);
            rightDrive2 = Igneous.makeTalonMotor(5, Igneous.MOTOR_FORWARD, 0.1f);
            shiftSolenoid = Igneous.makeSolenoid(7);
        } else {
            leftDrive1 = Igneous.makeVictorMotor(1, Igneous.MOTOR_FORWARD, 0.1f);
            leftDrive2 = Igneous.makeVictorMotor(2, Igneous.MOTOR_FORWARD, 0.1f);
            rightDrive1 = Igneous.makeVictorMotor(3, Igneous.MOTOR_REVERSE, 0.1f);
            rightDrive2 = Igneous.makeVictorMotor(4, Igneous.MOTOR_REVERSE, 0.1f);
            shiftSolenoid = Igneous.makeSolenoid(1);
        }
        FloatOutput leftDrive = FloatMixing.combine(leftDrive1, leftDrive2), rightDrive = FloatMixing.combine(rightDrive1, rightDrive2);
        Cluck.publish("test-drive-left", leftDrive);
        Cluck.publish("test-drive-right", rightDrive);
        Cluck.publish("test-drive-left-1", leftDrive1);
        Cluck.publish("test-drive-right-1", rightDrive1);
        Cluck.publish("test-drive-left-2", leftDrive2);
        Cluck.publish("test-drive-right-2", rightDrive2);
        Cluck.publish("test-shift-high", shiftSolenoid);

        setupDriveCode(leftDrive, rightDrive, shiftSolenoid);
        AutonomousFramework.addDriveMotors(leftDrive, rightDrive);
    }

    private static void setupDriveCode(FloatOutput leftDrive, FloatOutput rightDrive, BooleanOutput shiftSolenoidHigh) {
        final BooleanInputPoll actuallyDisable = BooleanMixing.andBooleans(Shooter.getShouldDisableDrivingAndCompressor(), UserInterface.getShouldOverrideDrivingDisable());

        FloatInputPoll speedScale = Mixing.select(actuallyDisable, Mixing.select(ApolloGemini.isKidMode, 1.0f, 0.5f), FloatMixing.always(0.0f));
        
        DriverImpls.createSynchTankDriver(Igneous.duringTele,
                UserInterface.getLeftAxis(), UserInterface.getRightAxis(),
                FloatMixing.multiplication.of(leftDrive, speedScale), FloatMixing.multiplication.of(rightDrive, speedScale));

        final BooleanStatus isHighGear = new BooleanStatus(shiftSolenoidHigh);
        isHighGear.setTrueWhen(Igneous.startTele); // begin
        isHighGear.setFalseWhen(Igneous.startAuto);
        isHighGear.setTrueWhen(UserInterface.getHighGearShift()); // high
        isHighGear.setFalseWhen(UserInterface.getLowGearShift()); // low
        Cluck.publish("drive-is-high-gear", isHighGear);
    }
}

/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 *
 * This file is part of the Revised ApolloGemini2014 project.
 *
 * ApolloGemini2014 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * ApolloGemini2014 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ApolloGemini2014.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.apollogemini2;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.Mixing;
import ccre.igneous.Igneous;

public class DriveCode {

    public static void setup() {
        FloatOutput leftDrive1, leftDrive2, rightDrive1, rightDrive2;
        BooleanOutput shiftSolenoidHigh;
        if (Igneous.isRoboRIO()) {
            leftDrive1 = Igneous.makeTalonMotor(2, Igneous.MOTOR_REVERSE, 0.1f);
            leftDrive2 = Igneous.makeTalonMotor(3, Igneous.MOTOR_REVERSE, 0.1f);
            rightDrive1 = Igneous.makeTalonMotor(4, Igneous.MOTOR_FORWARD, 0.1f);
            rightDrive2 = Igneous.makeTalonMotor(5, Igneous.MOTOR_FORWARD, 0.1f);
            shiftSolenoidHigh = Igneous.makeSolenoid(7);
        } else {
            leftDrive1 = Igneous.makeVictorMotor(1, Igneous.MOTOR_FORWARD, 0.1f);
            leftDrive2 = Igneous.makeVictorMotor(2, Igneous.MOTOR_FORWARD, 0.1f);
            rightDrive1 = Igneous.makeVictorMotor(3, Igneous.MOTOR_REVERSE, 0.1f);
            rightDrive2 = Igneous.makeVictorMotor(4, Igneous.MOTOR_REVERSE, 0.1f);
            shiftSolenoidHigh = Igneous.makeSolenoid(1);
        }
        FloatOutput leftDrive = leftDrive1.combine(leftDrive2), rightDrive = rightDrive1.combine(rightDrive2);
        Cluck.publish("Drive Motors Left", leftDrive);
        Cluck.publish("Drive Motors Right", rightDrive);
        Cluck.publish("Drive Motor Left 1", leftDrive1);
        Cluck.publish("Drive Motor Right 1", rightDrive1);
        Cluck.publish("Drive Motor Left 2", leftDrive2);
        Cluck.publish("Drive Motor Right 2", rightDrive2);

        setupDriveCode(leftDrive, rightDrive, shiftSolenoidHigh);
        AutonomousModeBase.addDriveMotors(leftDrive, rightDrive);
    }

    private static void setupDriveCode(FloatOutput leftDrive, FloatOutput rightDrive, BooleanOutput shiftSolenoidHigh) {
        final BooleanInput actuallyDisable = Shooter.getShouldDisableDrivingAndCompressor().and(UserInterface.getShouldOverrideDrivingDisable());

        FloatInput speedScale = Mixing.select(actuallyDisable, Mixing.select(ApolloGemini.isKidMode, 1.0f, 0.5f), FloatInput.always(0.0f));

        DriverImpls.extendedTankDrive(
                UserInterface.getLeftAxis(), UserInterface.getRightAxis(), UserInterface.getForwardAxis(),
                leftDrive.outputMultipliedBy(speedScale), rightDrive.outputMultipliedBy(speedScale));

        final BooleanStatus isHighGear = new BooleanStatus(shiftSolenoidHigh);
        isHighGear.setTrueWhen(Igneous.startTele); // begin
        isHighGear.setFalseWhen(Igneous.startAuto);
        isHighGear.setTrueWhen(UserInterface.getHighGearShift()); // high
        isHighGear.setFalseWhen(UserInterface.getLowGearShift()); // low
        Cluck.publish("Drive Shifting High Gear", isHighGear);
    }
}

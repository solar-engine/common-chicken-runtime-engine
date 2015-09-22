/*
 * Copyright 2014-2015 Colby Skeggs
 *
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 *
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.cantest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotor.DiagnosticType;
import ccre.ctrl.ExtendedMotor.OutputControlMode;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;

/**
 * A simple test program that will allow testing control of a CAN Jaguar.
 *
 * @author skeggsc
 */
public class CANTest implements FRCApplication {
    private static Logger logger = LoggerFactory.getLogger(CANTest.class);

    // WARNING: This has never actually been tested on a real robot.

    public void setupRobot() {
        try {
            ExtendedMotor motor = FRC.makeCANJaguar(0);
            FRC.joystick1.axis(2).send(motor.asMode(OutputControlMode.VOLTAGE_FIXED));
            FRC.getIsTeleop().send(motor.asEnable());
            motor.setInternalPID(1, 0.1f, 0.01f);
            Cluck.publish("CAN Jaguar Bus Fault", motor.getDiagnosticChannel(DiagnosticType.BUS_VOLTAGE_FAULT));
        } catch (ExtendedMotorFailureException e) {
            logger.error("Failed to initialize", e);
            throw new RuntimeException();
        }
    }
}

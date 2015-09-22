/*
 * Copyright 2013-2015 Colby Skeggs
 * Copyright 2014 Alexander Mackworth (single joystick)
 * Copyright 2015 Aidan Smith (mecanum)
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
package ccre.ctrl;

import ccre.channel.DerivedUpdate;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;

/**
 * Contains various presets for driving the robot.
 *
 * Provided types of drive:
 *
 * Tank drive is where two joystick axes are used to control two motors,
 * respectively.
 *
 * Extended tank drive adds another joystick axis that is added to both motors'
 * outputs, so that direct forward and backward movement is easy.
 *
 * Single joystick drive, aka Arcade drive, is where one joystick is used to
 * control a robot in rotation and movement.
 *
 * Mecanum drive only works with a special kind of wheel, and allows strafing in
 * addition to normal movement and rotation.
 *
 * Other types to come later.
 *
 * Mixing contains many more generic methods to work with channels.
 *
 * @see Mixing
 * @author skeggsc
 */
public class DriverImpls {

    /**
     * Run tank drive on the given two FloatInputs and FloatOutputs.
     *
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void tankDrive(FloatInput leftIn, FloatInput rightIn, FloatOutput leftOut, FloatOutput rightOut) {
        leftIn.send(leftOut);
        rightIn.send(rightOut);
    }

    /**
     * Run extended tank drive on the given three FloatInputs and FloatOutputs.
     *
     * @param leftIn the left control axis.
     * @param rightIn the right control axis.
     * @param forward the forward control axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void extendedTankDrive(FloatInput leftIn, FloatInput rightIn, FloatInput forward, FloatOutput leftOut, FloatOutput rightOut) {
        leftIn.plus(forward).send(leftOut);
        rightIn.plus(forward).send(rightOut);
    }

    /**
     * Run arcade drive on the given two FloatInputs and FloatOutputs.
     *
     * @param joystickXAxis the joystick's x-axis.
     * @param joystickYAxis the joystick's y-axis.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void arcadeDrive(final FloatInput joystickXAxis, final FloatInput joystickYAxis, final FloatOutput leftOut, final FloatOutput rightOut) {
        joystickYAxis.plus(joystickXAxis).send(leftOut);
        joystickYAxis.minus(joystickXAxis).send(rightOut);
    }

    /**
     * Run arcade drive on the given Joystick.
     *
     * @param joystick the joystick.
     * @param leftOut the left motor.
     * @param rightOut the right motor.
     * @see DriverImpls
     */
    public static void arcadeDrive(final IJoystick joystick, final FloatOutput leftOut, final FloatOutput rightOut) {
        arcadeDrive(joystick.axisX(), joystick.axisY(), leftOut, rightOut);
    }

    /**
     * When the returned EventInput is fired, run Mecanum drive on the given
     * FloatInputs and FloatOutputs.
     *
     * @param forward the forward movement axis.
     * @param strafe the strafing axis.
     * @param rotate the rotation axis.
     * @param leftFrontMotor the left front motor.
     * @param leftBackMotor the left back motor.
     * @param rightFrontMotor the right front motor.
     * @param rightBackMotor the right back motor.
     * @return the EventOutput that will update the motors.
     * @see DriverImpls
     */
    public static void mecanumDrive(final FloatInput forward, final FloatInput strafe, final FloatInput rotate, final FloatOutput leftFrontMotor, final FloatOutput leftBackMotor, final FloatOutput rightFrontMotor, final FloatOutput rightBackMotor) {
        // TODO: Optimize this?
        new DerivedUpdate(forward, strafe, rotate) {
            @Override
            protected void update() {
                float distanceY = forward.get();
                float distanceX = strafe.get();
                float rotationspeed = rotate.get();
                double speed = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                if (speed > 1) {
                    speed = 1;
                }
                double angle = Math.atan2(distanceY, distanceX) - Math.PI / 4;

                double sin = speed * Math.sin(angle), cos = speed * Math.cos(angle);
                double leftFront = sin - rotationspeed;
                double rightBack = sin + rotationspeed;
                double leftBack = cos - rotationspeed;
                double rightFront = cos + rotationspeed;
                double normalize = Math.max(
                        Math.max(Math.abs(leftFront), Math.abs(rightFront)),
                        Math.max(Math.abs(leftBack), Math.abs(rightBack)));
                double mul;
                if (normalize > 1) {
                    mul = 1 / normalize;
                } else if (normalize < speed) {
                    mul = speed / normalize;
                } else {
                    mul = 1;
                }
                rightFrontMotor.set((float) (rightFront * mul));
                leftFrontMotor.set((float) (leftFront * mul));
                rightBackMotor.set((float) (rightBack * mul));
                leftBackMotor.set((float) (leftBack * mul));
            }
        };
    }

    private DriverImpls() {
    }
}

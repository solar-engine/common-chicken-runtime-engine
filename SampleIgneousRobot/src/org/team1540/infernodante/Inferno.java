/*
 * Copyright 2013-2014 Colby Skeggs, Casey Currey-Wilson
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
package org.team1540.infernodante;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.EventMixing;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.ctrl.MultipleSourceBooleanController;
import ccre.igneous.Igneous;
import ccre.igneous.IgneousCore;
import ccre.phidget.PhidgetReader;

import java.util.Timer;
import java.util.TimerTask;

// Please note that this is not the best example of an Igneous program.
// A better example would be our 2014 code.
// This is an incomplete badly-ported version of our 2013 code.
public class Inferno extends IgneousCore {

    public static final boolean IS_COMPETITION_ROBOT = true;
    public static final long FRISBEE_SERVO_DELAY_MILLIS = 500;
    private BooleanInput isKiddieMode;
    private PController armController;

    private void createShifting() {
        EventInput shiftHighBtn = joystick1.getButtonSource(1);
        EventInput shiftLowBtn = joystick1.getButtonSource(3);
        BooleanStatus shifter = new BooleanStatus(makeSolenoid(2));
        shifter.setFalseWhen(startTele);
        shifter.setTrueWhen(shiftLowBtn);
        shifter.setFalseWhen(shiftHighBtn);
    }

    private void createDriving() {
        isKiddieMode = IS_COMPETITION_ROBOT ? BooleanMixing.alwaysFalse : PhidgetReader.getDigitalInput(4);
        FloatInputPoll leftAxis = joystick1.getAxisChannel(2);
        FloatInputPoll forwardAxis = joystick1.getAxisChannel(3);
        FloatInputPoll rightAxis = joystick1.getAxisChannel(5);
        FloatOutput leftOut = makeTalonMotor(2, MOTOR_FORWARD, 0);
        FloatOutput rightOut = makeTalonMotor(1, IS_COMPETITION_ROBOT ? MOTOR_REVERSE : MOTOR_FORWARD, 0);
        DriverImpls.createExtendedSynchTankDriver(duringTele, leftAxis, rightAxis, forwardAxis, leftOut, rightOut);
    }

    private void createShooterWheel() {
        FloatOutput wheel = FloatMixing.combine(makeTalonMotor(3, MOTOR_FORWARD, 0), makeTalonMotor(4, MOTOR_FORWARD, 0), makeTalonMotor(5, MOTOR_FORWARD, 0));
        FloatInput moddedSpeed = Mixing.select(isKiddieMode, 1f, 0.5f);
        BooleanOutput wheelControl = Mixing.select(wheel, FloatMixing.always(0f), moddedSpeed);
        BooleanInputPoll runWheelBtn = BooleanMixing.orBooleans(PhidgetReader.getDigitalInput(5), joystick1.getButtonChannel(4));
        BooleanMixing.pumpWhen(duringTele, runWheelBtn, wheelControl);
    }

    private void createShooterPistons() {
        BooleanOutput fire = BooleanMixing.combine(makeSolenoid(1), makeSolenoid(4));
        BooleanInputPoll shootWheelBtn = BooleanMixing.orBooleans(PhidgetReader.getDigitalInput(7), joystick1.getButtonChannel(2));
        BooleanMixing.pumpWhen(duringTele, shootWheelBtn, fire);
    }

    private void createArm() {
        FloatInputPoll manualArm = PhidgetReader.getAnalogInput(5);
        FloatInputPoll armPotentiometer = makeAnalogInput(2, 9);
        Cluck.publish("arm-potentiometer", FloatMixing.createDispatch(armPotentiometer, globalPeriodic));
        FloatOutput armMotor = IS_COMPETITION_ROBOT ? makeTalonMotor(6, MOTOR_FORWARD, 0) : makeVictorMotor(6, MOTOR_REVERSE, 0);

        createPotentiometerReadout(armPotentiometer);

        armController = new PController(armPotentiometer, armMotor, FloatMixing.deadzone(0.05f).wrap(manualArm));
        armController.updateWhen(duringTele);

        BooleanStatus deactivateBrake = IS_COMPETITION_ROBOT ? new BooleanStatus(makeSolenoid(5)) : new BooleanStatus();
        armController.isBrakeDeactivated = deactivateBrake;

        createArmButtonActions(deactivateBrake);
    }

    private void createPotentiometerReadout(FloatInputPoll pot) {
        makeDSFloatReadout("Poten", 2, pot, globalPeriodic);
    }

    private void createArmButtonActions(BooleanStatus deactivateBrake) {
        // Check when the arm override mover is moved.
        BooleanInputPoll isArmOverride = FloatMixing.floatIsOutsideRange(PhidgetReader.getAnalogInput(5), -0.2f, 0.2f);
        EventInput duringArmOverride = EventMixing.filterEvent(isArmOverride, true, (EventInput) duringTele);
        // Check when one of the analog-dispatch buttons is activated.
        FloatInputPoll armSelectorValue = PhidgetReader.getAnalogInput(4);
        BooleanInputPoll isArmFront = FloatMixing.floatIsInRange(armSelectorValue, -0.26f, -0.24f); // Measured constants.
        BooleanInputPoll isArmTop = FloatMixing.floatIsInRange(armSelectorValue, -1f, -0.99f);
        BooleanInputPoll isArmBack = FloatMixing.floatIsInRange(armSelectorValue, 0.362f, 0.382f);
        EventInput onPressArmFront = BooleanMixing.whenBooleanBecomes(isArmFront, true, duringTele);
        EventInput onPressArmTop = BooleanMixing.whenBooleanBecomes(isArmTop, true, duringTele);
        EventInput onPressArmBack = BooleanMixing.whenBooleanBecomes(isArmBack, true, duringTele);
        // Chuck when one of the normal buttons is pressed
        EventInput onPressDropSuction = BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(2), true);
        EventInput onPressPyramidActuated = BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(6), true);
        // Activate the break when the pyramid actuation button is pressed, deactivate on anything else
        deactivateBrake.setFalseWhen(onPressPyramidActuated);
        deactivateBrake.setTrueWhen(duringArmOverride);
        deactivateBrake.setTrueWhen(onPressArmFront);
        deactivateBrake.setTrueWhen(onPressArmTop);
        deactivateBrake.setTrueWhen(onPressArmBack);
        deactivateBrake.setTrueWhen(onPressDropSuction);
        // Disable the controller when overriden, enable it if you press any setpoint button
        armController.disableWhen(duringArmOverride);
        armController.enableWhen(onPressArmFront);
        armController.enableWhen(onPressArmTop);
        armController.enableWhen(onPressArmBack);
        armController.enableWhen(onPressDropSuction);
        // Set the position target if you press any setpoint button
        armController.setSetpointWhen(PController.ARM_PICKUP_PRESET, onPressArmFront);
        armController.setSetpointWhen(PController.ARM_DRIVE_PRESET, onPressArmTop);
        armController.setSetpointWhen(PController.ARM_LOAD_PRESET, onPressArmBack);
        armController.setSetpointWhen(PController.ARM_DROP_PRESET, onPressDropSuction);
        // Once stable and set to the pickup position, suspend the arm.
        //armController.suspendOnceStable = Mixing.floatsEqual(armController.setpoint, PController.ARM_PICKUP_PRESET);
    }

    private void createArmDropper() {
        BooleanInput isCorralSwitchUp = PhidgetReader.getDigitalInput(0); // TODO: Make this default to true!
        FloatOutput armServo = makeServo(7, 0, 170);
        BooleanOutput armDrop = makeSolenoid(3);
        ExpirationTimer delayServoClose = new ExpirationTimer();
        delayServoClose.schedule(FRISBEE_SERVO_DELAY_MILLIS, FloatMixing.getSetEvent(armServo, 120));
        isCorralSwitchUp.send(delayServoClose.getRunningControl());
        isCorralSwitchUp.send(BooleanMixing.triggerWhenBooleanChanges(FloatMixing.getSetEvent(armServo, 0), null));
        isCorralSwitchUp.send(armDrop);
    }

    private void createLightPatternGenerator() {
        final BooleanOutput backArmLED = PhidgetReader.getDigitalOutput(4), topArmLED = PhidgetReader.getDigitalOutput(6);
        final BooleanOutput frontArmLED = PhidgetReader.getDigitalOutput(5), dropSuctionLED = PhidgetReader.getDigitalOutput(2);
        final BooleanOutput leftSuctionLED = PhidgetReader.getDigitalOutput(1), rightSuctionLED = PhidgetReader.getDigitalOutput(3);

        final BooleanOutput leftLEDs = BooleanMixing.combine(backArmLED, topArmLED, frontArmLED);
        final BooleanOutput rightLEDs = BooleanMixing.combine(dropSuctionLED, leftSuctionLED, rightSuctionLED);
        final BooleanInputPoll isDisabled = Igneous.getIsDisabled(), isAuto = Igneous.getIsAutonomous();
        new Timer().schedule(new TimerTask() {
            private int frame = 0;

            public void run() {
                frame++;
                if (isDisabled.get()) {
                    frame %= 12;
                    backArmLED.set(frame < 2);
                    topArmLED.set(frame >= 2 && frame < 4);
                    frontArmLED.set(frame >= 4 && frame < 6);
                    rightSuctionLED.set(frame >= 6 && frame < 8);
                    dropSuctionLED.set(frame >= 8 && frame < 10);
                    leftSuctionLED.set(frame >= 10);
                } else if (isAuto.get()) {
                    frame %= 8;
                    boolean on = frame < 4;
                    leftLEDs.set(on);
                    rightLEDs.set(!on);
                } else { // Teleop!
                    rightSuctionLED.set(false);
                    leftSuctionLED.set(false);
                    frame %= 4;
                    if (armController == null) {
                        boolean on = frame < 2;
                        frontArmLED.set(on);
                        topArmLED.set(on);
                        backArmLED.set(on);
                        dropSuctionLED.set(on);
                    } else {
                        if (!armController.enabled.get()) {
                            frontArmLED.set(false);
                            topArmLED.set(false);
                            backArmLED.set(false);
                            dropSuctionLED.set(false);
                        } else {
                            float val = armController.setpoint.get();
                            // Pickup, drive, load, drop
                            if (val == PController.ARM_PICKUP_PRESET.get()) {
                                frontArmLED.set(true);
                                topArmLED.set(false);
                                backArmLED.set(false);
                                dropSuctionLED.set(false);
                            } else if (val == PController.ARM_DRIVE_PRESET.get()) {
                                frontArmLED.set(false);
                                topArmLED.set(true);
                                backArmLED.set(false);
                                dropSuctionLED.set(false);
                            } else if (val == PController.ARM_LOAD_PRESET.get()) {
                                frontArmLED.set(false);
                                topArmLED.set(false);
                                backArmLED.set(true);
                                dropSuctionLED.set(false);
                            } else if (val == PController.ARM_DROP_PRESET.get()) {
                                frontArmLED.set(false);
                                topArmLED.set(false);
                                backArmLED.set(false);
                                dropSuctionLED.set(true);
                            } else {
                                boolean on = frame < 2;
                                frontArmLED.set(on);
                                topArmLED.set(!on);
                                backArmLED.set(on);
                                dropSuctionLED.set(!on);
                            }
                        }
                    }
                }
            }
        }, 50, 100);
    }

    private void createClimber() {
        MultipleSourceBooleanController valve = new MultipleSourceBooleanController(MultipleSourceBooleanController.OR);
        valve.send(makeSolenoid(8));
        valve.addInput(PhidgetReader.getDigitalInput(3));
        BooleanStatus climbControl = new BooleanStatus(makeSolenoid(6));
        valve.addInput(climbControl);
        climbControl.setTrueWhen(EventMixing.filterEvent(PhidgetReader.getDigitalInput(1), true, duringTele));
        climbControl.setFalseWhen(EventMixing.filterEvent(PhidgetReader.getDigitalInput(3), true, duringTele));
    }

    private void createPressureMonitoring() {
        final FloatInputPoll pressure = FloatMixing.normalizeFloat(makeAnalogInput_ValueBased(1, 14), 100, 587);
        globalPeriodic.send(new EventOutput() {
            public void event() {
                PhidgetReader.getLCDLine(0).println("Pressure: " + ((int) (pressure.get() * 100)) + "%\n");
            }
        });
        useCustomCompressor(FloatMixing.floatIsAtLeast(pressure, 1.0f), 1);
    }

    public void setupRobot() {
        createLightPatternGenerator();
        createShifting();
        createDriving();
        createShooterWheel();
        createShooterPistons();
        createArm();
        createArmDropper();
        createClimber();
        createPressureMonitoring();
    }
}

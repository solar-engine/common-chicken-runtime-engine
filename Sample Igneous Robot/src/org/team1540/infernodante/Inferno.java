/*
 * Copyright 2013 Colby Skeggs, Casey Currey-Wilson
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

import ccre.chan.*;
import ccre.cluck.CluckGlobals;
import ccre.ctrl.*;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.igneous.SimpleCore;
import ccre.phidget.PhidgetReader;
import ccre.ctrl.ExpirationTimer;
import java.util.Timer;
import java.util.TimerTask;

public class Inferno extends SimpleCore {

    public static final boolean IS_COMPETITION_ROBOT = true;
    public static final long FRISBEE_SERVO_DELAY_MILLIS = 500;
    private BooleanInput isKiddieMode;

    private void createShifting() {
        EventSource shiftHighBtn = joystick1.getButtonSource(1);
        EventSource shiftLowBtn = joystick1.getButtonSource(3);
        BooleanStatus shifter = new BooleanStatus(makeSolenoid(2));
        shifter.setFalseWhen(startedTeleop);
        shifter.setTrueWhen(shiftLowBtn);
        shifter.setFalseWhen(shiftHighBtn);
    }

    private void createDriving() {
        isKiddieMode = IS_COMPETITION_ROBOT ? Mixing.alwaysFalse : PhidgetReader.digitalInputs[4];
        FloatInputPoll leftAxis = joystick1.getAxisChannel(2);
        FloatInputPoll forwardAxis = joystick1.getAxisChannel(3);
        FloatInputPoll rightAxis = joystick1.getAxisChannel(5);
        FloatOutput leftOut = makeTalonMotor(2, MOTOR_FORWARD, 0);
        FloatOutput rightOut = makeTalonMotor(1, IS_COMPETITION_ROBOT ? MOTOR_REVERSE : MOTOR_FORWARD, 0);
        DriverImpls.createExtendedSynchTankDriver(duringTeleop, leftAxis, rightAxis, forwardAxis, leftOut, rightOut);
    }

    private void createShooterWheel() {
        FloatOutput wheel = Mixing.combineFloats(makeTalonMotor(3, MOTOR_FORWARD, 0), makeTalonMotor(4, MOTOR_FORWARD, 0), makeTalonMotor(5, MOTOR_FORWARD, 0));
        FloatInput moddedSpeed = Mixing.booleanSelectFloat(isKiddieMode, 1f, 0.5f);
        BooleanOutput wheelControl = Mixing.booleanSelectFloat(wheel, Mixing.always(0f), moddedSpeed);
        BooleanInputPoll runWheelBtn = Mixing.orBooleans(PhidgetReader.digitalInputs[5], joystick1.getButtonChannel(4));
        Mixing.pumpWhen(duringTeleop, runWheelBtn, wheelControl);
    }

    private void createShooterPistons() {
        BooleanOutput fire = Mixing.combineBooleans(makeSolenoid(1), makeSolenoid(4));
        BooleanInputPoll shootWheelBtn = Mixing.orBooleans(PhidgetReader.digitalInputs[7], joystick1.getButtonChannel(2));
        Mixing.pumpWhen(duringTeleop, shootWheelBtn, fire);
    }
    private PController armController;

    private void createArm() {
        FloatInputPoll manualArm = PhidgetReader.analogInputs[5];
        FloatInputPoll armPotentiometer = makeAnalogInput(2, 9);
        CluckGlobals.node.publish("arm-potentiometer", Mixing.createDispatch(armPotentiometer, globalPeriodic));
        FloatOutput armMotor = IS_COMPETITION_ROBOT ? makeTalonMotor(6, MOTOR_FORWARD, 0) : makeVictorMotor(6, MOTOR_REVERSE, 0);

        createPotentiometerReadout(armPotentiometer);

        armController = new PController(armPotentiometer, armMotor, Mixing.deadzone(manualArm, 0.05f));
        armController.updateWhen(duringTeleop);

        BooleanStatus deactivateBrake = IS_COMPETITION_ROBOT ? new BooleanStatus(makeSolenoid(5)) : new BooleanStatus();
        armController.isBrakeDeactivated = deactivateBrake;

        createArmButtonActions(deactivateBrake);
    }

    private void createPotentiometerReadout(FloatInputPoll pot) {
        makeDSFloatReadout("Poten", 2, pot, globalPeriodic);
    }

    private void createArmButtonActions(BooleanStatus deactivateBrake) {
        // Check when the arm override mover is moved.
        BooleanInputPoll isArmOverride = Mixing.floatIsOutsideRange(PhidgetReader.analogInputs[5], -0.2f, 0.2f);
        EventSource duringArmOverride = Mixing.filterEvent(isArmOverride, true, (EventSource) duringTeleop);
        // Check when one of the analog-dispatch buttons is activated.
        FloatInputPoll armSelectorValue = PhidgetReader.analogInputs[4];
        BooleanInputPoll isArmFront = Mixing.floatIsInRange(armSelectorValue, -0.26f, -0.24f); // Measured constants.
        BooleanInputPoll isArmTop = Mixing.floatIsInRange(armSelectorValue, -1f, -0.99f);
        BooleanInputPoll isArmBack = Mixing.floatIsInRange(armSelectorValue, 0.362f, 0.382f);
        EventSource onPressArmFront = Mixing.whenBooleanBecomes(isArmFront, true, duringTeleop);
        EventSource onPressArmTop = Mixing.whenBooleanBecomes(isArmTop, true, duringTeleop);
        EventSource onPressArmBack = Mixing.whenBooleanBecomes(isArmBack, true, duringTeleop);
        // Chuck when one of the normal buttons is pressed
        EventSource onPressDropSuction = Mixing.whenBooleanBecomes(PhidgetReader.digitalInputs[2], true);
        EventSource onPressPyramidActuated = Mixing.whenBooleanBecomes(PhidgetReader.digitalInputs[6], true);
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
        armController.suspendOnceStable = Mixing.floatsEqual(armController.setpoint, PController.ARM_PICKUP_PRESET);
    }

    private void createArmDropper() {
        BooleanInputProducer isCorralSwitchUp = PhidgetReader.digitalInputs[0]; // TODO: Make this default to true!
        FloatOutput armServo = makeServo(7, 0, 170);
        BooleanOutput armDrop = makeSolenoid(3);
        ExpirationTimer delayServoClose = new ExpirationTimer();
        delayServoClose.schedule(FRISBEE_SERVO_DELAY_MILLIS, Mixing.getSetEvent(armServo, 120));
        isCorralSwitchUp.addTarget(delayServoClose.getRunningControl());
        isCorralSwitchUp.addTarget(Mixing.triggerWhenBooleanChanges(Mixing.getSetEvent(armServo, 0), null));
        isCorralSwitchUp.addTarget(armDrop);
    }

    private void createLightPatternGenerator() {
        final BooleanOutput[] outs = PhidgetReader.digitalOutputs;
        final BooleanOutput backArmLED = outs[4], topArmLED = outs[6], frontArmLED = outs[5];
        final BooleanOutput dropSuctionLED = outs[2], leftSuctionLED = outs[1], rightSuctionLED = outs[3];

        final BooleanOutput leftLEDs = Mixing.combineBooleans(backArmLED, topArmLED, frontArmLED);
        final BooleanOutput rightLEDs = Mixing.combineBooleans(dropSuctionLED, leftSuctionLED, rightSuctionLED);
        final BooleanInputPoll isDisabled = this.getIsDisabled(), isAuto = this.getIsAutonomous();
        new Timer().schedule(new TimerTask() {
            private int frame = 0;

            public void run() {
                frame++;
                if (isDisabled.readValue()) {
                    frame %= 12;
                    backArmLED.writeValue(frame < 2);
                    topArmLED.writeValue(frame >= 2 && frame < 4);
                    frontArmLED.writeValue(frame >= 4 && frame < 6);
                    rightSuctionLED.writeValue(frame >= 6 && frame < 8);
                    dropSuctionLED.writeValue(frame >= 8 && frame < 10);
                    leftSuctionLED.writeValue(frame >= 10);
                } else if (isAuto.readValue()) {
                    frame %= 8;
                    boolean on = frame < 4;
                    leftLEDs.writeValue(on);
                    rightLEDs.writeValue(!on);
                } else { // Teleop!
                    rightSuctionLED.writeValue(false);
                    leftSuctionLED.writeValue(false);
                    frame %= 4;
                    if (armController == null) {
                        boolean on = frame < 2;
                        frontArmLED.writeValue(on);
                        topArmLED.writeValue(on);
                        backArmLED.writeValue(on);
                        dropSuctionLED.writeValue(on);
                    } else {
                        if (!armController.enabled.readValue()) {
                            frontArmLED.writeValue(false);
                            topArmLED.writeValue(false);
                            backArmLED.writeValue(false);
                            dropSuctionLED.writeValue(false);
                        } else {
                            float val = armController.setpoint.readValue();
                            // Pickup, drive, load, drop
                            if (val == PController.ARM_PICKUP_PRESET.readValue()) {
                                frontArmLED.writeValue(true);
                                topArmLED.writeValue(false);
                                backArmLED.writeValue(false);
                                dropSuctionLED.writeValue(false);
                            } else if (val == PController.ARM_DRIVE_PRESET.readValue()) {
                                frontArmLED.writeValue(false);
                                topArmLED.writeValue(true);
                                backArmLED.writeValue(false);
                                dropSuctionLED.writeValue(false);
                            } else if (val == PController.ARM_LOAD_PRESET.readValue()) {
                                frontArmLED.writeValue(false);
                                topArmLED.writeValue(false);
                                backArmLED.writeValue(true);
                                dropSuctionLED.writeValue(false);
                            } else if (val == PController.ARM_DROP_PRESET.readValue()) {
                                frontArmLED.writeValue(false);
                                topArmLED.writeValue(false);
                                backArmLED.writeValue(false);
                                dropSuctionLED.writeValue(true);
                            } else {
                                boolean on = frame < 2;
                                frontArmLED.writeValue(on);
                                topArmLED.writeValue(!on);
                                backArmLED.writeValue(on);
                                dropSuctionLED.writeValue(!on);
                            }
                        }
                    }
                }
            }
        }, 50, 100);
    }
    
    private void createClimber() {
        MultipleSourceBooleanController valve = new MultipleSourceBooleanController(MultipleSourceBooleanController.OR);
        valve.addTarget(makeSolenoid(8));
        valve.addInput(PhidgetReader.digitalInputs[3]);
        BooleanStatus climbControl = new BooleanStatus(makeSolenoid(6));
        valve.addInput(climbControl);
        climbControl.setTrueWhen(Mixing.filterEvent(PhidgetReader.digitalInputs[1], true, duringTeleop));
        climbControl.setFalseWhen(Mixing.filterEvent(PhidgetReader.digitalInputs[3], true, duringTeleop));
    }

    private void createPressureMonitoring() {
        final FloatInputPoll pressure = Mixing.normalizeFloat(makeAnalogInput_ValueBased(1, 14), 100, 587);
        globalPeriodic.addListener(new EventConsumer() {
            public void eventFired() {
                PhidgetReader.phidgetLCD[0].println("Pressure: " + ((int) (pressure.readValue() * 100)) + "%\n");
            }
        });
        useCustomCompressor(Mixing.floatIsAtLeast(pressure, 1.0f), 1);
    }

    protected void createSimpleControl() {
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

/*
 * Files to port:
 * Arm: Mostly done, needs:
 *      Autonomous
 * [NOT USED] ArmPIDOutput
 * [NOT USED] ArmRegulator
 * [NOT SIGNIFICANT] Autotune
 * [NOT USED] BackupStick
 * [PORTED] ChickenRobot
 * Climber: Not done, needs:
 *      Climbing code
 * [AVAILABLE IN LIBRARY] Compressor1540
 * Config: Not really done, needs:
 *      Autonomous switches
 * Driver: Mostly done, needs:
 *      Autonomous
 * [NOT USED] HertzFinder
 * [NOT REALLY USED] KinectWrapper
 * [PORTED] NonSynchronousTicking
 * [PORTED] PController
 * [NOT USED] PIDController
 * [NOT SIGNIFICANT] Part
 * Shooter: Mostly done, needs:
 *      Autonomous
 *      Spinup Time
 *      Shooter LED
 *      Pushback
 *      Launch Sequencer
 *      Readout of launchability
 * [AVAILABLE IN LIBRARY] Utilities
 */

/*
 * Current bugs:
 * [LOW] Disconnection is not noticed promptly from poultry inspector.
 * [LOW] No reset of IO when phidget gets attached. (could not reproduce)
 * [VERY LOW] Input rate is not modified on phidget.
 * 
 * Needed features:
 *  Simple debugging methods.
 * 
 * Next in Inferno implementation:
 *  1. Climbing
 *  2. Autonomous
 */

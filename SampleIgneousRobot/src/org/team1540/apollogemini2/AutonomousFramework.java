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
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.holders.StringHolder;
import ccre.holders.TuningContext;
import ccre.igneous.Igneous;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModule;
import ccre.log.Logger;
import ccre.saver.StorageProvider;
import ccre.saver.StorageSegment;
import ccre.util.CArrayUtils;
import ccre.util.CList;

public abstract class AutonomousFramework extends InstinctModule {

    private static final StorageSegment tuningSegment = StorageProvider.openStorage("autonomous");
    protected static final TuningContext autonomousTuningContext = new TuningContext(tuningSegment).publishSavingEvent();

    private static final StringHolder option = new StringHolder("double");
    private static String[] options;
    private static CList<String> optionList;

    private static FloatOutput armCollectorMotor;
    private static BooleanOutput armFingerSolenoids;
    private static EventOutput armDown, armUp, armAlign;
    private static BooleanInput hotZoneActive;
    private static FloatOutput bothDriveMotors;
    private static final EventStatus shooterFire = new EventStatus(), shooterRearm = new EventStatus();
    private static final BooleanStatus waitingForRearm = new BooleanStatus();

    public static void addArmActuators(FloatOutput collector, BooleanOutput fingers) {
        armCollectorMotor = collector;
        armFingerSolenoids = fingers;
    }

    public static void addArmPositions(EventOutput down, EventOutput up, EventOutput align) {
        armDown = down;
        armUp = up;
        armAlign = align;
    }

    public static EventInput getWhenToFire() {
        return shooterFire;
    }

    public static EventInput getWhenToRearm() {
        return shooterRearm;
    }

    public static void notifyRearmFinished() {
        waitingForRearm.set(false);
    }

    public static void setHotZoneTrigger(BooleanInput hotZone) {
        hotZoneActive = hotZone;
    }

    public static void addDriveMotors(FloatOutput leftDrive, FloatOutput rightDrive) {
        bothDriveMotors = FloatMixing.combine(leftDrive, rightDrive);
    }

    public static void setup() {
        Igneous.registerAutonomous(new AutonomousModes());
        final EventOutput reportAutonomous = new EventOutput() {
            public void event() {
                Logger.info("Autonomous mode is currently set to: " + option.get());
            }
        };
        reportAutonomous.event();
        tuningSegment.attachStringHolder("autonomous-mode", option);
        Cluck.publish("Autonomous Mode Check", reportAutonomous);
        Cluck.publish("Autonomous Mode Next", new EventOutput() {
            public void event() {
                option.set(options[(optionList.indexOf(option.get()) + 1) % options.length]);
                reportAutonomous.event();
            }
        });
        Cluck.publish("Autonomous Mode Previous", new EventOutput() {
            public void event() {
                option.set(options[(optionList.indexOf(option.get()) - 1 + options.length) % options.length]);
                reportAutonomous.event();
            }
        });
    }

    @Override
    protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
        try {
            String opt = option.get();
            int id = getSelectedMode(opt);
            Logger.info("Dispatching to autonomous mode '" + opt + "' = " + id);
            runAutonomousMode(id, opt);
        } finally {
            bothDriveMotors.set(0);
        }
    }
    
    protected abstract void runAutonomousMode(int modeId, String modeName) throws InterruptedException, AutonomousModeOverException;
    
    protected int getSelectedMode(String opt) {
        for (int i = 0; i < options.length; i++) {
            String s = options[i];
            if (opt.equals(s)) {
                return i;
            }
        }
        Logger.severe("Nonexistent autonomous mode: " + opt);
        return 0;
    }
    
    protected AutonomousFramework(String... modes) {
        if (options != null) {
            throw new IllegalStateException("Cannot have more than one main autonomous class!");
        }
        options = modes;
        optionList = CArrayUtils.asList(modes);
    }
    
    // Methods to be used by the subclass.
    
    protected void drive(float speed) {
        bothDriveMotors.set(speed);
    }
    
    protected void drive(FloatInputPoll speed) {
        drive(speed.get());
    }
    
    protected void driveFor(FloatInputPoll speed, FloatInputPoll seconds) throws InterruptedException, AutonomousModeOverException {
        drive(speed);
        waitForTime(seconds);
        drive(0);
    }

    protected void startCollection(FloatInputPoll speed) {
        armCollectorMotor.set(speed.get());
        Logger.fine("Started collecting...");
    }

    protected void stopCollection() {
        armCollectorMotor.set(0);
        Logger.fine("Stopped collecting.");
    }
    
    protected void startRaisingArm() {
        armUp.event();
    }
    
    protected void raiseArm(FloatInputPoll seconds) throws InterruptedException, AutonomousModeOverException {
        startRaisingArm();
        waitForTime(seconds);
    }
    
    protected void startLoweringArm() {
        armDown.event();
    }
    
    protected void lowerArm(FloatInputPoll seconds) throws InterruptedException, AutonomousModeOverException {
        armDown.event();
        waitForTime(seconds);
    }
    
    protected void startAligningArm() {
        armAlign.event();
    }

    protected void alignArm(FloatInputPoll seconds) throws InterruptedException, AutonomousModeOverException {
        armAlign.event();
        waitForTime(seconds);
    }
    
    protected void openFingers() {
        armFingerSolenoids.set(true);
    }
    
    protected void closeFingers() {
        armFingerSolenoids.set(false);
    }

    protected BooleanInputPoll getHotZoneActive() {
        return hotZoneActive;
    }
    
    protected void fireShooter() {
        shooterFire.produce();
        Logger.fine("Fired shooter.");
    }
    
    protected void startRearming() {
        waitingForRearm.set(true);
        shooterRearm.produce();
        Logger.fine("Started rearming.");
    }
    
    protected void waitUntilRearmed() throws AutonomousModeOverException, InterruptedException {
        waitUntil(BooleanMixing.invert((BooleanInputPoll) waitingForRearm)); 
    }
}

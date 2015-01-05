/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 * 
 * This file is part of the ApolloGemini2014 project.
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
package org.team1540.apollogemini;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.cluck.rpc.RemoteProcedure;
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
import ccre.util.Utils;

public class AutonomousController extends InstinctModule {

    private final StorageSegment seg = StorageProvider.openStorage("autonomous");
    private final TuningContext tune = new TuningContext(Cluck.getNode(), seg).publishSavingEvent("Autonomous");
    // Provided channels
    private FloatOutput bothDrive, collect;
    private BooleanOutput collectSols, useCurrent;
    private BooleanInputPoll kinectTrigger;
    private EventOutput lowerArm, raiseArm, alignArm;
    private final EventStatus fireWhenEvent = new EventStatus(),
            rearmWhenEvent = new EventStatus();
    // Tuned constants are below near the autonomous modes.
    private final StringHolder option = new StringHolder("double");
    private final String[] options = { "none", "forward", "hotcheck", "double" };
    private final CList<String> optionList = CArrayUtils.asList(options);
    private final BooleanStatus winchGotten = new BooleanStatus();

    protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
        try {
            String cur = option.get();
            if ("none".equals(cur)) {
                autoNone();
            } else if ("forward".equals(cur)) {
                autoForward();
            } else if ("hotcheck".equals(cur)) {
                autoHotcheck();
            } else if ("double".equals(cur)) {
                autoDouble();
            } else {
                Logger.severe("Nonexistent autonomous mode: " + option.get());
            }
        } finally {
            bothDrive.set(0);
        }
    }

    // *** Modes ***
    private void autoNone() throws AutonomousModeOverException, InterruptedException {
        bothDrive.set(0);
    }

    private final FloatStatus forwardMovement = tune.getFloat("autom-forward-speed", -1f);
    private final FloatStatus forwardDelay = tune.getFloat("autom-forward-delay", 0.5f);

    private void autoForward() throws AutonomousModeOverException, InterruptedException {
        bothDrive.set(forwardMovement.get());
        waitForTime(forwardDelay);
        bothDrive.set(0);
    }

    private final FloatStatus hotcheckAlignDistance = tune.getFloat("autom-hotcheck-align-distance", 0.3f);
    private final FloatStatus hotcheckAlignSpeed = tune.getFloat("autom-hotcheck-align-speed", -1f);
    private final FloatStatus hotcheckTimeoutAfter = tune.getFloat("autom-hotcheck-timeout", 4);
    private final FloatStatus hotcheckPostFirePause = tune.getFloat("autom-hotcheck-postfire-pause", 0.5f);
    private final FloatStatus hotcheckPostFireSpeed = tune.getFloat("autom-hotcheck-postfire-speed", -0.7f);
    private final FloatStatus hotcheckPostFireMove = tune.getFloat("autom-hotcheck-postfire-move", 0);
    private final FloatStatus hotcheckCollectorSpeed = tune.getFloat("autom-hotcheck-collector", 0.5f);
    private final FloatStatus hotcheckPreFirePause = tune.getFloat("autom-hotcheck-prefire-pause", 1);
    private final FloatStatus hotcheckArmMoveTime = tune.getFloat("autom-hotcheck-armmove-time", .6f);

    private void autoHotcheck() throws AutonomousModeOverException, InterruptedException {
        FloatInputPoll currentTime = Utils.currentTimeSeconds;
        Logger.fine("Began Hotcheck");
        bothDrive.set(hotcheckAlignSpeed.get());
        waitForTime(hotcheckAlignDistance);
        bothDrive.set(0);
        if (hotcheckArmMoveTime.get() > 0.02f) {
            collect.set(hotcheckCollectorSpeed.get());
            raiseArm.event();
            waitForTime(hotcheckArmMoveTime);
            Logger.fine("Up");
            lowerArm.event();
            waitForTime(hotcheckArmMoveTime);
            Logger.fine("Down");
            collect.set(0);
        } else {
            Logger.fine("Skip Arm");
        }
        Logger.fine("Arrived");
        alignArm.event();
        float timeoutTime = currentTime.get() + hotcheckTimeoutAfter.get();
        BooleanInputPoll timedout = FloatMixing.floatIsAtLeast(currentTime, timeoutTime);
        if (waitUntilOneOf(new BooleanInputPoll[] { kinectTrigger, timedout }) != 0) {
            Logger.warning("Cancelled HotZone wait after " + hotcheckTimeoutAfter.get() + " secs: " + currentTime.get() + "," + timeoutTime);
        }
        Logger.fine("Found hotzone");
        waitForTime(hotcheckPreFirePause);
        fireWhenEvent.produce();
        Logger.fine("Fired.");
        waitForTime(hotcheckPostFirePause);
        Logger.fine("Firing delay over..");
        bothDrive.set(hotcheckPostFireSpeed.get());
        waitForTime(hotcheckPostFireMove);
        bothDrive.set(0);
        Logger.fine("Hotcheck done!");
    }

    private final FloatStatus doubleArmMoveTime = tune.getFloat("autom-double-armmove-time", 0.9f);
    private final FloatStatus doubleFireTime = tune.getFloat("autom-double-fire-time", 0.7f);
    private final FloatStatus doubleCollectTime = tune.getFloat("autom-double-collect-time", 0.9f);
    private final FloatStatus doubleAlignTime1 = tune.getFloat("autom-double-align1-time", 0.4f);
    private final FloatStatus doubleAlignTime2 = tune.getFloat("autom-double-align2-time", 0.5f);

    private void autoDouble() throws InterruptedException, AutonomousModeOverException {
        Logger.fine("Began double mode!");
        useCurrent.set(true);
        winchGotten.set(false);
        rearmWhenEvent.produce();
        if (doubleAlignTime1.get() > 0.02) {
            bothDrive.set(-1);
            Logger.fine("Aligning...");
            waitForTime(doubleAlignTime1);
            Logger.fine("Aligned.");
            bothDrive.set(0);
        }
        Logger.info("Collect On");
        collect.set(1f);
        lowerArm.event();
        waitForTime((long) (1000L * doubleArmMoveTime.get() + 0.5f) / 2);
        Logger.info("Collect Off");
        collect.set(0f);
        waitUntil(winchGotten);
        waitForTime((long) (1000L * doubleArmMoveTime.get() + 0.5f) / 2);
        useCurrent.set(false);
        Logger.fine("Arm moved - firing!");
        fireWhenEvent.produce();
        waitForTime(doubleFireTime);
        winchGotten.set(false);
        lowerArm.event();
        rearmWhenEvent.produce();
        collectSols.set(true);
        Logger.fine("Rearming... (and driving)");
        bothDrive.set(1f);
        collect.set(0.5f);
        waitForTime(doubleAlignTime2);
        Logger.fine("Drove!");
        bothDrive.set(0f);
        waitUntil(winchGotten);
        collect.set(0f);
        Logger.fine("Rearmed!");
        collect.set(1f);
        waitForTime(doubleCollectTime);
        lowerArm.event();
        collectSols.set(false);
        collect.set(0);
        Logger.fine("Collected.");
        if (doubleAlignTime2.get() > 0.02) {
            bothDrive.set(-1);
            Logger.fine("Aligning...");
            waitForTime(doubleAlignTime2);
            Logger.fine("Aligned.");
            bothDrive.set(0);
        }
        waitForTime(doubleArmMoveTime);
        Logger.fine("Firing...");
        fireWhenEvent.produce();
        waitForTime(doubleFireTime);
        raiseArm.event();
        Logger.fine("Double completed.");
    }

    // *** Framework ***

    public AutonomousController() {
        final EventOutput reportAutonomous = new EventOutput() {
            public void event() {
                Logger.info("Autonomous mode is currently set to: " + option.get());
            }
        };
        reportAutonomous.event();
        seg.attachStringHolder("autonomous-mode", option);
        Cluck.publish("autom-check", reportAutonomous);
        Cluck.publish("autom-next", new EventOutput() {
            public void event() {
                option.set(options[(optionList.indexOf(option.get()) + 1) % options.length]);
                reportAutonomous.event();
            }
        });
        Cluck.publish("autom-prev", new EventOutput() {
            public void event() {
                option.set(options[(optionList.indexOf(option.get()) - 1 + options.length) % options.length]);
                reportAutonomous.event();
            }
        });
        final RemoteProcedure openDialog = Cluck.getNode().getRPCManager().subscribe("phidget/display-dialog", 11000);
        Cluck.publish("autom-select", new EventOutput() {
            public void event() {
                StringBuffer sb = new StringBuffer("TITLE Select Autonomous Mode\n");
                for (int i = 0; i < options.length; i++) {
                    sb.append("BUTTON ").append(options[i]).append('\n');
                }
                openDialog.invoke(sb.toString().getBytes(), new ByteArrayOutputStream() {
                    public void close() throws UnsupportedEncodingException {
                        String str = new String(this.toByteArray());
                        if (str.length() > 0 && optionList.indexOf(str) != -1) {
                            option.set(str);
                            reportAutonomous.event();
                        }
                    }
                });
            }
        });
        Igneous.registerAutonomous(this);
    }

    public void putDriveMotors(FloatOutput leftDrive, FloatOutput rightDrive) {
        bothDrive = FloatMixing.combine(leftDrive, rightDrive);
    }

    public void putKinectTrigger(BooleanInputPoll kinectTrigger) {
        this.kinectTrigger = kinectTrigger;
    }

    public EventInput getWhenToFire() {
        return fireWhenEvent;
    }

    public EventInput getWhenToRearm() {
        return rearmWhenEvent;
    }

    public void putArm(EventOutput lowerArm, EventOutput raiseArm, EventOutput alignArm, FloatOutput collector, BooleanOutput collectorSolenoids) {
        this.lowerArm = lowerArm;
        this.raiseArm = raiseArm;
        this.alignArm = alignArm;
        collect = collector;
        collectSols = collectorSolenoids;
    }

    public EventOutput getNotifyRearmFinished() {
        return winchGotten.getSetTrueEvent();
    }

    public void putCurrentActivator(BooleanStatus shouldUseCurrent) {
        useCurrent = shouldUseCurrent;
    }
}

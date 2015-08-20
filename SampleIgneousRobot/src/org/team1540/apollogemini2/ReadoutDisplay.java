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

import java.io.PrintStream;

import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Ticker;
import ccre.igneous.Igneous;
import ccre.log.Logger;
import ccre.phidget.PhidgetReader;

public class ReadoutDisplay {

    private static String activeMessage = null;
    private static int activePriority = -1;
    private static int timeRemaining = -1;
    private static int defaultTimeRemaining = 10000;
    private static final PrintStream line = PhidgetReader.getLCDLine(0);
    private static String defaultString;

    static {
        setModeString("....");
        Igneous.startDisabled.send(new EventOutput() {
            public void event() {
                setModeString(Igneous.isRoboRIO() ? "____" : "1540");
            }
        });
        Igneous.startTele.send(new EventOutput() {
            public void event() {
                setModeString("TELE");
            }
        });
        Igneous.startAuto.send(new EventOutput() {
            public void event() {
                setModeString("AUTO");
            }
        });
        Igneous.startTest.send(new EventOutput() {
            public void event() {
                setModeString("TEST");
            }
        });
    }

    private static void setModeString(String str) {
        defaultString = Igneous.isRoboRIO() ? "[" + str + "] GEMINI [" + str + "]" : "(" + str + ") APOLLO (" + str + ")";
    }

    public static void setupErrors() {
        Igneous.constantPeriodic.send(new EventOutput() {
            public void event() {
                if (activeMessage != null) {
                    timeRemaining -= 10;
                    if (timeRemaining <= 0) {
                        activeMessage = null;
                        activePriority = -1;
                        timeRemaining = -1;
                        line.println(defaultString);
                    }
                }
                defaultTimeRemaining -= 10;
                if (defaultTimeRemaining <= 0 && activeMessage == null) {
                    line.println(defaultString);
                    defaultTimeRemaining = 10000;
                }
            }
        });
    }

    public static void displayAndLogError(int priority, String message, int timeoutMillis) {
        Logger.info("[DISPLAY] " + message);
        displayError(priority, message, timeoutMillis);
    }

    public static void displayError(int priority, String message, int timeoutMillis) {
        if (message == null) {
            message = "[NULL: SOFTWARE BUG]";
        } else if (message.length() < 20) {
            int missing = 20 - message.length();
            int init = missing / 2;
            message = ("......    " + message + "    ......").substring(10 - init, 30 - init);
        } else if (message.length() > 20) {
            message = message.substring(0, 20);
        }
        if (message.equals(activeMessage)) {
            if (timeoutMillis > timeRemaining) {
                timeRemaining = timeoutMillis;
            }
        } else if (activeMessage == null || priority > activePriority) {
            activeMessage = message;
            activePriority = priority;
            timeRemaining = timeoutMillis;
            line.println(message);
        }
    }

    private static FloatInput percentPressure = FloatMixing.always(-10);
    private static BooleanInput pressureSwitch = BooleanInput.alwaysFalse;
    private static FloatInput winchJoules = FloatMixing.always(-42);

    private static final EventOutput update = new EventOutput() {
        public void event() {
            Integer pressure = Math.round(percentPressure.get());
            boolean atPressure = pressureSwitch.get();
            String pressureMessage = pressure <= -10 ? "????" : Integer.toString(pressure) + "%";
            while (pressureMessage.length() < 4) {
                pressureMessage = " " + pressureMessage;
            }
            pressureMessage = "AIR " + (atPressure ? "<" : " ") + pressureMessage + (atPressure ? ">" : " ");
            PhidgetReader.getLCDLine(1).println(pressureMessage + " WNCH " + Float.toString(winchJoules.get()));
        }
    };

    public static void setupReadout() {
        new Ticker(2000).send(update);
    }

    public static void showPressure(FloatInput percent, BooleanInput fullPressure) {
        ReadoutDisplay.percentPressure = percent;
        ReadoutDisplay.pressureSwitch = fullPressure;
        fullPressure.onUpdate(update);
        FloatMixing.whenFloatChanges(percent, 0.5f).send(update);
    }

    public static void showWinchStatus(FloatInput joules) {
        winchJoules = joules;
        FloatMixing.whenFloatChanges(joules, 0.5f).send(update);
    }
}

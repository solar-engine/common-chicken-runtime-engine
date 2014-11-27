package org.team1540.apollogemini2;

import ccre.channel.*;
import ccre.igneous.Igneous;
import ccre.log.Logger;
import ccre.phidget.PhidgetReader;

import java.io.PrintStream;

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

    public static void showPressure(FloatInputPoll percentPressure, BooleanInputPoll pressureSwitch) {
        // TODO Auto-generated method stub
        
    }

    public static void showWinchStatus(FloatInput wattageaccumulator) {
        // TODO Auto-generated method stub
        
    }
}

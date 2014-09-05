package org.team1540.geminiapollo;

import ccre.channel.*;
import ccre.phidget.PhidgetReader;
import java.io.PrintStream;

public class ErrorMessages {

    private static String activeMessage = null;
    private static int activePriority = -1;
    private static int timeRemaining = -1;
    private static int defaultTimeRemaining = 10000;
    private static final PrintStream line = PhidgetReader.getLCDLine(0);

    public static void setupError(EventInput constant) {
        final String defaultStr = RobotMain.IS_COMPETITION_ROBOT ? "(1540) APOLLO (1540)" : "[____] GEMINI [____]";
        constant.send(new EventOutput() {
            public void event() {
                if (activeMessage != null) {
                    timeRemaining -= 10;
                    if (timeRemaining <= 0) {
                        activeMessage = null;
                        activePriority = -1;
                        timeRemaining = -1;
                        line.println(defaultStr);
                    }
                }
                defaultTimeRemaining -= 10;
                if (defaultTimeRemaining <= 0 && activeMessage == null) {
                    line.println(defaultStr);
                    defaultTimeRemaining = 10000;
                }
            }
        });
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
}

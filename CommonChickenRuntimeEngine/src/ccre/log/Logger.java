/*
 * Copyright 2013-2016 Cel Skeggs
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
package ccre.log;

import java.util.concurrent.CopyOnWriteArrayList;

import ccre.util.CallerInfo;
import ccre.util.Utils;

/**
 * A class containing easy global methods for logging, as well as holding the
 * default logger field.
 *
 * @author skeggsc
 */
public class Logger {

    /**
     * The logging targets to write logs to.
     */
    public static final CopyOnWriteArrayList<LoggingTarget> targets = new CopyOnWriteArrayList<LoggingTarget>();
    private static boolean includeLineNumbers = true;

    /**
     * Set whether or not filenames and line numbers should be prefixed to
     * logging messages, when available.
     *
     * @param shouldInclude if this debugging info should be included.
     */
    public static void setShouldIncludeLineNumbers(boolean shouldInclude) {
        includeLineNumbers = shouldInclude;
    }

    /**
     * Get whether or not filenames and line numbers are prefixed to logging
     * messages, when available.
     *
     * @return shouldInclude if this debugging info is included.
     */
    public static boolean getShouldIncludeLineNumbers() {
        return includeLineNumbers;
    }

    static {
        targets.add(new PrintStreamLogger(System.err));
    }

    /**
     * Add the specified target to the list of targets.
     *
     * @param lt The target to add.
     */
    public static synchronized void addTarget(LoggingTarget lt) {
        targets.add(lt);
    }

    /**
     * Remove the specified target from the list of targets.
     *
     * @param lt The target to remove.
     */
    public static synchronized void removeTarget(LoggingTarget lt) {
        targets.remove(lt);
    }

    /**
     * Log a given message and throwable at the given log level.
     *
     * @param level the level to log at.
     * @param message the message to log.
     * @param thr the Throwable to log
     */
    public static void log(LogLevel level, String message, Throwable thr) {
        logInternal(level, message, thr);
    }

    private static void logInternal(LogLevel level, String message, Throwable thr) {
        if (level == null || message == null) {
            throw new NullPointerException();
        }
        message = prependCallerInfo(3, message);
        for (LoggingTarget lt : targets) {
            lt.log(level, message, thr);
        }
    }

    /**
     * Log a given message and extended message at the given log level.
     *
     * @param level the level to log at.
     * @param message the message to log.
     * @param extended the extended message to log
     */
    public static void logExt(LogLevel level, String message, String extended) {
        if (level == null || message == null) {
            throw new NullPointerException();
        }
        message = prependCallerInfo(1, message);
        for (LoggingTarget lt : targets) {
            lt.log(level, message, extended);
        }
    }

    private static String prependCallerInfo(int index, String message) {
        if (includeLineNumbers && !message.startsWith("(") && !message.startsWith("[")) {
            CallerInfo caller = Utils.getMethodCaller(index + 1);
            if (caller != null && caller.getFileName() != null) {
                if (caller.getLineNum() > 0) {
                    return "(" + caller.getFileName() + ":" + caller.getLineNum() + ") " + message;
                } else {
                    return "(" + caller.getFileName() + ") " + message;
                }
            }
        }
        return message;
    }

    /**
     * Log a given message at the given log level.
     *
     * @param level the level to log at.
     * @param message the message to log.
     */
    public static void log(LogLevel level, String message) {
        logInternal(level, message, null);
    }

    /**
     * Log the given message at SEVERE level.
     *
     * @param message the message to log.
     */
    public static void severe(String message) {
        log(LogLevel.SEVERE, message);
    }

    /**
     * Log the given message at WARNING level.
     *
     * @param message the message to log.
     */
    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    /**
     * Log the given message at INFO level.
     *
     * @param message the message to log.
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Log the given message at CONFIG level.
     *
     * @param message the message to log.
     */
    public static void config(String message) {
        log(LogLevel.CONFIG, message);
    }

    /**
     * Log the given message at FINE level.
     *
     * @param message the message to log.
     */
    public static void fine(String message) {
        log(LogLevel.FINE, message);
    }

    /**
     * Log the given message at FINER level.
     *
     * @param message the message to log.
     */
    public static void finer(String message) {
        log(LogLevel.FINER, message);
    }

    /**
     * Log the given message at FINEST level.
     *
     * @param message the message to log.
     */
    public static void finest(String message) {
        log(LogLevel.FINEST, message);
    }

    /**
     * Log the given message and exception at SEVERE level.
     *
     * @param message the message to log.
     * @param thr The exception to include in the log.
     */
    public static void severe(String message, Throwable thr) {
        logInternal(LogLevel.SEVERE, message, thr);
    }

    /**
     * Log the given message and exception at WARNING level.
     *
     * @param message the message to log.
     * @param thr The exception to include in the log.
     */
    public static void warning(String message, Throwable thr) {
        logInternal(LogLevel.WARNING, message, thr);
    }

    /**
     * Log the given message and exception at INFO level.
     *
     * @param message the message to log.
     * @param thr The exception to include in the log.
     */
    public static void info(String message, Throwable thr) {
        logInternal(LogLevel.INFO, message, thr);
    }

    /**
     * Log the given message and exception at CONFIG level.
     *
     * @param message the message to log.
     * @param thr The exception to include in the log.
     */
    public static void config(String message, Throwable thr) {
        logInternal(LogLevel.CONFIG, message, thr);
    }

    /**
     * Log the given message and exception at FINE level.
     *
     * @param message the message to log.
     * @param thr The exception to include in the log.
     */
    public static void fine(String message, Throwable thr) {
        logInternal(LogLevel.FINE, message, thr);
    }

    /**
     * Log the given message and exception at FINER level.
     *
     * @param message the message to log.
     * @param thr The exception to include in the log.
     */
    public static void finer(String message, Throwable thr) {
        logInternal(LogLevel.FINER, message, thr);
    }

    /**
     * Log the given message and exception at FINEST level.
     *
     * @param message the message to log.
     * @param thr The exception to include in the log.
     */
    public static void finest(String message, Throwable thr) {
        logInternal(LogLevel.FINEST, message, thr);
    }

    private Logger() {
    }
}

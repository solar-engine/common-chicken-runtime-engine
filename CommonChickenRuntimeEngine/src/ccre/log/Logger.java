/*
 * Copyright 2013 Colby Skeggs
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

/**
 * A class containing easy global methods for logging, as well as holding the
 * default logger field.
 *
 * @author skeggsc
 */
public class Logger {

    /**
     * The logging target to write logs to by default.
     */
    public static LoggingTarget target = new StandardStreamLogger();
    /**
     * The minimum level of logging to keep when writing data using these
     * globals. Anything below this level will be ignored.
     */
    public static LogLevel minimumLevel = LogLevel.FINEST;

    /**
     * Log a given message and throwable at the given log level.
     *
     * @param level the level to log at.
     * @param message the message to log.
     * @param thr the Throwable to log
     */
    public static void log(LogLevel level, String message, Throwable thr) {
        if (level.atLeastAsImportant(minimumLevel)) {
            target.log(level, message, thr);
        }
    }

    /**
     * Log a given message at the given log level.
     *
     * @param level the level to log at.
     * @param message the message to log.
     */
    public static void log(LogLevel level, String message) {
        log(level, message, null);
    }

    /**
     * Log the given message at SEVERE level.
     * @param message the message to log.
     */
    public static void severe(String message) {
        log(LogLevel.SEVERE, message);
    }

    /**
     * Log the given message at WARNING level.
     * @param message the message to log.
     */
    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    /**
     * Log the given message at INFO level.
     * @param message the message to log.
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Log the given message at CONFIG level.
     * @param message the message to log.
     */
    public static void config(String message) {
        log(LogLevel.CONFIG, message);
    }

    /**
     * Log the given message at FINE level.
     * @param message the message to log.
     */
    public static void fine(String message) {
        log(LogLevel.FINE, message);
    }

    /**
     * Log the given message at FINER level.
     * @param message the message to log.
     */
    public static void finer(String message) {
        log(LogLevel.FINER, message);
    }

    /**
     * Log the given message at FINEST level.
     * @param message the message to log.
     */
    public static void finest(String message) {
        log(LogLevel.FINEST, message);
    }
}

/*
 * Copyright 2013-2014 Colby Skeggs
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
package ccre.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.log.CCRELogger;
import ccre.log.LogLevel;
import ccre.log.NetworkAutologger;

/**
 * An event that logs a string at a LogLevel when fired.
 *
 * @author skeggsc
 */
public final class EventLogger implements EventOutput {
    private static Logger logger = LoggerFactory.getLogger(EventLogger.class);

    /**
     * When the specified event is fired, log the specified message at the
     * specified logging level.
     *
     * @param when when to log.
     * @param level what level to log at.
     * @param message what message to log.
     */
    public static void log(EventInput when, LogLevel level, String message) {
        when.send(new EventLogger(level, message));
    }

    /**
     * The logging level at which to log the message.
     */
    private final LogLevel level;
    /**
     * The message to log.
     */
    private final String message;

    /**
     * When this event is fired, log the specified message at the specified
     * logging level.
     *
     * @param level the level to log at.
     * @param message the message to log.
     */
    public EventLogger(LogLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public void event() {
        CCRELogger.logByLevel(logger, level, message);
    }
}

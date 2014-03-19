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
 * A logging target that sends all logged messages to a set of specified
 * loggers. This is useful, for example, if you want to log across the network
 * but don't want to stop logging to standard error.
 *
 * @author skeggsc
 */
public class MultiTargetLogger implements LoggingTarget {

    /**
     * Create a new MultiTargetLogger. It will log to a specified list of
     * targets.
     *
     * @param targets the targets to log to.
     */
    public MultiTargetLogger(LoggingTarget... targets) {
        this.targets = targets;
    }
    /**
     * The list of targets that should receive logging.
     */
    private LoggingTarget[] targets;

    public void log(LogLevel level, String message, Throwable thr) {
        for (LoggingTarget t : targets) {
            t.log(level, message, thr);
        }
    }

    public void log(LogLevel level, String message, String extended) {
        for (LoggingTarget t : targets) {
            t.log(level, message, extended);
        }
    }
}

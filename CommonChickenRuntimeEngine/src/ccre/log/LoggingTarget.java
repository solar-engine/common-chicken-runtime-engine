/*
 * Copyright 2013,2015-2016 Colby Skeggs
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
 * A target that can receive logging messages.
 *
 * @author skeggsc
 */
public interface LoggingTarget {

    /**
     * A LoggingTarget that ignores all messages completely.
     */
    public static final LoggingTarget ignored = new LoggingTarget() {
        @Override
        public void log(LogLevel level, String message, Throwable throwable) {
            if (level == null || message == null) {
                throw new NullPointerException();
            }
            // nothing to do
        }

        @Override
        public void log(LogLevel level, String message, String extended) {
            if (level == null || message == null) {
                throw new NullPointerException();
            }
            // nothing to do
        }
    };

    /**
     * Log the given message at the given level with an optional throwable (can
     * be null).
     *
     * @param level the level to log at.
     * @param message the message to log.
     * @param throwable the optional throwable to log.
     */
    public void log(LogLevel level, String message, Throwable throwable);

    /**
     * Log the given message at the given level with an optional extended
     * string. (usually more details, such as a throwable traceback or
     * instructions on how to fix the error)
     *
     * @param level the level to log to.
     * @param message the message to log.
     * @param extended the optional extended message to log.
     */
    public void log(LogLevel level, String message, String extended);
}

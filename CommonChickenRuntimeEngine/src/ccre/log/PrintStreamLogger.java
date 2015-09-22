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
package ccre.log;

import java.io.PrintStream;

/**
 * A logging target that will write all messages to the specified PrintStream.
 *
 * @author skeggsc
 */
public final class PrintStreamLogger implements LoggingTarget {

    /**
     * The PrintStream to write the logs to.
     */
    private final PrintStream str;

    /**
     * Create a new PrintStreamLogger to log to the specific output.
     *
     * @param out the PrintStream to log to.
     */
    public PrintStreamLogger(PrintStream out) {
        if (out == null) {
            throw new NullPointerException();
        }
        this.str = out;
    }

    @Override
    public synchronized void log(LogLevel level, String message, Throwable thr) {
        if (thr != null) {
            str.println("[" + level.message + "] " + message);
            thr.printStackTrace(str);
        } else {
            str.println("[" + level.message + "] " + message);
        }
    }

    @Override
    public void log(LogLevel level, String message, String extended) {
        str.println("[" + level.message + "] " + message);
        if (extended != null && !extended.isEmpty()) {
            str.println(extended);
        }
    }
}

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

import ccre.workarounds.ThrowablePrinter;

/**
 * A logging target that will write all messages to the standard error. This is
 * the default logger.
 *
 * @author skeggsc
 */
final class StandardStreamLogger implements LoggingTarget {

    public synchronized void log(LogLevel level, String message, Throwable thr) {
        if (thr != null) {
            System.err.println("LOG{" + level.abbreviation + "} " + message);
            ThrowablePrinter.printThrowable(thr, System.err);
            //thr.printStackTrace();
        } else {
            System.err.println("LOG[" + level.abbreviation + "] " + message);
        }
    }

    public synchronized void log(LogLevel level, String message, String extended) {
        System.err.println("LOG[" + level.abbreviation + "] " + message);
        if (extended != null && extended.length() != 0) {
            System.err.println(extended);
        }
    }
}

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

import ccre.cluck.CluckGlobals;
import ccre.cluck.CluckNode;
import ccre.channel.EventOutput;
import ccre.workarounds.ThrowablePrinter;

/**
 * A logging tool that holds the first twenty log messages so that they can be
 * viewed over the network.
 *
 * @author skeggsc
 */
public class BootLogger implements LoggingTarget {

    /**
     * The number of log messages to save.
     */
    private static final int LOG_MESSAGE_COUNT = 20;
    
    /**
     * Register a new BootLogger with the logging manager.
     */
    public static void register() {
        Logger.addTarget(new BootLogger());
    }

    /**
     * The list of twenty lines logged near startup.
     */
    private final String[] outs = new String[LOG_MESSAGE_COUNT];
    /**
     * The next index in the output array to write.
     */
    private volatile int outId = 0;

    /**
     * Create a new BootLogger that publishes it over the CluckGlobal node.
     */
    public BootLogger() {
        this(CluckGlobals.getNode());
    }

    /**
     * Create a new BootLogger that publishes it over the specified node.
     *
     * @param node The CluckNode to publish over.
     */
    public BootLogger(CluckNode node) {
        final Object[] localOuts = outs;
        node.publish("post-bootlogs", new EventOutput() {
            public void event() {
                synchronized (BootLogger.this) {
                    Logger.log(LogLevel.INFO, "[BOOT-START]");
                    for (int i = 0; i < LOG_MESSAGE_COUNT; i++) {
                        if (localOuts[i] != null) {
                            Logger.log(LogLevel.INFO, "[BOOT-" + i + "] " + localOuts[i]);
                        }
                    }
                    Logger.log(LogLevel.INFO, "[BOOT-END]");
                }
            }
        });
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if (outId >= LOG_MESSAGE_COUNT) {
            return;
        }
        log(level, message, ThrowablePrinter.toStringThrowable(throwable));
    }

    public void log(LogLevel level, String message, String extended) {
        if (outId >= LOG_MESSAGE_COUNT) {
            return;
        }
        if (message.startsWith("[BOOT-")) {
            return;
        }
        synchronized (this) {
            if (outId >= LOG_MESSAGE_COUNT) {
                return;
            }
            if (extended == null) {
                outs[outId++] = level.toString() + ": " + message;
            } else {
                outs[outId++] = level.toString() + ": " + message + ": " + extended;
            }
        }
    }
}

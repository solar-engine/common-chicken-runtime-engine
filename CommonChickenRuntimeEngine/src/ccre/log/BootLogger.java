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

import ccre.cluck.CluckGlobals;
import ccre.cluck.CluckNode;
import ccre.event.EventConsumer;
import ccre.workarounds.ThrowablePrinter;

/**
 * A logging tool that holds the first twenty log messages so that they can be
 * viewed over the network.
 *
 * @author skeggsc
 */
public class BootLogger implements LoggingTarget {

    public static void register() {
        Logger.addTarget(new BootLogger());
    }
    
    public String[] outs = new String[20];
    public int outId = 0;

    public BootLogger() {
        this(CluckGlobals.node);
    }

    public BootLogger(CluckNode node) {
        node.publish("post-bootlogs", new EventConsumer() {
            public void eventFired() {
                Logger.log(LogLevel.INFO, "[BOOT-START]");
                for (int i=0;i<outs.length; i++) {
                    if (outs[i] != null) {
                        Logger.log(LogLevel.INFO, "[BOOT-" + i + "] " + outs[i]);
                    }
                }
                Logger.log(LogLevel.INFO, "[BOOT-END]");
            }
        });
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if (outId >= outs.length) {
            return;
        }
        log(level, message, ThrowablePrinter.toStringThrowable(throwable));
    }

    public void log(LogLevel level, String message, String extended) {
        if (outId >= outs.length) {
            return;
        }
        if (message.startsWith("[BOOT-")) {
            return;
        }
        synchronized (this) {
            if (outId >= outs.length) {
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

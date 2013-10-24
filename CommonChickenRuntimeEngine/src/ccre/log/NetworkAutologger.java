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
import ccre.cluck.CluckSubscriber;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.util.CHashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A logging tool that shares all logging between networked cluck systems
 * automatically.
 *
 * @author skeggsc
 */
public class NetworkAutologger implements LoggingTarget {

    private static boolean registered = false;

    public static void register() {
        if (registered) {
            Logger.warning("Network autologger registered twice!");
            return;
        }
        registered = true;
        Logger.target = new MultiTargetLogger(Logger.target, new NetworkAutologger(CluckGlobals.node));
    }
    private String[] remotes;
    private final CHashMap<String, LoggingTarget> targetCache = new CHashMap<String, LoggingTarget>();

    public NetworkAutologger(final CluckNode node) {
        final CollapsingWorkerThread autologger = new CollapsingWorkerThread("network-autologger") {
            @Override
            protected void doWork() throws Throwable {
                remotes = node.searchRemotes((int) CluckNode.RMT_LOGTARGET, 500);
                for (String s : remotes) {
                    if (s.contains("auto-") && targetCache.get(s) == null) {
                        targetCache.put(s, node.subscribeLT(s, LogLevel.FINEST));
                    }
                }
            }
        };
        autologger.start();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                autologger.trigger();
            }
        }, 10, 10000);
        String here = Integer.toHexString(hashCode()) + "-" + Integer.toHexString((int) System.currentTimeMillis());
        new CluckSubscriber() {
            @Override
            protected void receive(String source, byte[] data) {
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    autologger.trigger();
                }
            }
        }.attach(node, "netwatch-" + here);
        node.publish("auto-" + here, new LoggingTarget() {
            public void log(LogLevel level, String message, Throwable throwable) {
                Logger.log(level, "[NET] " + message, throwable);
            }

            public void log(LogLevel level, String message, String extended) {
                Logger.logExt(level, "[NET] " + message, extended);
            }
        });
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if (message.startsWith("[NET] ")) { // From the network, so don't broadcast.
            return;
        }
        String[] l = remotes;
        for (String cur : l) {
            LoggingTarget lt = targetCache.get(cur);
            if (lt == null) {
                continue;
            }
            lt.log(level, message, throwable);
        }
    }

    public void log(LogLevel level, String message, String extended) {
        if (message.startsWith("[NET] ")) { // From the network, so don't broadcast.
            return;
        }
        String[] l = remotes;
        for (String cur : l) {
            LoggingTarget lt = targetCache.get(cur);
            if (lt == null) {
                continue;
            }
            lt.log(level, message, extended);
        }
    }
}

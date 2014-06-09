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

import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.cluck.CluckRemoteListener;
import ccre.cluck.CluckSubscriber;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.concurrency.ConcurrentDispatchArray;
import ccre.util.CHashMap;
import ccre.util.UniqueIds;

/**
 * A logging tool that shares all logging between networked cluck systems
 * automatically.
 *
 * @author skeggsc
 */
public final class NetworkAutologger implements LoggingTarget, CluckRemoteListener {

    /**
     * Whether or not a NetworkAutologger has been registered globally.
     */
    private static volatile boolean registered = false;
    private static final LoggingTarget localLoggingTarget = new LoggingTarget() {
        public void log(LogLevel level, String message, Throwable throwable) {
            Logger.log(level, "[NET] " + message, throwable);
        }

        public void log(LogLevel level, String message, String extended) {
            Logger.logExt(level, "[NET] " + message, extended);
        }
    };

    /**
     * Register a new global NetworkAutologger with the logging manager. This
     * only occurs once - after that an warning will be logged again.
     */
    public static void register() {
        if (registered) {
            Logger.warning("Network autologger registered twice!");
            return;
        }
        registered = true;
        NetworkAutologger nlog = new NetworkAutologger(Cluck.getNode());
        Logger.addTarget(nlog);
        nlog.start();
    }
    /**
     * The current list of remotes to send logging messages to.
     */
    private final ConcurrentDispatchArray<String> remotes = new ConcurrentDispatchArray<String>();
    /**
     * The current cache of subscribed LoggingTargets to send logging messages
     * to.
     */
    private final CHashMap<String, LoggingTarget> targetCache = new CHashMap<String, LoggingTarget>();
    private final CluckNode node;
    private final String localpath, hereID;

    /**
     * Create a new NetworkAutologger hooked up to the specified node.
     *
     * @param node The node to attach to.
     */
    public NetworkAutologger(final CluckNode node) {
        hereID = UniqueIds.global.nextHexId();
        localpath = "auto-" + hereID;
        this.node = node;
        CluckPublisher.publish(node, localpath, localLoggingTarget);
    }

    /**
     * Start the Autologger - it will now start sending out logged messages.
     */
    public void start() {
        final CollapsingWorkerThread autologger = new CollapsingWorkerThread("network-autologger") {
            @Override
            protected void doWork() throws Throwable {
                Logger.fine("[LOCAL] Rechecking logging...");
                node.cycleSearchRemotes("auto-checker-" + hereID);
            }
        };
        node.startSearchRemotes("auto-checker-" + hereID, this);
        //autologger.start(); // TODO: Check if it's really safe to comment this out.
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) { // TODO: This is a common operation. Abstract it out?
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    autologger.trigger();
                }
            }
        }.attach("netwatch-" + hereID);
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if (message.startsWith("[NET] ")) { // From the network, so don't broadcast.
            return;
        }
        if (message.startsWith("[LOCAL] ")) { // Should not be sent over the network.
            return;
        }
        for (String cur : remotes) {
            LoggingTarget lt = targetCache.get(cur);
            if (lt != null) {
                lt.log(level, message, throwable);
            }
        }
    }

    public void log(LogLevel level, String message, String extended) {
        if (message.startsWith("[NET] ")) { // From the network, so don't broadcast.
            return;
        }
        if (message.startsWith("[LOCAL] ")) { // Should not be sent over the network.
            return;
        }
        for (String cur : remotes) {
            LoggingTarget lt = targetCache.get(cur);
            if (lt != null) {
                lt.log(level, message, extended);
            }
        }
    }

    public void handle(String remote, int remoteType) {
        if (remoteType != CluckNode.RMT_LOGTARGET) {
            return;
        }
        if (remote.contains("auto-") && !localpath.equals(remote) && targetCache.get(remote) == null) {
            targetCache.put(remote, CluckPublisher.subscribeLT(node, remote, LogLevel.FINEST));
            Logger.config("[LOCAL] Loaded logger: " + remote);
        }
        remotes.addIfNotFound(remote);
    }
}

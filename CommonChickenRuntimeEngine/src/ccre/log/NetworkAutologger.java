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

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.cluck.CluckRemoteListener;
import ccre.util.UniqueIds;

/**
 * A logging tool that shares all logging between networked cluck systems
 * automatically.
 *
 * @author skeggsc
 */
public final class NetworkAutologger implements LoggingTarget, CluckRemoteListener {
    private static Logger logger = LoggerFactory.getLogger(NetworkAutologger.class);

    /**
     * Whether or not a NetworkAutologger has been registered globally.
     */
    private static volatile boolean registered = false;
    private static final LoggingTarget localLoggingTarget = new LoggingTarget() {
        public void log(LogLevel level, String message, Throwable throwable) {
            switch (level.id) {
            case LogLevel.FATAL_ID:
                logger.error("[NET] " + message, throwable);
                break;
            case LogLevel.ERROR_ID:
                logger.error("[NET] " + message, throwable);
                break;
            case LogLevel.WARN_ID:
                logger.warn("[NET] " + message, throwable);
                break;
            case LogLevel.INFO_ID:
                logger.info("[NET] " + message, throwable);
                break;
            case LogLevel.DEBUG_ID:
                logger.debug("[NET] " + message, throwable);
                break;
            case LogLevel.TRACE_ID:
                logger.trace("[NET] " + message, throwable);
                break;
            }
        }

        public void log(LogLevel level, String message, String extended) {
            switch (level.id) {
            case LogLevel.FATAL_ID:
                logger.error("[NET] " + message, extended);
                break;
            case LogLevel.ERROR_ID:
                logger.error("[NET] " + message, extended);
                break;
            case LogLevel.WARN_ID:
                logger.warn("[NET] " + message, extended);
                break;
            case LogLevel.INFO_ID:
                logger.info("[NET] " + message, extended);
                break;
            case LogLevel.DEBUG_ID:
                logger.debug("[NET] " + message, extended);
                break;
            case LogLevel.TRACE_ID:
                logger.trace("[NET] " + message, extended);
                break;
            }
        }
    };

    /**
     * Register a new global NetworkAutologger with the logging manager. This
     * only occurs once - after that an warning will be logged again.
     */
    public static void register() {
        if (registered) {
            LoggerFactory.getLogger(NetworkAutologger.class).warn("Network autologger registered twice!");
            return;
        }
        registered = true;
        NetworkAutologger nlog = new NetworkAutologger(Cluck.getNode());
        CCRELoggerFactory.getLogger().addTarget(nlog);
        nlog.start();
    }

    /**
     * The current list of remotes to send logging messages to.
     */
    private final CopyOnWriteArrayList<String> remotes = new CopyOnWriteArrayList<String>();
    /**
     * The current cache of subscribed LoggingTargets to send logging messages
     * to.
     */
    private final HashMap<String, LoggingTarget> targetCache = new HashMap<String, LoggingTarget>();
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
        final EventOutput searcher = CluckPublisher.setupSearching(node, this);
        searcher.event();
        node.subscribeToStructureNotifications("netwatch-" + hereID, new EventOutput() {
            public void event() {
                LoggerFactory.getLogger(NetworkAutologger.class).trace("[LOCAL] Rechecking logging...");
                searcher.event();
            }
        });
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if (message.contains("[NET]")) { // From the network, so don't
                                         // broadcast.
            return;
        }
        if (message.contains("[LOCAL]")) { // Local messages should not be sent
                                           // over the network.
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
        if (message.contains("[NET]")) { // From the network, so don't
                                         // broadcast.
            return;
        }
        if (message.contains("[LOCAL]")) { // Should not be sent over the
                                           // network.
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
            targetCache.put(remote, CluckPublisher.subscribeLT(node, remote, LogLevel.TRACE));
            LoggerFactory.getLogger(NetworkAutologger.class).debug("[LOCAL] Loaded logger: " + remote);
        }
        remotes.addIfAbsent(remote);
    }
}

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
package ccre.cluck;

import ccre.channel.EventOutput;
import ccre.cluck.rpc.RPCManager;
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CHashMap;
import ccre.util.UniqueIds;
import java.util.Iterator;

/**
 * A CluckNode is the core hub of the Cluck networking system on a device. It
 * handles message routing, publishing, and subscribing.
 *
 * Usually the main instance of this is kept in CluckGlobals.
 *
 * @author skeggsc
 */
public class CluckNode {

    /**
     * The ID representing a PING message.
     */
    public static final byte RMT_PING = 0;
    /**
     * The ID representing an EventOutput firing message.
     */
    public static final byte RMT_EVENTOUTP = 1;
    /**
     * The ID representing an EventInput subscription message.
     */
    public static final byte RMT_EVENTINPUT = 2;
    /**
     * The ID representing an EventInput response message.
     */
    public static final byte RMT_EVENTINPUTRESP = 3;
    /**
     * The ID representing a logging message.
     */
    public static final byte RMT_LOGTARGET = 4;
    /**
     * The ID representing a BooleanInputProducer subscription message.
     */
    public static final byte RMT_BOOLPROD = 5;
    /**
     * The ID representing a BooleanInputProducer response message.
     */
    public static final byte RMT_BOOLPRODRESP = 6;
    /**
     * The ID representing a BooleanOutput write message.
     */
    public static final byte RMT_BOOLOUTP = 7;
    /**
     * The ID representing a FloatInputProducer subscription message.
     */
    public static final byte RMT_FLOATPROD = 8;
    /**
     * The ID representing a FloatInputProducer response message.
     */
    public static final byte RMT_FLOATPRODRESP = 9;
    /**
     * The ID representing a FloatOutput write message.
     */
    public static final byte RMT_FLOATOUTP = 10;
    /**
     * The ID representing an OutputStream write message.
     */
    public static final byte RMT_OUTSTREAM = 11;
    /**
     * The ID representing a network infrastructure modification notification.
     */
    public static final byte RMT_NOTIFY = 12;
    /**
     * The ID representing a remote procedure invocation.
     */
    public static final byte RMT_INVOKE = 13;
    /**
     * The ID representing a response to a remote procedure invocation.
     */
    public static final byte RMT_INVOKE_REPLY = 14;
    /**
     * The ID representing a notification that a link doesn't exist.
     */
    public static final byte RMT_NEGATIVE_ACK = 15;
    /**
     * The ID representing an EventInput unsubscription request.
     */
    public static final byte RMT_EVENTINPUT_UNSUB = 16;
    private static final String[] remoteNames = new String[]{"Ping", "EventOutput", "EventInput", "EventInputResponse", "LogTarget",
        "BooleanInputProducer", "BooleanInputProducerResponse", "BooleanOutput", "FloatInputProducer", "FloatInputProducerResponse",
        "FloatOutput", "OutputStream", "Notify", "RemoteProcedure", "RemoteProcedureReply", "NonexistenceNotification", "EventInputUnsubscription"};

    /**
     * Convert an RMT ID to a string.
     *
     * @param type The RMT_* message ID.
     * @return The version representing the name of the message type.
     */
    public static String rmtToString(int type) {
        if (type >= 0 && type < remoteNames.length) {
            return remoteNames[type];
        } else {
            return "Unknown #" + type;
        }
    }
    /**
     * A map of the current link names to the CluckLinks.
     */
    public final CHashMap<String, CluckLink> links = new CHashMap<String, CluckLink>();
    /**
     * The time when the last error message was printed about a link not
     * existing.
     */
    private long lastMissingLinkError = 0;
    /**
     * The link name of the last error message about a link not existing.
     */
    private String lastMissingLink = null;
    /**
     * The official RPCManager for this node.
     */
    private RPCManager rpcManager = null;

    /**
     * Notify everyone on the network that the network structure has been
     * modified - for example, when a connection is opened or closed.
     */
    public void notifyNetworkModified() {
        transmit("*", "#modsrc", new byte[]{RMT_NOTIFY});
    }

    /**
     * Transmit a message to the specified other link (relative to this node),
     * with the specified return address (relative to this node).
     *
     * Paths are /-separated, with each element being a link to follow.
     *
     * @param target The target path.
     * @param source The source path.
     * @param data The message data to transmit.
     */
    public void transmit(String target, String source, byte[] data) {
        transmit(target, source, data, null);
    }

    /**
     * Transmit a message to the specified other link (relative to this node),
     * with the specified return address (relative to this node). If this is a
     * broadcast, then don't include the specified link (to prevent infinite
     * loops).
     *
     * Paths are /-separated, with each element being a link to follow.
     *
     * @param target The target path.
     * @param source The source path.
     * @param data The message data to transmit.
     * @param denyLink The link for broadcasts to not follow.
     */
    public void transmit(String target, String source, byte[] data, CluckLink denyLink) {
        // TODO: Redo the byte counting mechanism!
        //estimatedByteCount += 24 + (target == null ? 0 : target.length()) + (source == null ? 0 : source.length()) + data.length; // 24 is the estimated packet overhead with a CluckTCPClient.
        if (target == null) {
            if (data.length == 0 || data[0] != RMT_NEGATIVE_ACK) {
                Logger.warning("Received message addressed to unreceving node (source: " + source + ")");
            }
        } else if ("*".equals(target)) {
            broadcast(source, data, denyLink);
        } else {
            int slash = target.indexOf('/');
            String direct, indirect;
            if (slash == -1) {
                direct = target;
                indirect = null;
            } else {
                direct = target.substring(0, slash);
                indirect = target.substring(slash + 1);
            }
            CluckLink link = links.get(direct);
            if (link == null) {
                reportMissingLink(data, source, target, direct);
            } else if (link.send(indirect, source, data) == false) {
                links.remove(direct); // Remove it if the link says that it's done.
            }
        }
    }

    /**
     * Broadcast a message to all receiving nodes.
     *
     * This is the same as <code>transmit("*", source, data, denyLink)</code>.
     *
     * @param source The source of the message.
     * @param data The contents of the message.
     * @param denyLink The link to not send broadcasts to.
     * @see #transmit(java.lang.String, java.lang.String, byte[],
     * ccre.cluck.CluckLink)
     */
    public void broadcast(String source, byte[] data, CluckLink denyLink) {
        for (Iterator<String> linkIter = links.iterator(); linkIter.hasNext();) {
            CluckLink cl = links.get(linkIter.next());
            if (cl != null && cl != denyLink) {
                if (cl.send("*", source, data) == false) {
                    linkIter.remove(); // Remove it if the link says that it's done.
                }
            }
        }
    }

    private void reportMissingLink(byte[] data, String source, String target, String direct) {
        // Warnings about lost RMT_NEGATIVE_ACK messages or research messages are annoying, so don't send these,
        // and don't warn about the same message path too quickly.
        if ((data.length == 0 || data[0] != RMT_NEGATIVE_ACK) && !target.contains("/rsch-")
                && (!direct.equals(lastMissingLink) || System.currentTimeMillis() >= lastMissingLinkError + 1000)) {
            lastMissingLink = direct;
            lastMissingLinkError = System.currentTimeMillis();
            Logger.warning("No link for " + target + "(" + direct + ") from " + source + "!");
            transmit(source, target, new byte[]{RMT_NEGATIVE_ACK});
        }
    }

    /**
     * Subscribe to any network structure modification notification messages,
     * which are sent each time that the structure of the Cluck network changes.
     *
     * @param localRecvName The name to bind to.
     * @param listener The listener to notify.
     */
    public void subscribeToStructureNotifications(String localRecvName, final EventOutput listener) {
        new CluckSubscriber(Cluck.getNode()) {
            @Override
            protected void receive(String source, byte[] data) {
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    listener.event();
                }
            }
        }.attach(localRecvName);
    }

    /**
     * Set up the specified CluckRemoteListener to start receiving updates about
     * what remote nodes are broadcasting their availability.
     *
     * After this is set up, use cycleSearchRemotes to recheck for more remote
     * nodes.
     *
     * @param localRecvName The local link name for the listener. Should be
     * unique.
     * @param listener The listener to notify with all found remotes.
     * @see #cycleSearchRemotes(java.lang.String)
     */
    public void startSearchRemotes(String localRecvName, final CluckRemoteListener listener) { // TODO: Make this a separate part of the library.
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (data.length == 2 && data[0] == RMT_PING) {
                    listener.handle(source, data[1]);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }
        }.attach(localRecvName);
        transmit("*", localRecvName, new byte[]{RMT_PING});
    }

    /**
     * Recheck for remote nodes, as in startSearchRemotes.
     *
     * @param localRecv The local receiving address, which must be the same as
     * the one passed to startSearchRemotes originally.
     * @see #startSearchRemotes(java.lang.String,
     * ccre.cluck.CluckRemoteListener)
     */
    public void cycleSearchRemotes(String localRecv) { // TODO: Make this a separate part of the library.
        transmit("*", localRecv, new byte[]{RMT_PING});
    }

    /**
     * Get a snapshot of the list of remote nodes of the specified remote type
     * (or null for all types).
     *
     * @param remoteType The remote type to search for, or null for all types.
     * @param timeout How long to wait for responses.
     * @return The snapshot of remotes.
     * @throws InterruptedException If the current thread is interrupted while
     * searching for remotes.
     */
    public String[] searchRemotes(final Integer remoteType, int timeout) throws InterruptedException { // TODO: Make this a separate part of the library, or delete it.
        final CArrayList<String> discovered = new CArrayList<String>();
        String localRecv = UniqueIds.global.nextHexId("rsch");
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (data.length == 2 && data[0] == RMT_PING) {
                    if (remoteType == null || remoteType == data[1]) {
                        synchronized (discovered) {
                            discovered.add(source);
                        }
                    }
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }
        }.attach(localRecv);
        try {
            transmit("*", localRecv, new byte[]{RMT_PING});
            Thread.sleep(timeout);
        } finally {
            links.remove(localRecv);
        }
        synchronized (discovered) {
            Iterator<String> it = discovered.iterator();
            String[] out = new String[discovered.size()];
            int i = 0;
            while (it.hasNext()) {
                String str = it.next();
                if (i >= out.length) {
                    Logger.severe("Lost remote due to concurrent modification!");
                    break;
                }
                out[i++] = str;
            }
            if (i < out.length) {
                Logger.severe("Damaged remotes due to concurrent modification!");
            }
            return out;
        }
    }

    /**
     * Get the name of the specified link.
     *
     * @param link The link to get the name for.
     * @return The link name.
     */
    public String getLinkName(CluckLink link) {
        if (link == null) {
            throw new NullPointerException();
        }
        for (String key : links) {
            if (links.get(key) == link) {
                return key;
            }
        }
        throw new RuntimeException("No such link!");
    }

    /**
     * Add the specified link at the specified link name.
     *
     * @param link The link.
     * @param linkName The link name.
     * @throws IllegalStateException if the specified link name is already used.
     */
    public void addLink(CluckLink link, String linkName) throws IllegalStateException {
        if (link == null) {
            throw new NullPointerException();
        }
        if (links.get(linkName) != null) {
            throw new IllegalStateException("Link name already used!");
        }
        links.put(linkName, link);
    }

    /**
     * Adds the specified link at the specified link name, replacing the current
     * link if necessary.
     *
     * @param link The link.
     * @param linkName The link name.
     */
    public void addOrReplaceLink(CluckLink link, String linkName) {
        if (link == null) {
            throw new NullPointerException();
        }
        if (links.get(linkName) != null) {
            Logger.fine("Replaced current link on: " + linkName);
        }
        links.put(linkName, link);
    }

    /**
     * Get the official RPCManager for this node.
     *
     * @return The RPCManager for this node.
     * @see ccre.cluck.rpc.RPCManager
     */
    public synchronized RPCManager getRPCManager() {
        if (rpcManager == null) {
            rpcManager = new RPCManager(this);
        }
        return rpcManager;
    }
}

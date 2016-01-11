/*
 * Copyright 2013-2016 Colby Skeggs
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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import ccre.channel.EventOutput;
import ccre.cluck.rpc.RPCManager;
import ccre.log.Logger;

/**
 * A CluckNode is the core hub of the Cluck networking system on a device. It
 * handles message routing, publishing, and subscribing.
 *
 * Usually the main instance of this is kept in CluckGlobals.
 *
 * @author skeggsc
 */
public class CluckNode implements Serializable {

    private static final long serialVersionUID = -5439319159206467512L;

    /**
     * A map of the current link names to the CluckLinks.
     */
    public final HashMap<String, CluckLink> links = new HashMap<String, CluckLink>();
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
     *
     * A notification message is simply a message with an
     * {@link CluckConstants#RMT_NOTIFY} header.
     */
    public void notifyNetworkModified() {
        transmit(CluckConstants.BROADCAST_DESTINATION, "#modsrc", new byte[] { CluckConstants.RMT_NOTIFY });
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
        if (data == null) {
            throw new NullPointerException();
        }
        // TODO: outlaw empty messages here.
        if (target == null) {
            if (data.length == 0 || data[0] != CluckConstants.RMT_NEGATIVE_ACK) {
                Logger.warning("[LOCAL] Received message addressed to unreceving node (source: " + source + ")");
            }
        } else if (CluckConstants.BROADCAST_DESTINATION.equals(target)) {
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
            } else {
                try {
                    boolean shouldLive = link.send(indirect, source, data);
                    if (!shouldLive) {
                        links.remove(direct);
                    }
                } catch (Throwable ex) {
                    Logger.severe("[LOCAL] Error while dispatching to Cluck link " + target, ex);
                }
            }
        }
    }

    /**
     * Broadcast a message to all receiving nodes.
     *
     * This is the same as
     * <code>transmit(CluckConstants.BROADCAST_DESTINATION, source, data, denyLink)</code>
     * .
     *
     * @param source The source of the message.
     * @param data The contents of the message.
     * @param denyLink The link to not send broadcasts to, or null.
     * @see #transmit(java.lang.String, java.lang.String, byte[],
     * ccre.cluck.CluckLink)
     */
    public void broadcast(String source, byte[] data, CluckLink denyLink) {
        if (data == null) {
            throw new NullPointerException();
        }
        for (String link : links.keySet().toArray(new String[links.keySet().size()])) {
            CluckLink cl = links.get(link);
            if (cl != null && cl != denyLink) {
                try {
                    boolean shouldLive = cl.send(CluckConstants.BROADCAST_DESTINATION, source, data);
                    if (!shouldLive) {
                        links.remove(link);
                    }
                } catch (Throwable ex) {
                    Logger.severe("[LOCAL] Error while broadcasting to Cluck link " + link, ex);
                }
            }
        }
    }

    private void reportMissingLink(byte[] data, String source, String target, String direct) {
        // Warnings about lost RMT_NEGATIVE_ACK messages or research messages
        // are annoying, so don't send these, and don't warn about the same
        // message path too quickly.

        // We use System.currentTimeMillis() instead of Time.currentTimeMillis()
        // because this is only to prevent message spam.
        if ((data.length == 0 || data[0] != CluckConstants.RMT_NEGATIVE_ACK) && !target.contains("/rsch-")) {
            if (!direct.equals(lastMissingLink) || System.currentTimeMillis() >= lastMissingLinkError + 1000) {
                lastMissingLink = direct;
                lastMissingLinkError = System.currentTimeMillis();
                Logger.warning("[LOCAL] No link for " + target + "(" + direct + ") from " + source + "!");
            }
            transmit(source, target, new byte[] { CluckConstants.RMT_NEGATIVE_ACK });
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
        if (localRecvName == null || listener == null) {
            throw new NullPointerException();
        }
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                // Ignore it.
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckConstants.RMT_NOTIFY) {
                    listener.event();
                }
            }
        }.attach(localRecvName);
    }

    /**
     * Get the name of the specified link.
     *
     * @param link The link to get the name for.
     * @throws IllegalArgumentException if the link isn't directly attached.
     * @return The link name.
     */
    public String getLinkName(CluckLink link) throws IllegalArgumentException {
        if (link == null) {
            throw new NullPointerException();
        }
        for (String key : links.keySet()) {
            if (links.get(key) == link) {
                return key;
            }
        }
        throw new IllegalArgumentException("No such link!");
    }

    /**
     * Add the specified link at the specified link name.
     *
     * @param link The link.
     * @param linkName The link name.
     * @throws IllegalStateException if the specified link name is already used.
     */
    public void addLink(CluckLink link, String linkName) throws IllegalStateException {
        if (link == null || linkName == null) {
            throw new NullPointerException();
        }
        if (linkName.contains("/")) {
            throw new IllegalArgumentException("Link name cannot contain slashes: " + linkName);
        }
        if (links.get(linkName) != null) {
            throw new IllegalStateException("Link name already used: " + linkName + " for " + links.get(linkName) + " not " + link);
        }
        links.put(linkName, link);
    }

    /**
     * Checks if a link exists. If it is routable, it exists. If it is not
     * routable, it probably (but not necessarily) doesn't exist - for example,
     * there is the case of a link pointing to a remote object.
     *
     * @param linkName the link name to check.
     * @return true if the link exists, and false otherwise.
     */
    public boolean hasLink(String linkName) {
        if (linkName == null) {
            throw new NullPointerException();
        }
        return links.containsKey(linkName);
    }

    /**
     * Removes the link attached to the specified link name.
     *
     * @param linkName The link name to remove.
     * @return whether or not there had been a link to remove.
     */
    public boolean removeLink(String linkName) {
        if (linkName == null) {
            throw new NullPointerException();
        }
        return links.remove(linkName) != null;
    }

    /**
     * Adds the specified link at the specified link name, replacing the current
     * link if necessary.
     *
     * @param link The link.
     * @param linkName The link name.
     */
    public void addOrReplaceLink(CluckLink link, String linkName) {
        if (link == null || linkName == null) {
            throw new NullPointerException();
        }
        if (linkName.contains("/")) {
            throw new IllegalArgumentException("Link name cannot contain slashes: " + linkName);
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException("Not serializable!");
    }

    private Object writeReplace() {
        return this == Cluck.getNode() ? new SerializedGlobalCluckNode() : this;
    }

    private static class SerializedGlobalCluckNode implements Serializable {

        private static final long serialVersionUID = 6554282414281830927L;

        private Object readResolve() {
            return Cluck.getNode();
        }
    }
}

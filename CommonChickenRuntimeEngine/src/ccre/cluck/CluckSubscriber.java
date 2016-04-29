/*
 * Copyright 2013-2016 Cel Skeggs
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

import ccre.log.Logger;
import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

/**
 * A helper class for objects shared on a CluckNode, by providing lots of basic
 * implementation for links.
 *
 * This provides sorting of messages into direct messages, broadcast messages,
 * and side-channel messages, provides easy ways to attach to a node, provides
 * reasonable default handling for side-channel messages, provides helpers for
 * responding to Cluck pings, and provides helpers for validating and sorting
 * based on Cluck message headers.
 *
 * @author skeggsc
 * @see CluckRMTSubscriber
 */
public abstract class CluckSubscriber implements CluckLink {

    /**
     * The CluckNode that this is attached to.
     */
    public final CluckNode node;
    /**
     * The link name of this subscriber.
     */
    private String linkName;

    /**
     * Create a new CluckSubscriber ready to be attached to the specified node.
     *
     * @param node The CluckNode that this should be shared over.
     */
    public CluckSubscriber(CluckNode node) {
        if (node == null) {
            throw new NullPointerException();
        }
        this.node = node;
    }

    /**
     * A default implementation of {@link #send(String, String, byte[])} that
     * automatically sorts messages into direct messages (to null), broadcast
     * messages (to *), and side-channel messages (to a subpath.)
     *
     * @param dest the destination address, usually expected to be null or the
     * broadcast address.
     * @param source the source address.
     * @param data the data.
     * @return always true - never detach.
     *
     * @see #receive(String, byte[])
     * @see #receiveBroadcast(String, byte[])
     * @see #receiveSideChannel(String, String, byte[])
     */
    @Override
    public final boolean send(String dest, String source, byte[] data) {
        if (dest == null) {
            receive(source, data);
        } else if (CluckConstants.BROADCAST_DESTINATION.equals(dest)) {
            receiveBroadcast(source, data);
        } else {
            receiveSideChannel(dest, source, data);
        }
        return true;
    }

    /**
     * Attach this subscriber to the specified name on the already-attached
     * node.
     *
     * @param name The name to attach with.
     */
    @SetupPhase
    public final void attach(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (linkName != null) {
            throw new IllegalStateException("Link name already set!");
        }
        this.linkName = name;
        node.addLink(this, name);
    }

    /**
     * Should be overridden to handle a message sent to something other than
     * this node or the broadcast address.
     *
     * @param dest The destination path.
     * @param source The source path.
     * @param data The message data.
     */
    @FlowPhase
    protected void receiveSideChannel(String dest, String source, byte[] data) {
        Logger.warning("Unhandled side-channel message sent to " + linkName + " / " + dest + " from " + source + "!");
    }

    /**
     * A handler for a common operation - ensure the message is not null, reply
     * to the message if it's a PING message, ensure that the remote type of the
     * message is correct, and then return true if all checks succeed.
     *
     * @param source The source address.
     * @param data The message contents.
     * @param rmt The remote type of this subscriber.
     * @return If this message should be handled as the given remote type.
     */
    @FlowPhase
    protected boolean requireRMT(String source, byte[] data, byte rmt) {
        return requireRMT(source, data, rmt, 1);
    }

    /**
     * A handler for a common operation - ensure the message is not null, reply
     * to the message if it's a PING message, ensure that the remote type of the
     * message is correct, ensure that the length is okay, and then return true
     * if all checks succeed.
     *
     * @param source The source address.
     * @param data The message contents.
     * @param rmt The remote type of this subscriber.
     * @param minLength The minimum length of the remote.
     * @return If this message should be handled as the given remote type.
     */
    @FlowPhase
    protected boolean requireRMT(String source, byte[] data, byte rmt, int minLength) {
        if (data.length == 0) {
            Logger.warning("Received null message from " + source);
        } else if (data[0] == CluckConstants.RMT_PING && data.length == 1) {
            node.transmit(source, linkName, new byte[] { CluckConstants.RMT_PING, rmt });
        } else if (data[0] == CluckConstants.RMT_NEGATIVE_ACK) {
            // Discard messages saying that the link is closed.
        } else if (data[0] != rmt) {
            Logger.warning("Received wrong RMT: " + CluckConstants.rmtToString(data[0]) + " from " + source + " (expected " + CluckConstants.rmtToString(rmt) + ") addressed to " + linkName);
        } else if (data.length < minLength) {
            Logger.warning("Received too-short message from " + source);
        } else {
            return true;
        }
        return false;
    }

    /**
     * Default handler for a broadcasted message - reply to it if it's a PING
     * message, otherwise ignore it
     *
     * @param source The source path.
     * @param data The message data.
     * @param rmt The remote type of this subscriber.
     */
    @FlowPhase
    protected void defaultBroadcastHandle(String source, byte[] data, byte rmt) {
        if (data.length == 1 && data[0] == CluckConstants.RMT_PING) {
            node.transmit(source, linkName, new byte[] { CluckConstants.RMT_PING, rmt });
        }
    }

    /**
     * Implement to handle messages sent to this subscriber. Usually this is
     * wrapped in a conditional of requireRMT.
     *
     * @param source The source path.
     * @param data The message data.
     */
    @FlowPhase
    protected abstract void receive(String source, byte[] data);

    /**
     * Implement to handle messages broadcasted to this subscriber. Usually this
     * is implemented using defaultBroadcastHandle.
     *
     * @param source The source path.
     * @param data The message data.
     */
    @FlowPhase
    protected abstract void receiveBroadcast(String source, byte[] data);
}

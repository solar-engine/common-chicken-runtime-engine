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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for objects shared on a CluckNode, by providing lots of basic
 * implementation for links.
 *
 * @author skeggsc
 */
public abstract class CluckSubscriber implements CluckLink {
    private static Logger logger = LoggerFactory.getLogger(CluckSubscriber.class);

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

    public final boolean send(String dest, String source, byte[] data) {
        if (dest == null) {
            receive(source, data);
        } else if ("*".equals(dest)) {
            receiveBroadcast(source, data);
        } else {
            handleOther(dest, source, data);
        }
        return true;
    }

    /**
     * Attach this subscriber to the specified name on the already-attached
     * node.
     *
     * @param name The name to attach with.
     */
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
    protected void handleOther(String dest, String source, byte[] data) {
        logger.warn("Unhandled side-channel message sent to " + linkName + " / " + dest + " from " + source + "!");
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
    protected boolean requireRMT(String source, byte[] data, byte rmt, int minLength) {
        if (data.length == 0) {
            logger.warn("Received null message from " + source);
        } else if (data[0] == CluckNode.RMT_PING && data.length == 1) {
            node.transmit(source, linkName, new byte[] { CluckNode.RMT_PING, rmt });
        } else if (data[0] == CluckNode.RMT_NEGATIVE_ACK) { // Discard messages
                                                            // saying that the
                                                            // link is closed.
            // Discard.
        } else if (data[0] != rmt) {
            logger.warn("Received wrong RMT: " + CluckNode.rmtToString(data[0]) + " from " + source + " (expected " + CluckNode.rmtToString(rmt) + ") addressed to " + linkName);
        } else if (data.length < minLength) {
            logger.warn("Received too-short message from " + source);
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
    protected void defaultBroadcastHandle(String source, byte[] data, byte rmt) {
        if (data.length == 1 && data[0] == CluckNode.RMT_PING) {
            node.transmit(source, linkName, new byte[] { CluckNode.RMT_PING, rmt });
        }
    }

    /**
     * Implement to handle messages sent to this subscriber. Usually this is
     * wrapped in a conditional of requireRMT.
     *
     * @param source The source path.
     * @param data The message data.
     */
    protected abstract void receive(String source, byte[] data);

    /**
     * Implement to handle messages broadcasted to this subscriber. Usually this
     * is implemented using defaultBroadcastHandle.
     *
     * @param source The source path.
     * @param data The message data.
     */
    protected abstract void receiveBroadcast(String source, byte[] data);
}

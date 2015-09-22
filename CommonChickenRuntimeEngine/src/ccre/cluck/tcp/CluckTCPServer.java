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
package ccre.cluck.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;
import ccre.net.ClientSocket;
import ccre.net.ConnectionReceiverThread;
import ccre.util.UniqueIds;

/**
 * A server-side handler for Cluck TCP connections.
 *
 * @author skeggsc
 */
public class CluckTCPServer extends ConnectionReceiverThread {
    private static Logger logger = LoggerFactory.getLogger(CluckTCPServer.class);

    /**
     * The shared CluckNode.
     */
    public final CluckNode node;

    /**
     * Create a new CluckTCPServer sharing a specified node on a specified port.
     *
     * @param node The node to share.
     * @param port The port to share it on.
     */
    public CluckTCPServer(CluckNode node, int port) {
        super("CluckTCPServer", port);
        this.node = node;
    }

    /**
     * Create a new CluckTCPServer sharing a specified node on port 80.
     *
     * @param node The node to share.
     */
    public CluckTCPServer(CluckNode node) {
        this(node, 80);
    }

    @Override
    protected void handleClient(ClientSocket conn) {
        try {
            DataInputStream din = new DataInputStream(conn.openInputStream());
            try {
                DataOutputStream dout = new DataOutputStream(conn.openOutputStream());
                try {
                    String linkName = CluckProtocol.handleHeader(din, dout, null);
                    if (linkName == null) {
                        linkName = UniqueIds.global.nextHexId("tcpserv");
                    }
                    logger.debug("Client connected at " + System.currentTimeMillis() + " named " + linkName);
                    CluckProtocol.setTimeoutOnSocket(conn);
                    CluckLink deny = CluckProtocol.handleSend(dout, linkName, node);
                    CluckProtocol.handleRecv(din, linkName, node, deny);
                    // node.notifyNetworkModified(); - sent by client, not
                    // needed here.
                } finally {
                    dout.close();
                }
            } finally {
                din.close();
            }
        } catch (IOException ex) {
            logger.warn("Bad IO in " + Thread.currentThread() + ": " + ex);
        }
    }
}

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

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.ConnectionReceiverThread;
import ccre.util.UniqueIds;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A server-side handler for Cluck TCP connections.
 *
 * @author skeggsc
 */
public class CluckTCPServer extends ConnectionReceiverThread {

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
            DataInputStream din = conn.openDataInputStream();
            DataOutputStream dout = conn.openDataOutputStream();
            String linkName = CluckProtocol.handleHeader(din, dout, null);
            if (linkName == null) {
                linkName = UniqueIds.global.nextHexId("tcpserv");
            }
            Logger.fine("Client connected at " + System.currentTimeMillis() + " named " + linkName);
            CluckLink deny = CluckProtocol.handleSend(dout, linkName, node);
            CluckProtocol.handleRecv(din, linkName, node, deny);
            // node.notifyNetworkModified(); - sent by client, not needed here.
        } catch (IOException ex) {
            Logger.warning("Bad IO in " + Thread.currentThread() + ": " + ex);
        }
    }
}

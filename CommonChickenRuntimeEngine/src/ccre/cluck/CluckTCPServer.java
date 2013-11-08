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
package ccre.cluck;

import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.ConnectionReceiverThread;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CluckTCPServer extends ConnectionReceiverThread {

    public final CluckNode node;

    public CluckTCPServer(CluckNode node, int port) {
        super("CluckTCPServer", port);
        this.node = node;
    }

    public CluckTCPServer(CluckNode node) {
        this(node, 80);
    }

    @Override
    protected void handleClient(ClientSocket conn) throws Throwable {
        DataInputStream din = conn.openDataInputStream();
        DataOutputStream dout = conn.openDataOutputStream();
        String linkName = CluckProtocol.handleHeader(din, dout, null);
        if (linkName == null) {
            linkName = "tcpserv-" + Integer.toHexString(conn.hashCode()) + "-" + System.currentTimeMillis();
        }
        Logger.fine("Client connected at " + System.currentTimeMillis() + " named " + linkName);
        node.notifyNetworkModified();
        CluckLink deny = CluckProtocol.handleSend(dout, linkName, node);
        CluckProtocol.handleRecv(din, linkName, node, deny);
    }
}

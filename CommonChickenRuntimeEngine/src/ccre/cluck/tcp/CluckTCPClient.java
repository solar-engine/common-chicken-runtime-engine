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
package ccre.cluck.tcp;

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;
import ccre.concurrency.ReporterThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.Network;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A self-maintaining handler for connecting to a specified remote address.
 *
 * @author skeggsc
 */
public class CluckTCPClient extends ReporterThread {

    /**
     * The CluckNode that this connection shares.
     */
    private final CluckNode node;
    /**
     * The link name for this connection.
     */
    private final String linkName;
    /**
     * The active remote socket.
     */
    private ClientSocket sock;
    /**
     * The connection port number.
     */
    private int port = 80;
    /**
     * The connection remote address.
     */
    private String remote;
    /**
     * The requested delay between each connection to the server.
     */
    public int reconnectDelayMillis = 5000;
    /**
     * The hint for what the other end of the connection should call this link.
     */
    private final String remoteNameHint;

    /**
     * Create a new CluckTCPClient connecting to the specified remote on the
     * default port, sharing the specified CluckNode, with the specified link
     * name and hint for what the other end should call this link.
     *
     * @param remote The remote address.
     * @param node The shared node.
     * @param linkName The link name.
     * @param remoteNameHint The hint for what the other end should call this
     * link.
     */
    public CluckTCPClient(String remote, CluckNode node, String linkName, String remoteNameHint) {
        super("cluckcli-" + remote);
        this.remote = remote;
        this.node = node;
        this.linkName = linkName;
        this.remoteNameHint = remoteNameHint;
    }

    /**
     * Modify the port that this connects to. This defaults to port 80.
     *
     * @param port The new port number.
     * @return This object.
     */
    public CluckTCPClient setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the remote address to this specified address.
     *
     * @param remote The remote address.
     */
    public void setRemote(String remote) {
        this.remote = remote;
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "IO Error while closing connection", ex);
            }
            sock = null;
        }
    }

    @Override
    protected void threadBody() throws IOException, InterruptedException {
        try {
            while (true) {
                long start = System.currentTimeMillis();
                Logger.fine("Connecting to " + remote + " at " + start);
                if (sock != null) {
                    try {
                        sock.close();
                    } catch (IOException ex) {
                        Logger.log(LogLevel.WARNING, "IO Error while closing connection", ex);
                    }
                }
                String postfix = "";
                try {
                    sock = Network.connect(remote, port);
                    DataInputStream din = sock.openDataInputStream();
                    DataOutputStream dout = sock.openDataOutputStream();
                    CluckProtocol.handleHeader(din, dout, remoteNameHint);
                    Logger.fine("Connected to " + remote + " at " + System.currentTimeMillis());
                    node.notifyNetworkModified();
                    CluckLink deny = CluckProtocol.handleSend(dout, linkName, node);
                    CluckProtocol.handleRecv(din, linkName, node, deny);
                } catch (IOException ex) {
                    if ("Remote server not available.".equals(ex.getMessage()) || "Timed out while connecting.".equals(ex.getMessage())) {
                        postfix = " (" + ex.getMessage() + ")";
                    } else {
                        Logger.log(LogLevel.WARNING, "IO Error while handling connection", ex);
                    }
                } catch (Throwable ex) {
                    Logger.log(LogLevel.SEVERE, "Uncaught exception in network handler!", ex);
                }
                long spent = System.currentTimeMillis() - start;
                long remaining = reconnectDelayMillis - spent;
                if (remaining > 0) {
                    if (remaining > 500) {
                        Logger.fine("Waiting " + remaining + " milliseconds before reconnecting." + postfix);
                    }
                    Thread.sleep(remaining);
                }
            }
        } finally {
            sock.close();
        }
    }
}

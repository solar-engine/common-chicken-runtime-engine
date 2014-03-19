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

import ccre.cluck.tcp.CluckTCPClient;
import ccre.cluck.tcp.CluckTCPServer;

/**
 * A storage location for the current CluckNode, CluckTCPServer, and
 * CluckTCPClient.
 *
 * @author skeggsc
 */
public final class CluckGlobals {

    private CluckGlobals() {
    }
    /**
     * The current CluckNode.
     */
    public static CluckNode node;
    /**
     * The current CluckTCPServer.
     */
    private static CluckTCPServer serv;
    /**
     * The current CluckTCPClient.
     */
    private static CluckTCPClient cli;

    /**
     * Create a Cluck node if it's not already initialized.
     */
    public static synchronized void ensureInitializedCore() {
        if (node == null) {
            node = new CluckNode();
        }
    }

    /**
     * Get the current global CluckTCPServer.
     *
     * @return The global CluckTCPServer.
     */
    public static synchronized CluckTCPServer getServer() {
        return serv;
    }

    /**
     * Get the current global CluckTCPClient.
     *
     * @return The global CluckTCPClient.
     */
    public static synchronized CluckTCPClient getClient() {
        return cli;
    }

    /**
     * Set up a server on the default port.
     */
    public static synchronized void setupServer() {
        if (serv != null) {
            throw new IllegalStateException("Server already set up!");
        }
        serv = new CluckTCPServer(node);
        serv.start();
    }

    /**
     * Set up a client pointing at the specified remote address, with the
     * specified name for this link and hint for what the remote end should call
     * this link.
     *
     * @param remote The remote address.
     * @param linkName The local link name.
     * @param hintedRemoteName The hint for what the remote server should call
     * this.
     */
    public static synchronized void setupClient(String remote, String linkName, String hintedRemoteName) {
        if (cli != null) {
            throw new IllegalStateException("Client already set up!");
        }
        cli = new CluckTCPClient(remote, node, linkName, hintedRemoteName);
        cli.start();
    }
}

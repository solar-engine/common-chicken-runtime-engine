/*
 * Copyright 2013, 2015 Colby Skeggs
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
package ccre.net;

import java.io.IOException;

/**
 * An open connection listening socket.
 *
 * @author skeggsc
 */
public class ServerSocket {

    private final java.net.ServerSocket sock;

    ServerSocket(java.net.ServerSocket ss) {
        sock = ss;
    }

    /**
     * Wait until a connection is made, and then return that connection.
     *
     * @return the ClientSocket representing the connection.
     * @throws IOException if an IO error occurs.
     */
    public ClientSocket accept() throws IOException {
        return new ClientSocket(sock.accept());
    }

    /**
     * Close this socket. This will terminate the connection.
     *
     * @throws IOException if an IO error occurs.
     */
    public void close() throws IOException {
        sock.close();
    }
}

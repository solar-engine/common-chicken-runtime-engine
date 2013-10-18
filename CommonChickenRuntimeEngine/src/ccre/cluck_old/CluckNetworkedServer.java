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
package ccre.cluck_old;

import ccre.concurrency.ReporterThread;
import ccre.net.Network;
import ccre.net.ServerSocket;
import java.io.IOException;

/**
 * The server for Cluck, allows clients to connect to it and transfer data.
 *
 * @see CluckNetworkedClient
 * @author skeggsc
 */
public class CluckNetworkedServer extends ReporterThread {

    /**
     * The socket behind this server.
     */
    protected ServerSocket sock;
    /**
     * The node to transfer data from/to.
     */
    protected CluckNode server;

    /**
     * Start a new server on the specified port that provides the specified
     * CluckNode.
     *
     * @param port the port to use, probably 80.
     * @param serv the CluckNode to transfer data from/to.
     * @throws IOException if an IO error occurs.
     */
    public CluckNetworkedServer(int port, CluckNode serv) throws IOException {
        super("CluckServer" + port);
        sock = Network.bind(port);
        this.server = serv;
        start();
    }

    @Override
    protected void threadBody() throws Throwable {
        while (true) {
            new CluckNetworkedClient(sock.accept(), server);
        }
    }
}

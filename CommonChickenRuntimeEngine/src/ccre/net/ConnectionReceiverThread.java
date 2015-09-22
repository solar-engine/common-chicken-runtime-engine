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
package ccre.net;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.concurrency.ReporterThread;

/**
 * A simple thread to allow easy writing of servers without creating a custom
 * thread to receive new client connections.
 *
 * @author skeggsc
 */
public abstract class ConnectionReceiverThread extends ReporterThread {
    private static Logger logger = LoggerFactory.getLogger(ConnectionReceiverThread.class);

    /**
     * The port that the server is started on.
     */
    private final int port;
    /**
     * The name of this thread.
     */
    private final String thrName;

    /**
     * Create a new ConnectionReceiverThread with the specified thread name and
     * port.
     *
     * @param name The thread name. This is passed directly to ReporterThread.
     * @param port This is the TCP port to host on.
     */
    public ConnectionReceiverThread(String name, int port) {
        super(name);
        this.thrName = name;
        this.port = port;
    }

    @Override
    protected final void threadBody() throws IOException {
        logger.debug("About to listen on " + port);
        ServerSocket sock;
        try {
            sock = Network.bind(port);
        } catch (IOException e) {
            if (e.getClass().getName().equals("java.net.BindException")) {
                logger.warn("Failed to bind to port " + port + ".");
                return;
            }
            throw e;
        }

        while (true) {
            final ClientSocket conn = sock.accept();
            new ReporterThread(thrName + "-client") {
                @Override
                protected void threadBody() throws Throwable {
                    try {
                        handleClient(conn);
                    } finally {
                        conn.close();
                    }
                }
            }.start();
        }
    }

    /**
     * Handle a client. This is run in a new thread, so you don't need to worry
     * about holding up other threads or accidentally terminating all
     * connections if an error occurs. The connection will be closed
     * automatically once this method returns or throws an error.
     *
     * @param conn The client connection.
     * @throws Throwable If something bad happens.
     */
    protected abstract void handleClient(ClientSocket conn) throws Throwable;
}

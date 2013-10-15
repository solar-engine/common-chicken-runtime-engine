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
package ccre.cluck2;

import ccre.concurrency.ReporterThread;
import ccre.net.ClientSocket;
import ccre.net.ServerSocket;

/**
 * A Cluck TCP-based server.
 *
 * @author skeggsc
 */
public class CluckTCPServer extends ReporterThread {

    /**
     * The socket behind this server.
     */
    protected ServerSocket sock;
    protected CluckModule serving;

    public CluckTCPServer(CluckModule serving) {
        super("CluckTCPServer");
        this.serving = serving;
    }

    @Override
    protected void threadBody() throws Throwable {
        while (true) {
            final ClientSocket cli = sock.accept();
            new CluckTCPServerWorker(serving, cli).start();
        }
    }
}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.log.NetworkAutologger;
import ccre.net.Network;

/**
 * A simple standalone cluck server for testing.
 *
 * @author skeggsc
 */
public class StandaloneCluckServer {
    private static Logger logger = LoggerFactory.getLogger(StandaloneCluckServer.class);

    /**
     * Start the simple server.
     *
     * @param args The program arguments. These are currently ignored.
     */
    public static void main(String[] args) {
        final long time = System.currentTimeMillis();
        NetworkAutologger.register();
        Cluck.publish("status-report", new EventOutput() {
            public void event() {
                StringBuilder b = new StringBuilder("Standalone server online on [");
                for (String addr : Network.listIPv4Addresses()) {
                    b.append(addr).append(", ");
                }
                b.setLength(b.length() - 2);
                logger.info(b.append("] - uptime ").append((System.currentTimeMillis() - time) / 1000).append(" seconds.").toString());
            }
        });
        Cluck.setupServer();
        LoggerFactory.getLogger(StandaloneCluckServer.class).info("Server is running.");
    }

    private StandaloneCluckServer() {
    }
}

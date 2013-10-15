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
package ccre.cluck3;

import ccre.chan.BooleanInputPoll;
import ccre.concurrency.ReporterThread;
import ccre.ctrl.Mixing;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.Network;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CluckTCPClient extends ReporterThread {

    private final CluckNode node;
    private final String linkName;
    private ClientSocket sock;
    private int port = 80;
    private final String remote;
    public BooleanInputPoll shouldAutoReconnect = Mixing.alwaysTrue;
    public int reconnectDelayMillis = 1000;

    public CluckTCPClient(String remote, CluckNode node, String linkName) {
        super("cluckcli-" + remote);
        this.remote = remote;
        this.node = node;
        this.linkName = linkName;
    }

    public CluckTCPClient setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    protected void threadBody() throws IOException, InterruptedException {
        try {
            while (shouldAutoReconnect.readValue()) {
                long start = System.currentTimeMillis();
                Logger.fine("Connecting to " + remote + " at " + start);
                try {
                    sock = Network.connect(remote, port);
                    DataInputStream din = sock.openDataInputStream();
                    DataOutputStream dout = sock.openDataOutputStream();
                    CluckProtocol.handleHeader(din, dout);
                    CluckProtocol.handleSend(dout, linkName, node);
                    CluckProtocol.handleRecv(din, linkName, node);
                } catch (IOException ex) {
                    Logger.log(LogLevel.WARNING, "IO Error while handling connection", ex);
                } catch (Throwable ex) {
                    Logger.log(LogLevel.SEVERE, "Uncaught exception in network handler!", ex);
                }
                long spent = System.currentTimeMillis() - start;
                long remaining = reconnectDelayMillis - spent;
                if (remaining > 0) {
                    if (remaining > 500) {
                        Logger.fine("Waiting " + remaining + " milliseconds before reconnecting.");
                    }
                    Thread.sleep(remaining);
                }
            }
        } finally {
            sock.close();
        }
    }
}

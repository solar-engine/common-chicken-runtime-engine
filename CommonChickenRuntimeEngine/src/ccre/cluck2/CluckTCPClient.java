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
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A Cluck TCP client allowing access to a remote CluckModule.
 *
 * @author skeggsc
 */
public class CluckTCPClient extends ReporterThread implements CluckModule {

    protected ClientSocket cli;
    protected DataInputStream din;
    protected DataOutputStream dout;
    protected String remote;
    protected final Object lock = new Object();
    protected LinkedList<Short> frees = new LinkedList<Short>();

    protected static interface Recv {

        public void receive() throws InterruptedException;
    }
    protected ArrayList<Recv> lines = new ArrayList<Recv>();

    public CluckTCPClient(String remote) {
        super("CluckTCPClient");
        this.remote = remote;
    }

    private short getAvailableLine() {
        Short out = frees.pollLast();
        if (out == null) {
            int on = lines.size();
            lines.add(null);
            if ((short) on == on) {
                return (short) on;
            }
            throw new RuntimeException("Out of lines!");
        }
        return out;
    }

    public CluckConnection connect(String route) throws IOException {
        if (cli == null) {
            throw new IOException("Route to " + remote + " is down!");
        }
        short line = getAvailableLine();
        final Object o = new Object();
        lines.set(line, new Recv() {
            public void receive() throws InterruptedException {
                synchronized (o) {
                    o.notifyAll();
                    o.wait();
                }
            }
        });
        synchronized (lock) {
            dout.writeShort(line);
            dout.writeUTF(route);
        }
        synchronized (o) {
            try {
                try {
                    o.wait();
                } catch (InterruptedException ex) {
                    // TODO: What if this occurs and causes a lock due to Recv waiting indefinitely?
                    Logger.warning("Potential corruption in CluckTCPClient.");
                    throw new InterruptedIOException("Interrupted during wait for connection response.");
                }
                lines.set(line, null);
                boolean success = din.readBoolean();
                if (!success) {
                    throw new IOException("[remote-connect] " + din.readUTF());
                }
            } finally {
                o.notifyAll();
            }
        }
        return new CluckConnection() {
            boolean closed = false;
            public void send(byte[] cur) throws IOException {
                if (closed) {
                    throw new IOException("Connection closed.");
                }
                WORKING HERE
            }

            public void setConnectionReceiver(CluckConnectionReceiver recv) throws IOException {
                if (closed) {
                    throw new IOException("Connection closed.");
                }
                WORKING HERE
            }

            public void close() throws IOException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    public void sendFlag(LogLevel level, String message, String extended, boolean hasBeenLogged) {
        NEED TO IMPLEMENT THIS
    }

    @Override
    protected void threadBody() throws Throwable {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.StreamConnection;

import ccre.log.Logger;
import ccre.util.CArrayUtils;
import ccre.util.CCollection;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * A network provider that works on Squawk and the FRC robot. This is because
 * Java ME doesn't use the same interface as Java SE.
 *
 * @author skeggsc
 */
public class IgneousNetworkProvider implements NetworkProvider {

    /**
     * Has this yet been registered as the network provider?
     */
    private static boolean registered = false;

    /**
     * Ensure that this is registered as the network provider. A warning will be
     * logged if this is called a second time.
     */
    public static void register() {
        if (registered) {
            Logger.warning("IgneousNetworkProvider already registered!");
            return;
        }
        registered = true;
        Network.setProvider(new IgneousNetworkProvider());
    }

    /**
     * Create a new IgneousNetworkProvider.
     */
    private IgneousNetworkProvider() {
    }

    public ClientSocket openClient(String host, int port) throws IOException {
        final SocketConnection conn = (SocketConnection) Connector.open("socket://" + host + ":" + port);
        return new IgneousClientSocket(conn);
    }

    public ServerSocket openServer(int port) throws IOException {
        return new IgneousServerSocket((ServerSocketConnection) Connector.open("socket://:" + port));
    }

    // Faked - there's no obvious way to get this info, so it's assumed based on the team number.
    public CCollection<String> listIPv4Addresses() {
        int team = DriverStation.getInstance().getTeamNumber();
        return CArrayUtils.asList(new String[] { "127.0.0.1", "10." + (team / 100) + "." + (team % 100) + "." + 2 });
    }

    public String getPlatformType() {
        return "NetIgneous";
    }

    private static class IgneousClientSocket implements ClientSocket {

        private final StreamConnection conn;

        IgneousClientSocket(StreamConnection conn) {
            this.conn = conn;
        }

        public boolean setSocketTimeout(int millis) throws IOException {
            return false; // Not implemented.
        }

        public InputStream openInputStream() throws IOException {
            return conn.openInputStream();
        }

        public OutputStream openOutputStream() throws IOException {
            return conn.openOutputStream();
        }

        public DataInputStream openDataInputStream() throws IOException {
            return conn.openDataInputStream();
        }

        public DataOutputStream openDataOutputStream() throws IOException {
            return conn.openDataOutputStream();
        }

        public void close() throws IOException {
            conn.close();
        }
    }

    private class IgneousServerSocket implements ServerSocket {

        private final ServerSocketConnection conn;

        IgneousServerSocket(ServerSocketConnection conn) {
            this.conn = conn;
        }

        public ClientSocket accept() throws IOException {
            return new IgneousClientSocket(conn.acceptAndOpen());
        }

        public void close() throws IOException {
            conn.close();
        }
    }

    public boolean isTimeoutException(IOException ex) {
        return false; // Not implemented.
    }
}

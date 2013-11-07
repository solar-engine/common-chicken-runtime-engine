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
package ccre.net;

import ccre.log.Logger;
import ccre.util.*;
import edu.wpi.first.wpilibj.DriverStation;
import java.io.*;
import javax.microedition.io.*;

/**
 * A network provider that works on Squawk and the FRC robot. This is because
 * Java ME doesn't use the same interface as Java SE.
 *
 * @author skeggsc
 */
public class IgneousNetworkProvider implements Network.Provider {

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
        Network.prov = new IgneousNetworkProvider();
    }

    /**
     * Create a new IgneousNetworkProvider.
     */
    private IgneousNetworkProvider() {
    }

    public ClientSocket openClient(String host, int port) throws IOException {
        final SocketConnection conn = (SocketConnection) Connector.open("socket://" + host + ":" + port);
        return wrap(conn);
    }

    public ServerSocket openServer(int port) throws IOException {
        final ServerSocketConnection conn = (ServerSocketConnection) Connector.open("socket://:" + port);
        return new ServerSocket() {
            public ClientSocket accept() throws IOException {
                return wrap(conn.acceptAndOpen());
            }

            public void close() throws IOException {
                conn.close();
            }
        };
    }

    private ClientSocket wrap(final StreamConnection conn) {
        return new ClientSocket() {
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
        };
    }

    // Faked - there's no obvious way to get this info, so it's assumed based on the team number.
    public CCollection listIPv4Addresses() {
        int team = DriverStation.getInstance().getTeamNumber();
        return CArrayUtils.asList(new Object[]{"127.0.0.1", "10." + (team / 100) + "." + (team % 100) + "." + 2});
    }

    public String getPlatformType() {
        return "NetIgneous";
    }
}

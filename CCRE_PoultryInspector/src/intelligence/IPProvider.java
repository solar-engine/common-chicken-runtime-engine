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
package intelligence;

import ccre.cluck.CluckGlobals;
import ccre.holders.StringHolder;
import ccre.log.Logger;
import ccre.net.Network;
import ccre.util.CCollection;
import java.io.IOException;

/**
 * The system to calculate what address to connect to.
 *
 * @author skeggsc
 */
public class IPProvider {

    /**
     * The address that should be connected to. "*" means that it should
     * autoconfigure based on the network.
     */
    public static StringHolder forcedAddress = new StringHolder("*");

    /**
     * Compute an address and connect to it.
     *
     * @throws IOException if the target cannot be connected to.
     */
    public static void connect() throws IOException {
        Logger.finest("Connecting...");
        String val = forcedAddress.get();
        if (val.isEmpty()) {
            val = "*";
            forcedAddress.set("*");
        }
        String addr = val.equals("*") ? getAddress() : val;
        if (addr == null) {
            if (CluckGlobals.cli != null) { // TODO: Move this to CluckGlobals.
                CluckGlobals.cli.stopClient();
                CluckGlobals.cli = null;
            }
            return;
        }
        Logger.finer("Found connect address: " + addr);
        CluckGlobals.reconnectClient(addr, 80);
    }

    /**
     * Calculate the address to connect to based on the network configuration.
     * This means looking for a network of 10.X.Y.Z and then the target is
     * 10.X.Y.2. Otherwise, it logs a warning and returns null.
     *
     * @return the probable address of the robot, or null if it cannot be
     * determined.
     */
    public static String getAddress() {
        CCollection<String> addresses = Network.listIPv4Addresses();
        for (String addr : addresses) {
            if (addr.startsWith("10.")) {
                return addr.substring(0, addr.lastIndexOf('.') + 1).concat("2");
            }
        }
        Logger.warning("Subnet Autodetect: Cannot find any valid network addresses!");
        return null;
    }
}

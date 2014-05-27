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
package intelligence;

import ccre.cluck.Cluck;
import ccre.log.Logger;
import ccre.net.Network;
import ccre.util.CCollection;

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
    public static String forcedAddress = "*";

    private static final boolean useHigherPort;

    static {
        String os = System.getProperty("os.name");
        useHigherPort = os != null && os.startsWith("Mac ");
    }

    /**
     * Compute an address and connect to it.
     */
    public static void connect() {
        String addr;
        if ("*".equals(forcedAddress)) {
            addr = getAddress();
        } else {
            addr = forcedAddress;
            Logger.finer("Forced connect address: " + addr);
        }
        if (Cluck.getClient() == null) {
            Cluck.setupClient(addr, "robot", "phidget");
        } else {
            Cluck.getClient().setRemote(addr);
        }
    }

    /**
     * Calculate the address to connect to based on the network configuration.
     *
     * @return the probable address of the robot, or null if it cannot be
     * determined.
     */
    public static String getAddress() {
        CCollection<String> addresses = Network.listIPv4Addresses();
        for (String addr : addresses) {
            if (addr.startsWith("10.") && addr.substring(0, addr.lastIndexOf('.')).length() <= 8) {
                String out = addr.substring(0, addr.lastIndexOf('.') + 1).concat("2:443");
                Logger.fine("Connecting to robot at: " + out);
                return out;
            } else if (addr.equals("192.168.7.1")) {
                Logger.fine("Connecting over BeagleBone direct connection.");
                return "192.168.7.2";
            }
        }
        Logger.warning("Autodetect: Not on robot subnet. Defaulting to localhost.");
        return useHigherPort ? "127.0.0.1:1540" : "127.0.0.1";
    }

    /**
     * Used to ensure that IPProvider is initialized.
     */
    public static void init() {
        // Do nothing
    }
}

/*
 * Copyright 2013-2015 Colby Skeggs
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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.slf4j.LoggerFactory;

/**
 * The global Network handler. This contains a location to store the current
 * network provider, and methods to interact with the provider, which will
 * automatically detect the provider in many cases.
 *
 * @author skeggsc
 */
public class Network {

    /**
     * Connect to the specified IP address and port, and return a ClientSocket
     * representing the connection.
     *
     * @param targetAddress the IP address to connect to.
     * @param port the port to connect to.
     * @return the ClientSocket that represents the connection.
     * @throws IOException if an IO error occurs.
     */
    public static ClientSocket connect(String targetAddress, int port) throws IOException {
        Socket sock = new Socket();
        boolean leaveOpen = false;
        try {
            InetSocketAddress ina = new InetSocketAddress(targetAddress, port);
            try {
                sock.connect(ina, 500);// TODO: What timeout should be used?
                leaveOpen = true;
                return new ClientSocket(sock);
            } catch (SocketTimeoutException ex) {
                throw new ConnectException("Timed out while connecting to " + ina);// Smaller
                                                                                   // traceback.
            } catch (ConnectException ctc) {
                if (ctc.getMessage().startsWith("Connection timed out")) {
                    throw new ConnectException("Timed out while connecting to " + ina);// Smaller
                                                                                       // traceback.
                } else if (ctc.getMessage().startsWith("Connection refused")) {
                    throw new ConnectException("Remote server not available: " + ina);// Smaller
                                                                                      // traceback.
                }
                throw ctc;
            }
        } finally {
            if (!leaveOpen) {
                sock.close();
            }
        }
    }

    /**
     * This is like connect(targetAddress, port), but if the target address ends
     * in a colon followed by a number, that port will be used instead of the
     * specified port.
     *
     * @param rawAddress the IP address to connect to, with an option port
     * specifier.
     * @param default_port the default port to connect to if there is no port
     * specifier.
     * @return the ClientSocket that represents the connection.
     * @throws java.io.IOException if an IO error occurs or the port specifier
     * is invalid.
     */
    public static ClientSocket connectDynPort(String rawAddress, int default_port) throws IOException {
        int cln = rawAddress.lastIndexOf(':');
        int port = default_port;
        String realAddress = rawAddress;
        if (cln != -1) {
            try {
                port = Integer.parseInt(rawAddress.substring(cln + 1));
                realAddress = rawAddress.substring(0, cln);
            } catch (NumberFormatException ex) {
                throw new IOException("Cannot connect to address - bad port specifier: " + rawAddress.substring(cln + 1));
            }
        }
        return connect(realAddress, port);
    }

    /**
     * Listen on the specified port, and return a ServerSocket representing the
     * connection.
     *
     * @param port the port to listen on.
     * @return the ServerSocket that represents the connection.
     * @throws IOException if an IO error occurs.
     */
    public static ServerSocket bind(int port) throws IOException {
        return new ServerSocket(new java.net.ServerSocket(port));
    }

    /**
     * List all IPv4 addresses of the current system. This includes 127.0.0.1.
     *
     * @return a collection of the IPv4 addresses of the current system.
     */
    public static Collection<String> listIPv4Addresses() {
        Enumeration<NetworkInterface> enm = null;
        try {
            enm = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            LoggerFactory.getLogger(Network.class).error("Could not enumerate IP addresses!", ex);
        }
        if (enm == null) {
            return Collections.emptyList();
        }
        ArrayList<String> allAddresses = new ArrayList<String>();
        while (enm.hasMoreElements()) {
            NetworkInterface ni = enm.nextElement();
            Enumeration<InetAddress> ins = ni.getInetAddresses();
            while (ins.hasMoreElements()) {
                InetAddress addr = ins.nextElement();
                byte[] raw = addr.getAddress();
                if (raw.length == 4) {
                    allAddresses.add(addr.getHostAddress());
                } else if (raw.length != 16) {
                    LoggerFactory.getLogger(Network.class).warn("Found an address that's not 4 or 16 long: " + Arrays.toString(raw));
                }
            }
        }
        return allAddresses;
    }

    private Network() {
    }

}

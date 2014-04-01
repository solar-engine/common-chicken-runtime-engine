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

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CCollection;
import java.io.IOException;

/**
 * The global Network handler. This contains a location to store the current
 * network provider, and methods to interact with the provider, which will
 * automatically detect the provider in many cases.
 *
 * @author skeggsc
 */
public class Network {

    private Network() {
    }

    /**
     * A Network provider.
     */
    public static interface Provider {

        /**
         * Open a client socket to the specified target address and port.
         *
         * @param targetAddress the IP address to connect to.
         * @param port the port to connect to.
         * @return the ClientSocket that represents the connection.
         * @throws IOException if an IO error occurs.
         */
        ClientSocket openClient(String targetAddress, int port) throws IOException;

        /**
         * Open a server socket to listen on the specified port.
         *
         * @param port the port to listen on.
         * @return the ServerSocket that represents the connection.
         * @throws IOException
         */
        ServerSocket openServer(int port) throws IOException;

        /**
         * List all IPv4 addresses of the current system. This includes
         * 127.0.0.1.
         *
         * @return a collection of the IPv4 addresses of the current system.
         */
        CCollection<String> listIPv4Addresses();

        /**
         * Gets a string representing the platform type for this system. This is
         * used by CluckNode to create a node ID.
         *
         * @return The platform type string.
         */
        public String getPlatformType();
    }
    /**
     * The current network provider.
     */
    private static Provider prov = null;
    
    static synchronized void setProvider(Provider pvdr) {
        if (prov != null) {
            throw new IllegalStateException("Provider already registered!");
        }
        prov = pvdr;
    }

    /**
     * Return the current network provider, finding it if it doesn't exist. This
     * will look for ccre.net.DefaultNetworkProvider by default, and throw an
     * exception if it doesn't exist.
     *
     * @return the active network Provider.
     */
    public static synchronized Provider getProvider() {
        if (prov == null) {
            try {
                prov = (Provider) Class.forName("ccre.net.DefaultNetworkProvider").newInstance();
            } catch (InstantiationException ex) {
                Logger.log(LogLevel.WARNING, "Cannot start network provider!", ex);
                throw new RuntimeException("Cannot load the default network provider. It was probably (purposefully) ignored during the build process.");
            } catch (IllegalAccessException ex) {
                Logger.log(LogLevel.WARNING, "Cannot start network provider!", ex);
                throw new RuntimeException("Cannot load the default network provider. It was probably (purposefully) ignored during the build process.");
            } catch (ClassNotFoundException ex) {
                Logger.log(LogLevel.WARNING, "Cannot start network provider!", ex);
                throw new RuntimeException("Cannot load the default network provider. It was probably (purposefully) ignored during the build process.");
            }
        }
        return prov;
    }

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
        return getProvider().openClient(targetAddress, port);
    }

    /**
     * This is like connect(targetAddress, port), but if the target address ends
     * in a colon followed by a number, that port will be used instead of the
     * specified port.
     *
     * @param targetAddress the IP address to connect to, with an option port
     * specifier.
     * @param port the default port to connect to if there is no port specifier.
     * @return the ClientSocket that represents the connection.
     * @throws java.io.IOException if an IO error occurs or the port specifier
     * is invalid.
     */
    public static ClientSocket connectDynPort(String targetAddress, int port) throws IOException {
        int cln = targetAddress.lastIndexOf(':');
        if (cln != -1) {
            try {
                port = Integer.parseInt(targetAddress.substring(cln + 1));
                targetAddress = targetAddress.substring(0, cln);
            } catch (NumberFormatException ex) {
                throw new IOException("Cannot connect to address - bad port specifier: " + targetAddress.substring(cln + 1));
            }
        }
        return connect(targetAddress, port);
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
        return getProvider().openServer(port);
    }

    /**
     * List all IPv4 addresses of the current system. This includes 127.0.0.1.
     *
     * @return a collection of the IPv4 addresses of the current system.
     */
    public static CCollection<String> listIPv4Addresses() {
        return getProvider().listIPv4Addresses();
    }

    /**
     * Gets a string representing the platform type for this system. This is
     * used by CluckNode to create a node ID.
     *
     * @return The platform type string.
     */
    public static String getPlatformType() {
        return getProvider().getPlatformType();
    }
}

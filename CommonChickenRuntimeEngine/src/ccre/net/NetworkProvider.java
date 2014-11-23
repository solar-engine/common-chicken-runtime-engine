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

import ccre.util.CCollection;
import java.io.IOException;

/**
 * A Network provider, which provides access to the basic networking
 * functionality of the platform.
 */
public interface NetworkProvider {

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
     * @throws IOException If a server cannot be started.
     */
    ServerSocket openServer(int port) throws IOException;

    /**
     * List all IPv4 addresses of the current system. This includes 127.0.0.1.
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

    /**
     * Checks if the specified exception was thrown due to a timeout while
     * reading from a socket.
     *
     * @param ex the IO exception to check
     * @return if the exception was thrown due to a timeout.
     */
    public boolean isTimeoutException(IOException ex);

}

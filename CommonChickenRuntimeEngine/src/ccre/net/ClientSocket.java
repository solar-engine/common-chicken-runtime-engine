/*
 * Copyright 2013, 2015 Colby Skeggs
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * An open data transfer socket.
 *
 * @author skeggsc
 */
public class ClientSocket {

    private final Socket sock;

    ClientSocket(Socket s) {
        sock = s;
    }

    /**
     * Set the blocking operation timeout on this socket in milliseconds to
     * wait, or zero to wait forever.
     *
     * @param millis how long to wait before throwing an IOException.
     * @throws IOException if an IO error occurs.
     */
    public void setSocketTimeout(int millis) throws IOException {
        sock.setSoTimeout(millis);
    }

    /**
     * Open an InputStream that reads from this socket.
     *
     * @return the InputStream that reads from the socket.
     * @throws IOException if an IO error occurs.
     */
    public InputStream openInputStream() throws IOException {
        return TrafficCounting.wrap(sock.getInputStream());
    }

    /**
     * Open an OutputStream that writes to this socket.
     *
     * @return the OutputStream that writes to the socket.
     * @throws IOException if an IO error occurs.
     */
    public OutputStream openOutputStream() throws IOException {
        return TrafficCounting.wrap(sock.getOutputStream());
    }

    /**
     * Close this socket. This will terminate the connection.
     *
     * @throws IOException if an IO error occurs.
     */
    public void close() throws IOException {
        sock.close();
    }
}

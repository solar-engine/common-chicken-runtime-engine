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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An open data transfer socket.
 *
 * @author skeggsc
 */
public interface ClientSocket {

    /**
     * Open an InputStream that reads from this socket.
     *
     * @return the InputStream that reads from the socket.
     * @throws IOException if an IO error occurs.
     */
    public InputStream openInputStream() throws IOException;

    /**
     * Open an OutputStream that writes to this socket.
     *
     * @return the OutputStream that writes to the socket.
     * @throws IOException if an IO error occurs.
     */
    public OutputStream openOutputStream() throws IOException;

    /**
     * Open a DataInputStream that reads from this socket.
     *
     * @return the DataInputStream that reads from the socket.
     * @throws IOException if an IO error occurs.
     */
    public DataInputStream openDataInputStream() throws IOException;

    /**
     * Open a DataOutputStream that writes to this socket.
     *
     * @return the DataOutputStream that writes to the socket.
     * @throws IOException if an IO error occurs.
     */
    public DataOutputStream openDataOutputStream() throws IOException;

    /**
     * Close this socket. This will terminate the connection.
     *
     * @throws IOException if an IO error occurs.
     */
    public void close() throws IOException;
}

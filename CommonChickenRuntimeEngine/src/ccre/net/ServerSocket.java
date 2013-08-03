package ccre.net;

import java.io.IOException;

/**
 * An open connection listening socket.
 *
 * @author skeggsc
 */
public interface ServerSocket {

    /**
     * Wait until a connection is made, and then return that connection.
     *
     * @return the ClientSocket representing the connection.
     * @throws IOException if an IO error occurs.
     */
    public ClientSocket accept() throws IOException;

    /**
     * Close this socket. This will terminate the connection.
     *
     * @throws IOException if an IO error occurs.
     */
    public void close() throws IOException;
}

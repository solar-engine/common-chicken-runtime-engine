package ccre.cluck;

import ccre.concurrency.ReporterThread;
import ccre.net.Network;
import ccre.net.ServerSocket;
import java.io.IOException;

/**
 * The server for Cluck, allows clients to connect to it and transfer data.
 *
 * @see CluckNetworkedClient
 * @author skeggsc
 */
public class CluckNetworkedServer extends ReporterThread {

    /**
     * The socket behind this server.
     */
    protected ServerSocket sock;
    /**
     * The node to transfer data from/to.
     */
    protected CluckNode server;

    /**
     * Start a new server on the specified port that provides the specified
     * CluckNode.
     *
     * @param port the port to use, probably 80.
     * @param serv the CluckNode to transfer data from/to.
     * @throws IOException if an IO error occurs.
     */
    public CluckNetworkedServer(int port, CluckNode serv) throws IOException {
        super("CluckServer" + port);
        sock = Network.bind(port);
        this.server = serv;
        start();
    }

    @Override
    protected void threadBody() throws Throwable {
        while (true) {
            new CluckNetworkedClient(sock.accept(), server);
        }
    }
}

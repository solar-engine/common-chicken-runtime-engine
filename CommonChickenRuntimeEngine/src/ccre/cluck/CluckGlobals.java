package ccre.cluck;

import java.io.IOException;

/**
 * A class containing static fields for various Cluck objects.
 * @author skeggsc
 */
public class CluckGlobals {

    /**
     * The global CluckEncoder, most likely the one used for the entire
     * application. Should use CluckGlobals.node as its node.
     */
    public static CluckEncoder encoder;
    /**
     * The global CluckNode, most likely the one used for the entire
     * application.
     */
    public static CluckNode node;
    /**
     * The global CluckNetworkedServer, if any; most likely the one used for the
     * entire application.
     */
    public static CluckNetworkedServer serv;
    /**
     * The global CluckNetworkedClient, if any; possibly the one used for the
     * entire application.
     */
    public static CluckNetworkedClient cli;

    /**
     * Ensures that the CluckGlobals.node and CluckGlobals.encoder fields are
     * initialized, and initializes them if they aren't.
     */
    public static void ensureInitializedCore() {
        if (node == null) {
            node = new CluckNode();
        }
        if (encoder == null) {
            encoder = new CluckEncoder(node);
        }
    }

    /**
     * Begin a server on the specified port.
     * @param port the port number. you should probably use 80.
     * @throws IOException if an IO error occurs.
     */
    public static void initializeServer(int port) throws IOException {
        ensureInitializedCore();
        if (serv == null && cli == null) {
            serv = new CluckNetworkedServer(port, node);
        } else {
            throw new IllegalStateException("Cluck Globals already initialized!");
        }
    }

    /**
     * Begin a client connection on the specified port.
     * @param target the target IP address.
     * @param port the port number. you should probably use 80.
     * @throws IOException if an IO Error occurs.
     */
    public static void initializeClient(String target, int port) throws IOException {
        ensureInitializedCore();
        if (serv == null && cli == null) {
            cli = new CluckNetworkedClient(target, port, node);
        } else {
            throw new IllegalStateException("Cluck Globals already initialized!");
        }
    }

    /**
     * Reconnect the client on the specified port.
     * @param target the target IP address.
     * @param port the port number. you should probably use 80.
     * @throws IOException if an IO Error occurs.
     */
    public static void reconnectClient(String target, int port) throws IOException {
        if (encoder != null && node != null && serv == null) {
            if (cli != null) {
                cli.stopClient();
                cli = null;
            }
            cli = new CluckNetworkedClient(target, port, node);
        } else {
            throw new IllegalStateException("Cluck Globals not properly initialized!");
        }
    }
}

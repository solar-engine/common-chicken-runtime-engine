package ccre.net;

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

    /**
     * A Network provider.
     */
    static interface Provider {

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
    }
    /**
     * The current network provider.
     */
    static Provider prov = null;

    /**
     * Return the current network provider, finding it if it doesn't exist. This
     * will look for ccre.net.DefaultNetworkProvider by default, and throw an
     * exception if it doesn't exist.
     *
     * @return the active network Provider.
     */
    static Provider getProvider() {
        if (prov == null) {
            try {
                prov = (Provider) Class.forName("ccre.net.DefaultNetworkProvider").newInstance();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Cannot load the default network provider. It was probably (purposefully) ignored during the build process.");
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Cannot load the default network provider. It was probably (purposefully) ignored during the build process.");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
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
}

package ccre.cluck;

import ccre.concurrency.ReporterThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.Network;
import ccre.util.CArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * A single endpoint of a Cluck connection. Both sides will transmit subscribed
 * messages from their CluckNodes.
 *
 * @see CluckNetworkedServer
 * @author skeggsc
 */
public class CluckNetworkedClient extends ReporterThread implements CluckChannelListener, CluckSubscriptionListener {

    /**
     * The socket behind this connection.
     */
    protected ClientSocket sock;
    /**
     * The DataOutputStream behind this connection. Acquire writeLock while
     * using this.
     *
     * @see #writeLock
     */
    protected DataOutputStream dout;
    /**
     * The DataInputStream behind this connection.
     */
    protected DataInputStream din;
    /**
     * The CluckNode to be shared.
     */
    protected CluckNode node;
    /**
     * A lock to be acquired for writing to the output stream.
     */
    protected final Object writeLock = new Object();
    /**
     * Active flag. Set to false to stop the client. Use stopClient() to do
     * that.
     *
     * @see #stopClient()
     */
    protected boolean shouldRun = true;
    /**
     * The list of what the other end of the connection wants.
     */
    protected CArrayList<String> otherEndWants = new CArrayList<String>();
    // TODO: What if another client of the server wants something but the server doesn't? Currently that won't get transmitted.

    /**
     * Connect to the given address and port, and create a client to handle the
     * connection.
     *
     * @param name the address to connect to.
     * @param port the port number to connect to, probably 80.
     * @param node the CluckNode to synchronize.
     * @throws IOException if an IO error occurs.
     */
    public CluckNetworkedClient(String name, int port, CluckNode node) throws IOException {
        this(Network.connect(name, port), node);
    }

    /**
     * Create a client to handle the given connection.
     *
     * @param sock the connection.
     * @param node the CluckNode to synchronize.
     * @throws IOException if an IO error occurs.
     */
    CluckNetworkedClient(ClientSocket sock, CluckNode node) throws IOException {
        super("CluckServerHandler" + sock);
        this.sock = sock;
        this.node = node;
        if (node == null) {
            throw new NullPointerException();
        }
        dout = sock.openDataOutputStream();
        din = sock.openDataInputStream();
        start();
    }

    /**
     * Stop the client. The client's thread will stop as soon as possible.
     */
    public void stopClient() {
        shouldRun = false;
        this.interrupt();
        try {
            sock.close();
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Socket error during disconnect", ex);
        }
    }

    @Override
    protected void threadBody() throws Throwable {
        Logger.finer("Client connected");
        try {
            node.subscribe(null, this);
            node.subscribeToSubscriptions(this);
            while (shouldRun) {
                int type = din.readByte();
                String channel = din.readUTF();
                if (type == 0) { // Message
                    byte[] data = new byte[din.readShort() & 0xffff];
                    din.readFully(data);
                    node.publish(channel, data, this);
                } else if (type == 1) { // Subscribe
                    otherEndWants.add(channel);
                } else if (type == 2) { // Unsubscribe
                    otherEndWants.remove(channel);
                } else {
                    Logger.warning("Invalid messagetype byte: " + type);
                }
            }
        } catch (EOFException e) {
            node.unsubscribe(null, this);
            Logger.finer("Client disconnected.");
        } catch (IOException e) {
            node.unsubscribe(null, this);
            Logger.log(LogLevel.FINER, "IOException in client", e);
        } finally {
            node.unsubscribe(null, this);
            node.unsubscribeFromSubscriptions(this);
            try {
                sock.close();
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Socket error during disconnect", ex);
            }
        }
    }

    public void receive(String channel, byte[] data) {
        if (!isAlive()) {
            Logger.warning("Message after death!");
            node.unsubscribe(null, this);
            return;
        }
        if (!otherEndWants.contains(channel)) {
            return;
        }
        if ((data.length & 0xffff) != data.length) {
            throw new IndexOutOfBoundsException("Too much data!");
        }
        synchronized (writeLock) {
            try {
                dout.writeByte(0);
                dout.writeUTF(channel);
                dout.writeShort(data.length);
                dout.write(data);
            } catch (IOException ex) {
                node.unsubscribe(null, this);
                Logger.log(LogLevel.WARNING, "Disconnect during Cluck send", ex);
                try {
                    sock.close();
                } catch (IOException ex1) {
                    Logger.log(LogLevel.WARNING, "Socket error during disconnect", ex1);
                }
            }
        }
    }

    public void addSubscription(String key) {
        synchronized (writeLock) {
            try {
                dout.writeByte(1);
                dout.writeUTF(key);
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Disconnect during Cluck send", ex);
                try {
                    sock.close();
                } catch (IOException ex1) {
                    Logger.log(LogLevel.WARNING, "Socket error during disconnect", ex1);
                }
            }
        }
    }

    public void removeSubscription(String key) {
        synchronized (writeLock) {
            try {
                dout.writeByte(2);
                dout.writeUTF(key);
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Disconnect during Cluck send", ex);
                try {
                    sock.close();
                } catch (IOException ex1) {
                    Logger.log(LogLevel.WARNING, "Socket error during disconnect", ex1);
                }
            }
        }
    }
}

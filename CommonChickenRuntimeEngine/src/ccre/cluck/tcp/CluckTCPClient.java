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
package ccre.cluck.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;
import ccre.concurrency.ReporterThread;
import ccre.net.ClientSocket;
import ccre.net.Network;

/**
 * A self-maintaining handler for connecting to a specified remote address.
 *
 * @author skeggsc
 */
public class CluckTCPClient extends ReporterThread {
    private static Logger logger = LoggerFactory.getLogger(CluckTCPClient.class);

    /**
     * The default port to run on.
     */
    public static final int DEFAULT_PORT = 80;
    /**
     * The CluckNode that this connection shares.
     */
    protected final CluckNode node;
    /**
     * The link name for this connection.
     */
    protected final String linkName;
    /**
     * The active remote socket.
     */
    private ClientSocket sock;
    /**
     * The connection remote address.
     */
    private String remote;
    /**
     * A short summary of the current error being experienced by this client.
     */
    private String errorSummary;
    /**
     * The delay between each connection to the server.
     */
    private int reconnectDelayMillis = 5000;
    /**
     * The hint for what the other end of the connection should call this link.
     */
    protected final String remoteNameHint;
    /**
     * Should this client continue running?
     */
    private volatile boolean isRunning = true;
    /**
     * Is this connection currently reconnecting?
     */
    private volatile boolean isReconnecting = false;
    /**
     * Reconnect deadline: we should be reconnecting again by this time,
     * hopefully.
     */
    private volatile long reconnectDeadline = 0;
    /**
     * Is this connection currently established?
     */
    private volatile boolean isEstablished = false;
    /**
     * Should this component log anything during normal operation?
     */
    private boolean logDuringNormalOperation = true;

    /**
     * Create a new CluckTCPClient connecting to the specified remote on the
     * default port, sharing the specified CluckNode, with the specified link
     * name and hint for what the other end should call this link.
     *
     * @param remote The remote address.
     * @param node The shared node.
     * @param linkName The link name.
     * @param remoteNameHint The hint for what the other end should call this
     * link.
     */
    public CluckTCPClient(String remote, CluckNode node, String linkName, String remoteNameHint) {
        super("cluckcli-" + remote);
        this.remote = remote;
        this.node = node;
        this.linkName = linkName;
        this.remoteNameHint = remoteNameHint;
    }

    /**
     * Set the remote address to this specified address.
     *
     * @param remote The remote address.
     */
    public void setRemote(String remote) {
        this.remote = remote;
        closeActiveConnectionIfAny();
    }

    /**
     * Get the remote address.
     *
     * @return the remote address.
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Set whether or not this component should log during normal operation.
     *
     * @param logDuringNormalOperation if logging should occur normally.
     */
    public void setLogDuringNormalOperation(boolean logDuringNormalOperation) {
        this.logDuringNormalOperation = logDuringNormalOperation;
    }

    /**
     * End the active connection and don't reconnect.
     */
    public void terminate() {
        isRunning = false;
        closeActiveConnectionIfAny();
    }

    /**
     * Set the delay between times when this client reconnects to the server.
     *
     * @param millis The positive integer of milliseconds to wait.
     * @throws IllegalArgumentException If millis &lt;= 0.
     */
    public void setReconnectDelay(int millis) throws IllegalArgumentException {
        if (millis <= 0) {
            throw new IllegalArgumentException("Reconnection delay must be >= 0.");
        }
        reconnectDelayMillis = millis;
    }

    private void closeActiveConnectionIfAny() {
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException ex) {
                logger.warn("IO Error while closing connection", ex);
            }
        }
    }

    @Override
    protected void threadBody() throws IOException, InterruptedException {
        try {
            while (isRunning) {
                long start = System.currentTimeMillis();
                if (logDuringNormalOperation) {
                    logger.debug("Connecting to " + remote + " at " + start);
                }
                String postfix = "";
                closeActiveConnectionIfAny();
                try {
                    postfix = tryConnection();
                } catch (Throwable ex) {
                    logger.error("Uncaught exception in network handler!", ex);
                }
                pauseBeforeSubsequentCycle(start, postfix);
            }
        } finally {
            isReconnecting = false;
            isEstablished = false;
            if (sock != null) {
                sock.close();
            }
        }
    }

    private void pauseBeforeSubsequentCycle(long start, String postfix) throws InterruptedException {
        reconnectDeadline = reconnectDelayMillis + start;
        long spent = System.currentTimeMillis() - start;
        long remaining = reconnectDelayMillis - spent;
        if (remaining > 0) {
            if (remaining > 500 && logDuringNormalOperation) {
                logger.debug("Waiting " + remaining + " milliseconds before reconnecting." + postfix);
            }
            Thread.sleep(remaining);
        }
    }

    private String tryConnection() {
        String postfix = "";
        this.errorSummary = null;
        try {
            try {
                isReconnecting = true;
                sock = Network.connectDynPort(remote, DEFAULT_PORT);
                DataInputStream din = new DataInputStream(sock.openInputStream());
                try {
                    DataOutputStream dout = new DataOutputStream(sock.openOutputStream());
                    isEstablished = true;
                    try {
                        CluckLink deny = doStart(din, dout, sock);
                        isReconnecting = false;
                        doMain(din, dout, sock, deny);
                    } finally {
                        dout.close();
                    }
                } finally {
                    din.close();
                }
            } catch (IOException ex) {
                boolean uhe = "java.net.UnknownHostException".equals(ex.getClass().getName());
                this.errorSummary = uhe ? "Unknown Host: " + ex.getMessage() : ex.getMessage();
                if (uhe || (ex.getMessage() != null &&
                        (ex.getMessage().startsWith("Remote server not available") || ex.getMessage().startsWith("Timed out while connecting")))) {
                    postfix = " (" + ex.getMessage() + ")";
                } else {
                    logger.warn("IO Error while handling connection", ex);
                }
            }
        } finally {
            isReconnecting = false;
            isEstablished = false;
        }
        return postfix;
    }

    protected CluckLink doStart(DataInputStream din, DataOutputStream dout, ClientSocket socket) throws IOException {
        CluckProtocol.handleHeader(din, dout, remoteNameHint);
        logger.debug("Connected to " + remote + " at " + System.currentTimeMillis());
        CluckProtocol.setTimeoutOnSocket(socket);
        CluckLink deny = CluckProtocol.handleSend(dout, linkName, node);
        node.notifyNetworkModified(); // Only send here, not on server.
        return deny;
    }

    protected void doMain(DataInputStream din, DataOutputStream dout, ClientSocket socket, CluckLink deny) throws IOException {
        CluckProtocol.handleRecv(din, linkName, node, deny);
    }

    /**
     * @return if this connection is currently reconnecting
     */
    public boolean isReconnecting() {
        return isReconnecting;
    }

    /**
     * Get the reconnect deadline - only really useful if isReconnecting() and
     * isEstablished() both return false.
     *
     * @return the reconnect deadline: by when we should be reconnecting again.
     */
    public long getReconnectDeadline() {
        return reconnectDeadline;
    }

    /**
     * @return if this connection is currently established
     */
    public boolean isEstablished() {
        return isEstablished;
    }

    /**
     * @return a short summary of the current error being experienced by this
     * client.
     */
    public String getErrorSummary() {
        return errorSummary;
    }
}

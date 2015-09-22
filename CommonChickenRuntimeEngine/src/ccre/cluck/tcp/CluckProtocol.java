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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;
import ccre.concurrency.ReporterThread;
import ccre.net.ClientSocket;

/**
 * A static utility class for handling various encodings of Cluck packets.
 *
 * @author skeggsc
 */
public class CluckProtocol {
    /**
     * NOTE: changing this will break compatibility with anything older than
     * CCRE v3!
     */
    static final int CURRENT_VERSION = 0;
    // also, it can only be, at most, 0xFF.

    /**
     * Milliseconds
     */
    static final int TIMEOUT_PERIOD = 600;
    
    /**
     * Milliseconds, should always be noticeably less than TIMEOUT_PERIOD
     */
    static final int KEEPALIVE_INTERVAL = 200;

    /**
     * Sets the appropriate timeout on sock, for disconnection reporting.
     *
     * @param sock the socket to modify.
     * @throws IOException if the timeout cannot be set.
     */
    public static void setTimeoutOnSocket(ClientSocket sock) throws IOException {
        sock.setSocketTimeout(TIMEOUT_PERIOD);
    }

    /**
     * Start a Cluck connection. Must be run from both ends of the connection.
     *
     * @param din The connection's input.
     * @param dout The connection's output.
     * @param remoteHint The hint for what the remote node should call this
     * link, or null for no recommendation.
     * @return What the remote node hints that this link should be called.
     * @throws IOException If an IO error occurs.
     */
    protected static String handleHeader(DataInputStream din, DataOutputStream dout, String remoteHint) throws IOException {
        dout.writeInt(0x154000CA | (CURRENT_VERSION << 8));
        Random r = new Random();
        int ra = r.nextInt(), rb = r.nextInt();
        dout.writeInt(ra);
        dout.writeInt(rb);
        int raw_magic = din.readInt();
        if ((raw_magic & 0xFFFF00FF) != 0x154000CA) {
            throw new IOException("Magic number did not match!");
        }
        int version = (raw_magic & 0x0000FF00) >> 8; // always in [0, 255]
        if (version < CURRENT_VERSION) {
            // The side with the higher version (if they differ) is responsible
            // for providing a transformer to be compatible with the older
            // version.
            // But so far, we're still on version 0, so this shouldn't happen!
            throw new IOException("Remote end is on an older version of Cluck! I don't quite know how to deal with this.");
        }
        dout.writeInt(din.readInt() ^ din.readInt());
        if (din.readInt() != (ra ^ rb)) {
            throw new IOException("Did not bounce properly!");
        }
        dout.writeUTF(remoteHint == null ? "" : remoteHint);
        String rh = din.readUTF();
        return rh.isEmpty() ? null : rh;
    }

    /**
     * Calculate a checksum from a basis and a byte array.
     *
     * @param data The data to checksum.
     * @param basis The basis initializer.
     * @return The checksum.
     */
    protected static long checksum(byte[] data, long basis) {
        long h = basis;
        for (int i = 0; i < data.length; i++) {
            h = 43 * h + data[i];
        }
        return h;
    }

    /**
     * Start a receive loop from the specified Connection input, link name,
     * node, and link to deny broadcasts to.
     *
     * @param din The connection input.
     * @param linkName The link name.
     * @param node The node to provide access to.
     * @param denyLink The link to deny transmits to, usually the link that
     * sends back to the other end of the connection. (To stop infinite loops)
     * @throws IOException If an IO error occurs
     */
    protected static void handleRecv(DataInputStream din, String linkName, CluckNode node, CluckLink denyLink) throws IOException {
        try {
            boolean expectKeepAlives = false;
            long lastReceive = System.currentTimeMillis();
            while (true) {
                try {
                    String dest = readNullableString(din);
                    String source = readNullableString(din);
                    byte[] data = new byte[din.readInt()];
                    long checksumBase = din.readLong();
                    din.readFully(data);
                    if (din.readLong() != checksum(data, checksumBase)) {
                        throw new IOException("Checksums did not match!");
                    }
                    if (!expectKeepAlives && "KEEPALIVE".equals(dest) && source == null && data.length >= 2 && data[0] == CluckNode.RMT_NEGATIVE_ACK && data[1] == 0x6D) {
                        expectKeepAlives = true;
                        LoggerFactory.getLogger(CluckProtocol.class).info("Detected KEEPALIVE message. Expecting future keepalives on " + linkName + ".");
                    }
                    source = prependLink(linkName, source);
                    long start = System.currentTimeMillis();
                    node.transmit(dest, source, data, denyLink);
                    long endAt = System.currentTimeMillis();
                    if (endAt - start > 1000) {
                        LoggerFactory.getLogger(CluckProtocol.class).warn("[LOCAL] Took a long time to process: " + dest + " <- " + source + " of " + (endAt - start) + " ms");
                    }
                    lastReceive = System.currentTimeMillis();
                } catch (SocketTimeoutException ex) {
                    if (expectKeepAlives && System.currentTimeMillis() - lastReceive > TIMEOUT_PERIOD) {
                        throw ex;
                    } else {
                        // otherwise, don't do anything - we don't know if this
                        // is a timeout.
                    }
                }
            }
        } catch (SocketTimeoutException ex) {
            LoggerFactory.getLogger(CluckProtocol.class).debug("Link timed out: " + linkName);
        } catch (SocketException ex) {
            if ("Connection reset".equals(ex.getMessage())) {
                LoggerFactory.getLogger(CluckProtocol.class).debug("Link receiving disconnected: " + linkName);
            } else {
                throw ex;
            }
        }
    }

    static String readNullableString(DataInputStream din) throws IOException {
        String out = din.readUTF();
        return out.isEmpty() ? null : out;
    }

    static String prependLink(String linkName, String source) {
        return source == null ? linkName : linkName + "/" + source;
    }

    /**
     * Create and register a cluck link using the specified connection output,
     * link name, and node to get messages from.
     *
     * Returns the newly created link so that it can be denied from future
     * receives.
     *
     * @param dout The connection output.
     * @param linkName The link name.
     * @param node The node to provide access to.
     * @return The newly created link.
     */
    protected static CluckLink handleSend(final DataOutputStream dout, final String linkName, CluckNode node) {
        final LinkedList<SendableEntry> queue = new LinkedList<SendableEntry>();
        final ReporterThread main = new CluckSenderThread("Cluck-Send-" + linkName, queue, dout);
        main.start();
        CluckLink clink = new CluckLink() {
            private Logger logger = LoggerFactory.getLogger(CluckProtocol.class);

            private boolean isRunning = false;

            public synchronized boolean send(String dest, String source, byte[] data) {
                if (isRunning) {
                    logger.error("[LOCAL] Already running transmit!");
                    return true;
                }
                isRunning = true;
                try {
                    int size;
                    synchronized (queue) {
                        queue.addLast(new SendableEntry(source, dest, data));
                        queue.notifyAll();
                        size = queue.size();
                    }
                    Thread.yield();
                    if (size > 75) {
                        logger.warn("[LOCAL] Queue too long: " + size + " for " + dest + " at " + System.currentTimeMillis());
                    }
                } finally {
                    isRunning = false;
                }
                return main.isAlive();
            }
        };
        node.addOrReplaceLink(clink, linkName);
        return clink;
    }

    private CluckProtocol() {
    }

    /**
     * Stored in a queue of the messages that need to be sent over a connection.
     *
     * @author skeggsc
     */
    private static class SendableEntry {

        /**
         * The sender of this message.
         */
        public final String src;
        /**
         * The receiver of this message.
         */
        public final String dst;
        /**
         * The contents of this message.
         */
        public final byte[] data;

        /**
         * Create a new SendableEntry with the specified attributes.
         *
         * @param src The source of the message.
         * @param dst The destination of the message.
         * @param data The contents of the message.
         */
        SendableEntry(String src, String dst, byte[] data) {
            super();
            this.src = src;
            this.dst = dst;
            this.data = data;
        }

        @Override
        public String toString() {
            return "[" + src + "->" + dst + "#" + data.length + "]";
        }
    }

    private static class CluckSenderThread extends ReporterThread {
        private static Logger logger = LoggerFactory.getLogger(CluckSenderThread.class);

        private final LinkedList<SendableEntry> queue;
        private final DataOutputStream dout;

        CluckSenderThread(String name, LinkedList<SendableEntry> queue, DataOutputStream dout) {
            super(name);
            this.queue = queue;
            this.dout = dout;
        }

        @Override
        protected void threadBody() throws InterruptedException {
            try {
                while (true) {
                    long nextKeepAlive = System.currentTimeMillis() + KEEPALIVE_INTERVAL;
                    SendableEntry ent;
                    synchronized (queue) {
                        while (queue.isEmpty() && System.currentTimeMillis() < nextKeepAlive) {
                            queue.wait(200);
                        }
                        if (queue.isEmpty()) {
                            // Send a "keep-alive" message. RMT_NEGATIVE_ACK
                            // will never be complained about, so it works.
                            ent = new SendableEntry(null, "KEEPALIVE", new byte[] { CluckNode.RMT_NEGATIVE_ACK, 0x6D });
                        } else {
                            ent = queue.removeFirst();
                        }
                    }
                    String source = ent.src, dest = ent.dst;
                    byte[] data = ent.data;
                    dout.writeUTF(dest == null ? "" : dest);
                    dout.writeUTF(source == null ? "" : source);
                    dout.writeInt(data.length);
                    long begin = (((long) data.length) << 32) ^ (dest == null ? 0 : ((long) dest.hashCode()) << 16) ^ (source == null ? 0 : source.hashCode() ^ (((long) source.hashCode()) << 48));
                    dout.writeLong(begin);
                    dout.write(data);
                    dout.writeLong(checksum(data, begin));
                }
            } catch (IOException ex) {
                logger.warn("Bad IO in " + this + ": " + ex);
            }
        }
    }
}

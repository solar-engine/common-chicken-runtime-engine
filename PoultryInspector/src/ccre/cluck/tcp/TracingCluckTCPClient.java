/*
 * Copyright 2015 Colby Skeggs
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

import ccre.cluck.CluckLink;
import ccre.cluck.CluckConstants;
import ccre.cluck.CluckNode;
import ccre.drivers.ByteFiddling;
import ccre.log.Logger;
import ccre.net.ClientSocket;

/**
 * A CluckTCPClient that traces all traffic that goes through it, for debugging
 * purposes.
 *
 * @author skeggsc
 */
public class TracingCluckTCPClient extends CluckTCPClient {

    /**
     * A CluckLink that wraps another CluckLink and traces everything that gets
     * sent through it.
     *
     * @author skeggsc
     */
    public static class TracingLink implements CluckLink {

        private final CluckLink link;

        /**
         * Create a new TracingLink to wrap an existing CluckLink.
         *
         * @param link the link to wrap.
         */
        public TracingLink(CluckLink link) {
            this.link = link;
        }

        @Override
        public boolean send(String dest, String source, byte[] data) {
            if (data.length == 0) {
                Logger.finest("[LOCAL] SEND " + source + " -> " + dest + ": EMPTY");
            } else if (data.length == 1) {
                Logger.finest("[LOCAL] SEND " + source + " -> " + dest + ": " + CluckConstants.rmtToString(data[0]));
            } else {
                Logger.finest("[LOCAL] SEND " + source + " -> " + dest + ": " + CluckConstants.rmtToString(data[0]) + " <" + ByteFiddling.toHex(data, 1, data.length) + ">");
            }
            return link.send(dest, source, data);
        }
    }

    /**
     * Create a new TracingCluckTCPClient connecting to the specified remote on
     * the default port, sharing the specified CluckNode, with the specified
     * link name and hint for what the other end should call this link.
     *
     * @param remote The remote address.
     * @param node The shared node.
     * @param linkName The link name.
     * @param remoteNameHint The hint for what the other end should call this
     * link.
     */
    public TracingCluckTCPClient(String remote, CluckNode node, String linkName, String remoteNameHint) {
        super(remote, node, linkName, remoteNameHint);
    }

    @Override
    protected CluckLink doStart(DataInputStream din, DataOutputStream dout, ClientSocket sock) throws IOException {
        CluckProtocol.handleHeader(din, dout, remoteNameHint);
        Logger.fine("Connected to " + getRemote() + " at " + System.currentTimeMillis());
        CluckProtocol.setTimeoutOnSocket(sock);
        CluckLink link = CluckProtocol.handleSend(dout, linkName, node);
        link = new TracingLink(link);
        node.addOrReplaceLink(link, linkName);
        node.notifyNetworkModified();// Only send here, not on server.
        return link;
    }

    @Override
    protected void doMain(DataInputStream din, DataOutputStream dout, ClientSocket sock, CluckLink denyLink) throws IOException {
        try {
            boolean expectKeepAlives = false;
            long lastReceive = System.currentTimeMillis();
            while (true) {
                try {
                    String dest = CluckProtocol.readNullableString(din);
                    String source = CluckProtocol.readNullableString(din);
                    byte[] data = new byte[din.readInt()];
                    long checksumBase = din.readLong();
                    din.readFully(data);
                    if (din.readLong() != CluckProtocol.checksum(data, checksumBase)) {
                        throw new IOException("Checksums did not match!");
                    }
                    if (!expectKeepAlives && "KEEPALIVE".equals(dest) && source == null && data.length >= 2 && data[0] == CluckConstants.RMT_NEGATIVE_ACK && data[1] == 0x6D) {
                        expectKeepAlives = true;
                        Logger.info("Detected KEEPALIVE message. Expecting future keepalives on " + linkName + ".");
                    }
                    source = CluckProtocol.prependLink(linkName, source);
                    long start = System.currentTimeMillis();
                    logLocal(dest, source, data);
                    node.transmit(dest, source, data, denyLink);
                    long endAt = System.currentTimeMillis();
                    if (endAt - start > 1000) {
                        Logger.warning("[LOCAL] Took a long time to process: " + dest + " <- " + source + " of " + (endAt - start) + " ms");
                    }
                    lastReceive = System.currentTimeMillis();
                } catch (SocketTimeoutException ex) {
                    if (expectKeepAlives && System.currentTimeMillis() - lastReceive > CluckProtocol.TIMEOUT_PERIOD) {
                        throw ex;
                    } else {
                        // otherwise, don't do anything - we don't know if this
                        // is a timeout.
                    }
                }
            }
        } catch (SocketTimeoutException ex) {
            Logger.fine("Link timed out: " + linkName);
        } catch (SocketException ex) {
            if ("Connection reset".equals(ex.getMessage())) {
                Logger.fine("Link receiving disconnected: " + linkName);
            } else {
                throw ex;
            }
        }
    }

    private void logLocal(String dest, String source, byte[] data) {
        if (data.length == 0) {
            Logger.finest("[LOCAL] RECV " + source + " -> " + dest + ": EMPTY");
        } else if (data.length == 1) {
            Logger.finest("[LOCAL] RECV " + source + " -> " + dest + ": " + CluckConstants.rmtToString(data[0]));
        } else if (!dest.equals("KEEPALIVE")) {
            Logger.finest("[LOCAL] RECV: " + data.length);
            Logger.finest("[LOCAL] RECV " + source + " -> " + dest + ": " + CluckConstants.rmtToString(data[0]) + " <" + ByteFiddling.toHex(data, 1, data.length) + ">");
        }
    }
}

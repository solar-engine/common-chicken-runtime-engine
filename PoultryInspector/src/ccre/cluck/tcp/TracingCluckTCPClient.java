package ccre.cluck.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;
import ccre.cluck.tcp.CluckProtocol;
import ccre.cluck.tcp.CluckTCPClient;
import ccre.drivers.ByteFiddling;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.Network;

public class TracingCluckTCPClient extends CluckTCPClient {

    public class TracingLink implements CluckLink {

        private final CluckLink link;

        public TracingLink(CluckLink link) {
            this.link = link;
        }

        public boolean send(String dest, String source, byte[] data) {
            if (data.length == 0) {
                Logger.finest("[LOCAL] SEND " + source + " -> " + dest + ": EMPTY");
            } else if (data.length == 1) {
                Logger.finest("[LOCAL] SEND " + source + " -> " + dest + ": " + CluckNode.rmtToString(data[0]));
            } else {
                Logger.finest("[LOCAL] SEND " + source + " -> " + dest + ": " + CluckNode.rmtToString(data[0]) + " <" + ByteFiddling.toHex(data, 1, data.length) + ">");
            }
            return link.send(dest, source, data);
        }
    }

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
        node.notifyNetworkModified(); // Only send here, not on server.
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
                    if (!expectKeepAlives && "KEEPALIVE".equals(dest) && source == null && data.length >= 2 && data[0] == CluckNode.RMT_NEGATIVE_ACK && data[1] == 0x6D) {
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
                } catch (IOException ex) {
                    if ((expectKeepAlives && System.currentTimeMillis() - lastReceive > CluckProtocol.TIMEOUT_PERIOD) || !Network.isTimeoutException(ex)) {
                        throw ex;
                    }
                }
            }
        } catch (IOException ex) {
            if (ex.getClass().getName().equals("java.net.SocketException") && ex.getMessage().equals("Connection reset")) {
                Logger.fine("Link receiving disconnected: " + linkName);
            } else if (Network.isTimeoutException(ex)) {
                Logger.fine("Link timed out: " + linkName);
            } else {
                throw ex;
            }
        }
    }

    private void logLocal(String dest, String source, byte[] data) {
        if (data.length == 0) {
            Logger.finest("[LOCAL] RECV " + source + " -> " + dest + ": EMPTY");
        } else if (data.length == 1) {
            Logger.finest("[LOCAL] RECV " + source + " -> " + dest + ": " + CluckNode.rmtToString(data[0]));
        } else if (!dest.equals("KEEPALIVE")) {
            Logger.finest("[LOCAL] RECV: " + data.length);
            Logger.finest("[LOCAL] RECV " + source + " -> " + dest + ": " + CluckNode.rmtToString(data[0]) + " <" + ByteFiddling.toHex(data, 1, data.length) + ">");
        }
    }
}

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
package ccre.cluck;

import ccre.log.LogLevel;
import ccre.log.Logger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * A static utility class for handling various encodings of Cluck packets.
 *
 * @author skeggsc
 */
public class CluckProtocol {

    /**
     * Start a Cluck connection. Must be ran from both ends of the connection.
     *
     * @param din The connection's input.
     * @param dout The connection's output.
     * @param remoteHint The hint for what the remote node should call this
     * link, or null for no recommendation.
     * @return What the remote node hints that this link should be called.
     * @throws IOException If an IO error occurs.
     */
    public static String handleHeader(DataInputStream din, DataOutputStream dout, String remoteHint) throws IOException {
        dout.writeInt(0x154000CA);
        Random r = new Random();
        int ra = r.nextInt(), rb = r.nextInt();
        dout.writeInt(ra);
        dout.writeInt(rb);
        if (din.readInt() != 0x154000CA) {
            throw new IOException("Magic number did not match!");
        }
        dout.writeInt(din.readInt() ^ din.readInt());
        if (din.readInt() != (ra ^ rb)) {
            throw new IOException("Did not bounce properly!");
        }
        if (remoteHint == null) {
            remoteHint = "";
        }
        dout.writeUTF(remoteHint);
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
    public static long checksum(byte[] data, long basis) {
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
    public static void handleRecv(DataInputStream din, String linkName, CluckNode node, CluckLink denyLink) throws IOException {
        try {
            while (true) {
                String dest = din.readUTF();
                if (dest.length() == 0) {
                    dest = null;
                }
                String source = din.readUTF();
                if (source.length() == 0) {
                    source = null;
                }
                int len = din.readInt();
                if (len > 64 * 1024) {
                    Logger.warning("Received packet of over 64 KB (" + source + " -> " + dest + ")");
                }
                byte[] data = new byte[len];
                long begin = din.readLong();
                din.readFully(data);
                long check = checksum(data, begin);
                long end = din.readLong();
                if (end != check) {
                    throw new IOException("Checksums did not match!");
                }
                if (source == null) {
                    source = linkName;
                } else {
                    source = linkName + "/" + source;
                }
                node.transmit(dest, source, data, denyLink);
            }
        } catch (IOException ex) {
            if (ex.getClass().getName().equals("java.net.SocketException") && ex.getMessage().equals("Connection reset")) {
                Logger.fine("Link receiving disconnected: " + linkName);
            } else {
                throw ex;
            }
        }
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
    public static CluckLink handleSend(final DataOutputStream dout, final String linkName, CluckNode node) {
        CluckLink clink = new CluckLink() {
            private boolean isRunning = false;

            public synchronized boolean transmit(String dest, String source, byte[] data) {
                if (isRunning) {
                    System.err.println("Already running transmit!");
                    return true;
                }
                isRunning = true;
                try {
                    try {
                        if (dest == null) {
                            dout.writeUTF("");
                        } else {
                            dout.writeUTF(dest);
                        }
                        if (source == null) {
                            dout.writeUTF("");
                        } else {
                            dout.writeUTF(source);
                        }
                        dout.writeInt(data.length);
                        long begin = (((long) data.length) << 32) ^ (dest == null ? 0 : ((long) dest.hashCode()) << 16) ^ (source == null ? 0 : source.hashCode() ^ (((long) source.hashCode()) << 48));
                        dout.writeLong(begin);
                        dout.write(data);
                        dout.writeLong(checksum(data, begin));
                        return true;
                    } catch (IOException ex) {
                        if (ex.getClass().getName().equals("java.net.SocketException") && ex.getMessage().equals("Socket closed")) {
                            Logger.fine("Link sending disconnected: " + linkName);
                        } else {
                            Logger.log(LogLevel.SEVERE, "Could not transmit over cluck connection", ex);
                        }
                        return false;
                    }
                } finally {
                    isRunning = false;
                }
            }
        };
        node.addOrReplaceLink(clink, linkName);
        return clink;
    }
}

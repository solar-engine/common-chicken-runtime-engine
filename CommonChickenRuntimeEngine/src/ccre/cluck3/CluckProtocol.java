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
package ccre.cluck3;

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

    public static void handleHeader(DataInputStream din, DataOutputStream dout) throws IOException {
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
    }

    public static long checksum(byte[] data, long basis) {
        long h = basis;
        for (int i = 0; i < data.length; i++) {
            h = 43 * h + data[i];
        }
        return h;
    }

    public static void handleRecv(DataInputStream din, String linkNamePrefix, CluckNode node) throws IOException {
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
                source = linkNamePrefix;
            } else {
                source = linkNamePrefix + "." + source;
            }
            node.transmit(dest, source, data);
        }
    }

    public static void handleSend(final DataOutputStream dout, String linkName, CluckNode node) {
        node.addOrReplaceLink(new CluckLink() {
            public synchronized boolean transmit(String rest, String source, byte[] data) {
                try {
                    dout.writeUTF(rest);
                    dout.writeUTF(source);
                    dout.writeInt(data.length);
                    long begin = (((long)data.length) << 32) ^ (((long)rest.hashCode()) << 16) ^ source.hashCode() ^ (((long)source.hashCode()) << 48);
                    dout.writeLong(begin);
                    dout.write(data);
                    dout.writeLong(checksum(data, begin));
                    return true;
                } catch (IOException ex) {
                    Logger.log(LogLevel.SEVERE, "Could not transmit over cluck connection!", ex);
                    return false;
                }
            }
        }, linkName);
    }
}

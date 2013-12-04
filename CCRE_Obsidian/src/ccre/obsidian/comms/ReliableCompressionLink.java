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
package ccre.obsidian.comms;

import ccre.log.Logger;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * A link that allows for compression of message headers.
 *
 * @author skeggsc
 */
public abstract class ReliableCompressionLink extends ReliableLink {

    public static final int recentCount = 255;
    private final String[] sendRecentDests = new String[recentCount];
    private final String[] sendRecentSources = new String[recentCount];
    private final String[] recvRecentDests = new String[recentCount];
    private final String[] recvRecentSources = new String[recentCount];
    private final Random unreliableElevationRandom = new Random();

    @Override
    protected synchronized void handleReliableSendingReset() {
        for (int i = 0; i < recentCount; i++) {
            sendRecentDests[i] = null;
            sendRecentSources[i] = null;
        }
    }

    @Override
    protected synchronized void handleReliableReceivingReset() {
        for (int i = 0; i < recentCount; i++) {
            recvRecentDests[i] = null;
            recvRecentSources[i] = null;
        }
    }

    @Override
    protected synchronized final void reliableReceive(byte[] packet, int offset, int count) {
        ByteBuffer out = ByteBuffer.wrap(packet, offset, count);
        int udest = out.get();
        int usource = out.get();
        byte[] data = new byte[out.getShort()];
        String dest, source;
        if (udest != -1) {
            dest = recvRecentDests[udest & 0xff];
        } else {
            byte[] dbytes = new byte[out.get()];
            out.get(dbytes);
            dest = new String(dbytes);
        }
        for (int i = 1; i < recvRecentDests.length; i++) {
            recvRecentDests[i - 1] = recvRecentDests[i];
        }
        recvRecentDests[recvRecentDests.length - 1] = dest;
        if (usource != -1) {
            source = recvRecentSources[usource & 0xff];
        } else {
            byte[] sbytes = new byte[out.get()];
            out.get(sbytes);
            source = new String(sbytes);
        }
        for (int i = 1; i < recvRecentSources.length; i++) {
            recvRecentSources[i - 1] = recvRecentSources[i];
        }
        recvRecentSources[recvRecentSources.length - 1] = source;
        out.get(data);
        Logger.config("C-RECV " + dest + " " + source + " " + data + "[" + data.length + "]");
        compressionReceive(dest, source, data);
    }

    @Override
    protected synchronized final void unreliableReceive(byte[] packet, int offset, int count) {
        ByteBuffer out = ByteBuffer.wrap(packet, offset, count);
        short dh = out.getShort();
        short sh = out.getShort();
        byte[] data = new byte[out.getShort()];
        out.get(data);
        String dest = null;
        for (int i = 0; i < recvRecentDests.length; i++) {
            if (recvRecentDests[i] != null && dh == (short) recvRecentDests[i].hashCode()) {
                if (dest != null && !dest.equals(recvRecentDests[i])) {
                    Logger.warning("Lost packet due to hashCode collision: " + dest + " and " + recvRecentDests[i]);
                    return;
                }
                dest = recvRecentDests[i];
            }
        }
        if (dest == null) {
            StringBuilder r = new StringBuilder();
            for (String c : recvRecentDests) {
                if (c != null) {
                    r.append("(" + c + "=" + (short) c.hashCode() + ")");
                }
            }
            Logger.warning("Lost packet due to dest hashCode lookup failure: " + dh + " in " + r);
            return;
        }
        String source = null;
        for (int i = 0; i < recvRecentSources.length; i++) {
            if (recvRecentSources[i] != null && sh == (short) recvRecentSources[i].hashCode()) {
                if (source != null && !source.equals(recvRecentSources[i])) {
                    Logger.warning("Lost packet due to hashCode collision: " + source + " and " + recvRecentSources[i]);
                    return;
                }
                source = recvRecentSources[i];
            }
        }
        if (source == null) {
            Logger.warning("Lost packet due to source hashCode lookup failure: " + sh);
            return;
        }
        Logger.config("R-RECV " + dest + " " + source + " " + data + "[" + data.length + "]");
        compressionReceive(dest, source, data);
    }

    public synchronized final void compressionUnreliableTransmit(String dest, String source, byte[] data) throws InterruptedException {
        Logger.config("R-SEND " + dest + " " + source + " " + data + "[" + data.length + "]");
        if (data.length != (short) data.length) {
            Logger.warning("Lost packet due to data too long: " + data.length);
            return;
        }
        if (unreliableElevationRandom.nextInt(10) > 1) { // Randomly use reliable instead to make sure that names get across
            int foundId_D = -1;
            for (int i = 0; i < sendRecentDests.length; i++) {
                if (sendRecentDests[i] == null) {
                    continue;
                }
                if (dest.equals(sendRecentDests[i])) {
                    foundId_D = i;
                } else if ((short) dest.hashCode() == (short) sendRecentDests[i].hashCode()) {
                    foundId_D = -1;
                    break;
                }
            }
            if (foundId_D != -1) {
                int foundId_S = -1;
                for (int i = 0; i < sendRecentSources.length; i++) {
                    if (sendRecentSources[i] == null) {
                        continue;
                    }
                    if (source.equals(sendRecentSources[i])) {
                        foundId_S = i;
                    } else if ((short) source.hashCode() == (short) sendRecentSources[i].hashCode()) {
                        foundId_S = -1;
                        break;
                    }
                }
                if (foundId_S != -1) {
                    // Can unreliably transmit!
                    ByteBuffer out = ByteBuffer.allocate(6 + data.length); // desthash as short + sourcehash as short + data length as short + data
                    out.putShort((short) dest.hashCode());
                    out.putShort((short) source.hashCode());
                    out.putShort((short) data.length);
                    out.put(data);
                    if (out.remaining() > 0) {
                        throw new RuntimeException("Wait, what?");
                    }
                    this.blockingUnreliableTransmit(out.array(), 0, out.capacity());
                    return;
                }
            }
        }
        // Send as reliable if it can't easily be sent unreliably
        compressionTransmit(dest, source, data);
    }

    public synchronized final void compressionTransmit(String dest, String source, byte[] data) throws InterruptedException {
        Logger.config("C-SEND " + dest + " " + source + " " + data + "[" + data.length + "]");
        int rlen = 4 + data.length; // destid + sourceid + 2 bytes of data length + data
        if (data.length != (short) data.length) {
            Logger.warning("Lost packet due to data too long: " + data.length);
            return;
        }
        if (dest == null) {
            dest = "";
        }
        int udest = -1;
        for (int i = 0; i < sendRecentDests.length; i++) {
            if (dest.equals(sendRecentDests[i])) {
                udest = i;
            }
        }
        byte[] dbytes = null;
        if (udest == -1) {
            dbytes = dest.getBytes();
            if (dbytes.length != (byte) dbytes.length) {
                Logger.warning("Lost packet due to destination address too long: " + dest);
            }
            rlen += 1 + dbytes.length;
        }
        for (int i = 1; i < sendRecentDests.length; i++) {
            sendRecentDests[i - 1] = sendRecentDests[i];
        }
        sendRecentDests[sendRecentDests.length - 1] = dest;
        if (source == null) {
            source = "";
        }
        int usource = -1;
        for (int i = 0; i < sendRecentSources.length; i++) {
            if (source.equals(sendRecentSources[i])) {
                usource = i;
            }
        }
        byte[] sbytes = null;
        if (usource == -1) {
            sbytes = source.getBytes();
            if (sbytes.length != (byte) sbytes.length) {
                Logger.warning("Lost packet due to source address too long: " + source);
            }
            rlen += 1 + sbytes.length;
        }
        for (int i = 1; i < sendRecentSources.length; i++) {
            sendRecentSources[i - 1] = sendRecentSources[i];
        }
        sendRecentSources[sendRecentSources.length - 1] = source;
        ByteBuffer out = ByteBuffer.allocate(rlen);
        out.put((byte) udest);
        out.put((byte) usource);
        out.putShort((short) data.length);
        if (dbytes != null) {
            out.put((byte) dbytes.length);
            out.put(dbytes);
        }
        if (sbytes != null) {
            out.put((byte) sbytes.length);
            out.put(sbytes);
        }
        out.put(data);
        if (out.remaining() > 0) {
            throw new RuntimeException("Wait, what?");
        }
        this.blockingReliableTransmit(out.array(), 0, rlen);
    }

    protected abstract void compressionReceive(String dest, String source, byte[] data);
}

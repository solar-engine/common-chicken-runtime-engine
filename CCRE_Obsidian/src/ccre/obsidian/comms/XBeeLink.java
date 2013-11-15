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

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;
import ccre.concurrency.ReporterThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A Cluck Link running over an XBee radio.
 *
 * @author skeggsc
 */
public class XBeeLink implements CluckLink, PacketListener {

    // TODO: Add sequence markers to XBee link!
    private final int[] remote;
    private final String linkName;
    private final CluckNode node;
    private final BlockingQueue<int[]> dataQueue = new LinkedBlockingQueue<int[]>();

    public XBeeLink(final XBeeRadio radio, final int[] remote, String linkName, CluckNode node, final int subTimeout, final int timeout) {
        this.remote = remote;
        radio.addPacketListener(this);
        this.linkName = linkName;
        this.node = node;
        new ReporterThread("XBeeLink-Transmitter") {
            @Override
            protected void threadBody() throws Throwable {
                while (true) {
                    int[] take = dataQueue.take();
                    try {
                        radio.sendPacketVerified(remote, take, subTimeout, timeout);
                    } catch (XBeeException ex) {
                        Logger.log(LogLevel.WARNING, "Could not transmit packet to remote XBee!", ex);
                    }
                }
            }
        }.start();
    }

    public void addToNode() {
        node.addLink(this, linkName);
    }

    @Override
    public synchronized boolean transmit(String dest, String source, byte[] data) {
        if (dest == null) {
            dest = "";
        }
        if (source == null) {
            source = "";
        }
        byte[] dchars = dest.getBytes();
        byte[] schars = source.getBytes();
        if ((short) schars.length != schars.length || (short) dchars.length != dchars.length || (data.length & 0xffff) != data.length) {
            Logger.warning("Dropping packet from " + source + " to " + dest + " because of invalid segment length!");
            return true;
        }
        ByteBuffer bout = ByteBuffer.allocate(8 + dchars.length + schars.length + data.length);
        bout.putShort((short) dchars.length);
        bout.putShort((short) schars.length);
        bout.putShort((short) data.length);
        bout.putShort((short) 0xDEAD); // Placeholder checksum
        int cursum = ((data.length << 16) ^ (data.length >> 16));
        if (dest.isEmpty()) {
            cursum ^= 17;
        } else {
            bout.put(dchars);
            cursum += dest.hashCode();
            cursum ^= dest.length();
        }
        if (source.isEmpty()) {
            cursum ^= 10;
        } else {
            bout.put(schars);
            cursum += source.hashCode();
            cursum ^= source.length();
        }
        bout.put(data);
        cursum ^= Arrays.hashCode(data) - data.length;
        bout.putShort(6, ((short) ((cursum >> 16) ^ cursum)));
        if (bout.remaining() > 0) {
            throw new RuntimeException("Wait, what?");
        }
        int[] outarray = new int[bout.position()];
        for (int i = 0; i < outarray.length; i++) {
            outarray[i] = bout.get(i);
        }
        dataQueue.add(outarray);
        return true;
    }

    @Override
    public void processResponse(XBeeResponse pkt) {
        if (pkt instanceof ZNetRxResponse) { // TODO: Compress common destinations and sources
            ZNetRxResponse zp = (ZNetRxResponse) pkt;
            int[] raddr = zp.getRemoteAddress64().getAddress();
            if (!Arrays.equals(raddr, remote)) {
                Logger.log(LogLevel.WARNING, "Dropped packet from bad remote: " + Arrays.toString(raddr));
                return;
            }
            int[] input = zp.getData();
            byte[] realindata = new byte[input.length];
            for (int i = 0; i < input.length; i++) {
                realindata[i] = (byte) input[i];
            }
            ByteBuffer bin = ByteBuffer.wrap(realindata);
            try {
                short dlen = bin.getShort();
                short slen = bin.getShort();
                int len = bin.getShort();
                short checksum = bin.getShort();
                int cursum = ((len << 16) ^ (len >> 16));
                int co = 2;
                String dest;
                if (dlen == 0) {
                    dest = null;
                    cursum ^= 17;
                } else {
                    byte[] strbytes = new byte[dlen];
                    bin.get(strbytes, 0, dlen);
                    dest = new String(strbytes);
                    cursum += dest.hashCode();
                    cursum ^= dlen;
                }
                String source;
                if (slen == 0) {
                    source = null;
                    cursum ^= 10;
                } else {
                    byte[] strbytes = new byte[slen];
                    bin.get(strbytes);
                    source = new String(strbytes);
                    cursum += source.hashCode();
                    cursum ^= slen;
                }
                byte[] data = new byte[len];
                bin.get(data);
                cursum ^= Arrays.hashCode(data) - len;
                if (source == null) {
                    source = linkName;
                } else {
                    source = linkName + "/" + source;
                }
                if ((short) ((cursum >> 16) ^ cursum) != checksum) {
                    Logger.warning("Dropped packet from " + source + " to " + dest + " because of checksum mismatch!");
                    return;
                }
                if (bin.remaining() > 0) {
                    Logger.warning("Packet from " + source + " to " + dest + " had incorrect (but non-erroneous) length.");
                }
                node.transmit(dest, source, data, this);
            } catch (ArrayIndexOutOfBoundsException ex) {
                Logger.log(LogLevel.WARNING, "Error while parsing XBee Cluck message", ex);
            }
        } else if (!(pkt instanceof ZNetTxStatusResponse)) {
            Logger.warning("Unknown XBee Response packet: " + pkt + " : " + pkt.getClass());
        }
    }
}

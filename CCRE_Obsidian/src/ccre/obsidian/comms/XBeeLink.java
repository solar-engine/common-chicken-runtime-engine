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
import ccre.log.LogLevel;
import ccre.log.Logger;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;

/**
 * A Cluck Link running over an XBee radio.
 *
 * @author skeggsc
 */
public class XBeeLink implements CluckLink, PacketListener {

    private final XBeeRadio radio;
    private final int[] remote;
    private final String linkName;
    private final CluckNode node;
    private final int subTimeout;
    private final int timeout;

    public XBeeLink(XBeeRadio radio, int[] remote, String linkName, CluckNode node, int subTimeout, int timeout) {
        this.radio = radio;
        this.remote = remote;
        radio.addPacketListener(this);
        this.linkName = linkName;
        this.node = node;
        this.subTimeout = subTimeout;
        this.timeout = timeout;
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
        if ((short) source.length() != source.length() || (short) dest.length() != dest.length() || (data.length & 0xffff) != data.length) {
            Logger.warning("Dropping packet from " + source + " to " + dest + " because of invalid segment length!");
            return true;
        }
        int[] message = new int[2 + (dest.length() + 3) / 4 + (source.length() + 3) / 4 + (data.length + 3) / 4];
        message[0] = (source.length() << 16) | (dest.length() & 0xffff);
        // message[1] set later
        int cursum = ((data.length << 16) ^ (data.length >> 16));
        int co = 2;
        if (dest.isEmpty()) {
            cursum ^= 17;
        } else {
            for (int i = 0; i < dest.length(); i++) {
                message[co + (i / 4)] |= (dest.charAt(i) & 0xff) << (8 * (i & 3));
            }
            co += (dest.length() + 3) / 4;
            cursum += dest.hashCode();
            cursum ^= dest.length();
        }
        if (source.isEmpty()) {
            cursum ^= 10;
        } else {
            for (int i = 0; i < source.length(); i++) {
                message[co + (i / 4)] |= (source.charAt(i) & 0xff) << (8 * (i & 3));
            }
            co += (source.length() + 3) / 4;
            cursum += source.hashCode();
            cursum ^= source.length();
        }
        cursum += source.hashCode();
        cursum ^= source.length();
        for (int i = 0; i < data.length; i++) {
            message[co + (i / 4)] |= (data[i] & 0xff) << (8 * (i & 3));
        }
        for (int i=co; i<message.length; i++) {
            cursum -= message[i];
            cursum ^= data.length - i;
        }
        co += (data.length + 3) / 4;
        if (co != message.length) {
            throw new RuntimeException("Wait, what?");
        }
        message[1] = (((short) ((cursum >> 16) ^ cursum)) << 16) | data.length;
        try {
            radio.sendPacketVerified(remote, message, subTimeout, timeout);
        } catch (XBeeException ex) {
            Logger.log(LogLevel.WARNING, "Could not transmit packet to remote XBee!", ex);
        }
        return true;
    }

    @Override
    public void processResponse(XBeeResponse pkt) {
        if (pkt instanceof ZNetRxResponse) { // TODO: Compress common destinations and sources
            ZNetRxResponse zp = (ZNetRxResponse) pkt;
            int[] indata = zp.getData();
            try {
                short dlen = (short) indata[0];
                short slen = (short) (indata[0] >> 16);
                int len = indata[1] & 0xffff;
                short checksum = (short) (indata[1] >> 16);
                int cursum = ((len << 16) ^ (len >> 16));
                int co = 2;
                String dest;
                if (dlen == 0) {
                    dest = null;
                    cursum ^= 17;
                } else {
                    char[] ddata = new char[(dlen + 3) & ~3];
                    int i;
                    for (i = 0; i < dlen; i += 4) {
                        int cv = indata[co++];
                        ddata[i] = (char) (cv & 0xff);
                        ddata[i + 1] = (char) ((cv >> 8) & 0xff);
                        ddata[i + 2] = (char) ((cv >> 16) & 0xff);
                        ddata[i + 3] = (char) ((cv >> 24) & 0xff);
                    }
                    dest = new String(ddata, 0, dlen);
                    cursum += dest.hashCode();
                    cursum ^= dlen;
                }
                String source;
                if (slen == 0) {
                    source = null;
                    cursum ^= 10;
                } else {
                    char[] sdata = new char[(slen + 3) & ~3];
                    int i;
                    for (i = 0; i < slen; i += 4) {
                        int cv = indata[co++];
                        sdata[i] = (char) (cv & 0xff);
                        sdata[i + 1] = (char) ((cv >> 8) & 0xff);
                        sdata[i + 2] = (char) ((cv >> 16) & 0xff);
                        sdata[i + 3] = (char) ((cv >> 24) & 0xff);
                    }
                    source = new String(sdata, 0, slen);
                    cursum += source.hashCode();
                    cursum ^= slen;
                }
                byte[] data = new byte[len];
                int i;
                for (i = 0; i < (len & ~3); i += 4) {
                    int cv = indata[co++];
                    data[i] = (byte) cv;
                    data[i + 1] = (byte) (cv >> 8);
                    data[i + 2] = (byte) (cv >> 16);
                    data[i + 3] = (byte) (cv >> 24);
                    cursum -= cv;
                    cursum ^= len - i;
                }
                if ((len & 3) != 0) {
                    int cv = indata[co++];
                    switch (len & 3) {
                        case 3:
                            data[i + 2] = (byte) (cv >> 16);
                        // fall through
                        case 2:
                            data[i + 1] = (byte) (cv >> 8);
                        // fall through
                        case 1:
                            data[i] = (byte) cv;
                    }
                    cursum -= cv;
                    cursum ^= len - i;
                }
                if (source == null) {
                    source = linkName;
                } else {
                    source = linkName + "/" + source;
                }
                if ((short) ((cursum >> 16) ^ cursum) != checksum) {
                    Logger.warning("Dropped packet from " + source + " to " + dest + " because of checksum mismatch!");
                    return;
                }
                if (co != indata.length) {
                    Logger.warning("Packet from " + source + " to " + dest + " had incorrect (but non-erroneous) length.");
                }
                node.transmit(dest, source, data, this);
            } catch (ArrayIndexOutOfBoundsException ex) {
                Logger.log(LogLevel.WARNING, "Error while parsing XBee Cluck message", ex);
            }
        } else {
            Logger.warning("Unknown XBee Response packet: " + pkt + " : " + pkt.getClass());
        }
    }
}

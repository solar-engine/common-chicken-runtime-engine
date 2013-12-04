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

import ccre.cluck.CluckNode;
import ccre.concurrency.ReporterThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * An advanced XBee Cluck link, with reliability and compression.
 *
 * @author skeggsc
 */
public class ReliableCompressionXBeeLink extends ReliableCompressionCluckLink {

    private final XBeeRadio radio;
    private final int[] remote;
    private final BlockingQueue<int[]> xbeeSendQueue = new ArrayBlockingQueue<int[]>(100);

    public ReliableCompressionXBeeLink(XBeeRadio r, int[] rem, CluckNode n) {
        super(n);
        this.radio = r;
        this.remote = rem;
    }

    public ReliableCompressionXBeeLink(XBeeRadio r, int[] rem, CluckNode n, String linkName) {
        super(n, linkName);
        this.radio = r;
        this.remote = rem;
    }

    @Override
    protected final void basicTransmit(byte[] packet, int offset, int count) {
        int[] tosend = new int[count];
        for (int i = 0; i < count; i++) {
            tosend[i] = packet[i + offset];
        }
        if (!xbeeSendQueue.offer(tosend)) {
            xbeeSendQueue.poll(); // Remove an old entry, so that messages rotate through faster.
            Logger.warning("Send queue overflow!");
        }
    }

    @Override
    protected final void basicStartComms() {
        new ReporterThread("Xbee-SendQueue") {
            @Override
            protected void threadBody() throws InterruptedException {
                while (true) {
                    try {
                        radio.sendPacketUnverified(remote, xbeeSendQueue.take());
                    } catch (Throwable ex) {
                        Logger.log(LogLevel.WARNING, "Error in XBee send", ex);
                    }
                }
            }
        }.start();
        radio.addPacketListener(new PacketListener() {
            @Override
            public void processResponse(XBeeResponse xbr) {
                if (!(xbr instanceof ZNetRxResponse)) {
                    return;
                }
                ZNetRxResponse zp = (ZNetRxResponse) xbr;
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
                addBasicReceiveToQueue(realindata);
            }
        });
    }
}

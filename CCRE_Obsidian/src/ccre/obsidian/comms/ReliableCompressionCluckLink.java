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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An advanced XBee Cluck link, with reliability and compression.
 *
 * @author skeggsc
 */
public abstract class ReliableCompressionCluckLink extends ReliableCompressionLink implements CluckLink {

    private final BlockingQueue<ReceptionNode> cluckReception = new LinkedBlockingQueue<ReceptionNode>();
    private final BlockingQueue<byte[]> rawReception = new LinkedBlockingQueue<byte[]>();
    private String linkName;
    private final CluckNode node;

    private final class ReceptionNode {

        private final String dest;
        private final String source;
        private final byte[] data;

        private ReceptionNode(String dest, String source, byte[] data) {
            this.dest = dest;
            this.source = source;
            this.data = data;
        }
    }

    public ReliableCompressionCluckLink(CluckNode node) {
        this.node = node;
    }

    public ReliableCompressionCluckLink(CluckNode node, String linkName) {
        this.node = node;
        this.linkName = linkName;
        node.addLink(this, linkName);
    }
    
    @Override
    protected synchronized void handleReliableReceivingReset() {
        super.handleReliableReceivingReset();
        node.notifyNetworkModified();
    }

    @Override
    protected final void compressionReceive(String dest, String source, byte[] data) {
        cluckReception.add(new ReceptionNode(dest, source, data));
    }

    @Override
    public final boolean send(String dest, String source, byte[] data) {
        try {
            if (dest.startsWith("unT/")) { // Marked as unreliable TO destination
                compressionUnreliableTransmit(dest.substring(4), "unF/" + source, data);
            } else if (dest.startsWith("unF/")) { // Marked as unreliable FROM destination
                compressionTransmit(dest.substring(4), "unT/" + source, data);
            } else if (dest.startsWith("unB/")) { // Marked as unreliable TO and FROM destination (_B_oth)
                compressionUnreliableTransmit(dest.substring(4), "unB/" + source, data);
            } else {
                compressionTransmit(dest, source, data);
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    public final void addBasicReceiveToQueue(byte[] realindata) {
        rawReception.add(realindata);
    }

    @Override
    protected final void basicStart() {
        new ReporterThread("Xbee-ReceiveQueue") {
            @Override
            protected void threadBody() throws InterruptedException {
                while (true) {
                    byte[] pkt = rawReception.take();
                    try {
                        basicReceiveHandler(pkt, 0, pkt.length);
                    } catch (Throwable ex) {
                        Logger.log(LogLevel.SEVERE, "Error in reliable handle!", ex);
                    }
                }
            }
        }.start();
        new ReporterThread("Xbee-CluckHandle") {
            @Override
            protected void threadBody() throws Throwable {
                while (true) {
                    ReceptionNode rn = cluckReception.take();
                    String dest = rn.dest;
                    String source = rn.source;
                    byte[] data = rn.data;
                    if (linkName == null) {
                        linkName = node.getLinkName(ReliableCompressionCluckLink.this);
                    }
                    if (source == null) {
                        source = linkName;
                    } else {
                        source = linkName + "/" + source;
                    }
                    try {
                        node.transmit(dest, source, data, ReliableCompressionCluckLink.this);
                    } catch (Throwable thr) {
                        Logger.log(LogLevel.SEVERE, "Error during Cluck transmit!", thr);
                    }
                }
            }
        }.start();
        basicStartComms();
    }

    protected abstract void basicStartComms();
}

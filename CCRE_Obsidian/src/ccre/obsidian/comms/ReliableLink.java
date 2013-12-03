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

import ccre.concurrency.ReporterThread;
import ccre.log.Logger;
import ccre.util.CLinkedList;
import java.util.Arrays;
import java.util.Random;

/**
 * Constructs a reliable link over an unreliable link.
 *
 * @author skeggsc
 */
public abstract class ReliableLink {

    private long lastConnectionKeep = 0; // Last time a correct ack packet was received.
    private long lastSendSequenceUpdate = 0;
    private long lastLivingTransmit = 0;

    private boolean isConnected() { // Up to 4000 milliseconds.
        return System.currentTimeMillis() - lastConnectionKeep < 4000;
    }

    protected abstract void handleReliableSendingReset();

    protected abstract void handleReliableReceivingReset();
    public static final int sendCount = 8;
    private final Object transmitLock = new Object();
    private final byte[][] sending = new byte[sendCount][];
    private final Integer[] sendSeqs = new Integer[sendCount]; // sequence identifiers 
    private final long[] sentAt = new long[sendCount];
    private Integer nextSendSeq = null;
    private final ReporterThread resender = new ReporterThread("Link-Resender") {
        @Override
        protected void threadBody() throws Throwable {
            while (true) {
                if (!isConnected()) {
                    if (lastSendSequenceUpdate < System.currentTimeMillis() - 1500) {
                        lastSendSequenceUpdate = System.currentTimeMillis();
                        // Tell the other end what sequence number to listen for next!
                        for (int i = 0; i < sendCount; i++) {
                            sendSeqs[i] = null;
                        }
                        int s = nextSendSeq = new Random().nextInt() & 0x7FFFFF;
                        Logger.warning("Gave remote new target ID: " + s + " from " + ReliableLink.this + " at " + System.currentTimeMillis());
                        basicTransmit(new byte[]{(byte) 0xDA, (byte) 'S', (byte) 0x7A, (byte) 'R', (byte) 0x70, (byte) s, (byte) (s >> 8), (byte) (s >> 16)}, 0, 8);
                        for (int i = 0; i < sendCount; i++) {
                            sendSeqs[i] = null;
                        }
                        handleReliableSendingReset();
                    }
                } else {
                    long expiring = System.currentTimeMillis() - 150;
                    byte[][] tosend = new byte[sendCount][];
                    synchronized (transmitLock) {
                        for (int i = 0; i < sendCount; i++) {
                            if (sendSeqs[i] != null) {
                                if (sentAt[i] < expiring) {
                                    tosend[i] = sending[i];
                                    sentAt[i] = System.currentTimeMillis();
                                }
                            }
                        }
                    }
                    for (int i = 0; i < tosend.length; i++) {
                        byte[] ts = tosend[i];
                        if (ts != null) {
                            basicTransmit(ts, 0, ts.length);
                        }
                    }
                    if (nextRecvSeq != null && lastLivingTransmit < System.currentTimeMillis() - 500) {
                        lastLivingTransmit = System.currentTimeMillis();
                        Logger.fine("SEND KEEPALIVE from " + ReliableLink.this + " at " + lastLivingTransmit);
                        basicTransmit(new byte[]{(byte) 'K', (byte) 'P', (byte) 'A'}, 0, 3);
                    }
                }
                Thread.sleep(50);
            }
        }
    };
    public static final int receiveCount = 16;
    private final Object receiveLock = new Object();
    private final int[] waitingFor = new int[receiveCount];
    private Integer nextRecvSeq = null;
    private final CLinkedList<byte[]> receiveQueue = new CLinkedList<byte[]>();
    private Integer nextReceiveQueueId = null;

    public final void start() {
        resender.start();
        basicStart();
    }

    protected abstract void basicStart();

    protected abstract void basicTransmit(byte[] packet, int offset, int count);

    protected abstract void reliableReceive(byte[] packet, int offset, int count);
    
    protected abstract void unreliableReceive(byte[] packet, int offset, int count);

    public final void basicReceiveHandler(byte[] pkt, int offset, int count) {
        if (count < 3) {
            Logger.warning("Dropped too-short packet!");
            return;
        }
        int seq = ((pkt[offset] & 0xff) << 16) | ((pkt[offset + 1] & 0xff) << 8) | (pkt[offset + 2] & 0xff);

        Logger.fine(this + ": Recv " + seq + ": " + Arrays.toString(Arrays.copyOfRange(pkt, offset, offset + count)));
        if (count == 8 && pkt[offset] == (byte) 0xDA && pkt[offset + 1] == (byte) 'S' && pkt[offset + 2] == (byte) 0x7A && pkt[offset + 3] == (byte) 'R' && pkt[offset + 4] == (byte) 0x70) {
            Logger.fine("GOT REASSIGN " + this);
            // Force sequence ID!
            nextReceiveQueueId = nextRecvSeq = (pkt[offset + 5] & 0xff) | ((pkt[offset + 6] & 0xff) << 8) | ((pkt[offset + 7] & 0xff) << 16);
            receiveQueue.clear();
            synchronized (receiveLock) {
                for (int i = 0; i < receiveCount; i++) {
                    waitingFor[i] = nextRecvSeq;
                    nextRecvSeq = (nextRecvSeq + 1) & 0x7fffff;
                }
            }
            basicTransmit(new byte[]{(byte) 'K', (byte) 'P', (byte) 'A'}, 0, 3);
            lastLivingTransmit = System.currentTimeMillis();
            Logger.fine("SEND ALIVE from " + this);
            handleReliableReceivingReset();
            return;
        }
        if (count == 3 && pkt[offset] == (byte) 'K' && pkt[offset + 1] == (byte) 'P' && pkt[offset + 2] == (byte) 'A') {
            // No-content ping.
            lastConnectionKeep = System.currentTimeMillis();
            Logger.fine("Got keep-alive: " + this + " " + lastConnectionKeep);
            return;
        }
        if (pkt[offset] == (byte) 0xCA && pkt[offset+1] == (byte) 0xDB && pkt[offset+2] == (byte) 0xEC) {
            Logger.fine("Unreliable receive " + Arrays.toString(pkt));
            unreliableReceive(pkt, offset + 3, count - 3);
            return;
        }
        if (count == 3) { // Received bounce
            synchronized (transmitLock) {
                Logger.config("Receive " + seq + " in " + Arrays.toString(sendSeqs));
                for (int i = 0; i < sendSeqs.length; i++) {
                    if (sendSeqs[i] != null && sendSeqs[i].intValue() == seq) {
                        sendSeqs[i] = null;
                        transmitLock.notifyAll();
                        lastConnectionKeep = System.currentTimeMillis();
                        Logger.fine("As keep-alive: " + this + " " + lastConnectionKeep);
                    }
                }
            }
        } else {
            if (nextRecvSeq == null) { // Can't do anything!
                Logger.info("Null receive sequence!");
                return;
            }
            lastLivingTransmit = System.currentTimeMillis();
            Logger.fine("SEND ALIVE from " + this);
            basicTransmit(pkt, offset, 3);
            boolean found = false;
            synchronized (receiveLock) {
                for (int i = 0; i < receiveCount; i++) {
                    if (waitingFor[i] == seq) {
                        waitingFor[i] = nextRecvSeq;
                        nextRecvSeq = (nextRecvSeq + 1) & 0x7fffff;
                        found = true;
                    }
                }
            }
            Logger.config("FOUND " + found + " OF " + seq + " IN " + Arrays.toString(waitingFor));
            if (found) {
                synchronized (receiveQueue) {
                    int slot = seq - nextReceiveQueueId;
                    while (slot >= receiveQueue.size()) {
                        receiveQueue.addLast(null);
                    }
                    byte[] arr = Arrays.copyOfRange(pkt, offset + 3, offset + count);
                    receiveQueue.set(slot, arr);
                    while (!receiveQueue.isEmpty() && receiveQueue.getFirst() != null) {
                        byte[] rf = receiveQueue.removeFirst();
                        nextReceiveQueueId++;
                        reliableReceive(rf, 0, rf.length);
                    }
                }
            }
        }
    }
    
    public final void blockingUnreliableTransmit(byte[] packet, int offset, int count) throws InterruptedException {
        byte[] realpacket = new byte[count + 3];
        realpacket[0] = (byte) 0xCA;
        realpacket[1] = (byte) 0xDB;
        realpacket[2] = (byte) 0xEC;
        System.arraycopy(packet, offset, realpacket, 3, count);
        basicTransmit(realpacket, 0, realpacket.length);
    }

    public final void blockingReliableTransmit(byte[] packet, int offset, int count) throws InterruptedException {
        int sequence;
        synchronized (transmitLock) {
            while (nextSendSeq == null) {
                transmitLock.wait(500);
            }
            sequence = nextSendSeq;
            nextSendSeq = (sequence + 1) & 0x7fffff;
        }
        Logger.fine(this + ": Send " + sequence + ": " + Arrays.toString(Arrays.copyOfRange(packet, offset, offset + count)));
        byte[] realpacket = new byte[count + 3];
        realpacket[0] = (byte) (sequence >> 16);
        realpacket[1] = (byte) (sequence >> 8);
        realpacket[2] = (byte) sequence;
        System.arraycopy(packet, offset, realpacket, 3, count);
        synchronized (transmitLock) {
            boolean added = false;
            while (true) {
                for (int i = 0; i < sendSeqs.length; i++) {
                    if (sendSeqs[i] == null) {
                        sendSeqs[i] = sequence;
                        sending[i] = realpacket;
                        sentAt[i] = System.currentTimeMillis();
                        added = true;
                        break;
                    }
                }
                if (added) {
                    break;
                }
                transmitLock.wait(2000);
            }
        }
        basicTransmit(realpacket, 0, realpacket.length);
    }
}

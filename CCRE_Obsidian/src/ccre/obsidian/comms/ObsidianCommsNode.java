/*
 * Copyright 2013 Colby Skeggs and Vincent Miller
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

import ccre.chan.BooleanOutput;
import ccre.chan.FloatOutput;
import ccre.event.EventConsumer;
import ccre.log.LogLevel;
import ccre.log.Logger;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class provides a more lightweight comms system for XBee radios. It works
 * similarly to Cluck, but is more lightweight.
 *
 * @author MillerV
 */
public class ObsidianCommsNode {

    /**
     * The main node, which should be used in most cases. This should only be
     * used after createGlobalNode has be called.
     */
    public static ObsidianCommsNode globalNode;
    /**
     * The packet header for packets targeted at an OutputStream.
     */
    public static final byte HEADER_RAW = 0x00;
    /**
     * The packet header for packets targeted at a FloatOutput.
     */
    public static final byte HEADER_FLOAT = 0x01;
    /**
     * The packet header for packets targeted at a BooleanOutput.
     */
    public static final byte HEADER_BOOLEAN = 0x02;
    /**
     * The packet header for packets targeted at an EventConsumer.
     */
    public static final byte HEADER_EVENT = 0x03;

    private final HashMap<Byte, OutputStream> outputStreams;
    private final HashMap<Byte, FloatOutput> floatOutputs;
    private final HashMap<Byte, BooleanOutput> booleanOutputs;
    private final HashMap<Byte, EventConsumer> eventConsumers;

    private final boolean secure;
    private final LinkedList<int[]> allowedAddresses;
    private final XBeeRadio radio;

    public static void createGlobalNode(boolean secure, XBeeRadio radio) {
        globalNode = new ObsidianCommsNode(secure, radio);
    }

    /**
     * Create a new ObsidianCommsNode on an existing XBeeRadio.
     *
     * @param secure whether the source addresses of incoming packets are
     * checked
     * @param radio the XBeeRadio to create this node on.
     */
    public ObsidianCommsNode(boolean secure, XBeeRadio radio) {
        outputStreams = new HashMap<Byte, OutputStream>();
        floatOutputs = new HashMap<Byte, FloatOutput>();
        booleanOutputs = new HashMap<Byte, BooleanOutput>();
        eventConsumers = new HashMap<Byte, EventConsumer>();

        this.secure = secure;
        allowedAddresses = new LinkedList<int[]>();

        this.radio = radio;
        radio.addPacketListener(new NodePacketListener());
    }

    /**
     * Give access to the given remote addresses. Irrelevant if secure is set
     * to false.
     *
     * @param addresses the addresses to give access to.
     */
    public void allowAccess(int[]  
        ... addresses) {
        allowedAddresses.addAll(Arrays.asList(addresses));
    }

    /**
     * Remove access from the given remote addresses. Irrelevant if secure is
     * set to false.
     *
     * @param addresses the addresses to remove access from.
     */
    public void blockAccess(int[]  
        ... addresses) {
        allowedAddresses.removeAll(Arrays.asList(addresses));
    }

    /**
     * Add an OutputStream to listen for incoming data on the specified id.
     *
     * @param id the id to listen on
     * @param os the OutputStream to add as a listener
     */
    public void addListener(byte id, OutputStream os) {
        outputStreams.put(id, os);
    }

    /**
     * Add a FloatOutput to listen for incoming float data on the specified id.
     *
     * @param id the id to listen on
     * @param fo the FloatOutput to add as a listener
     */
    public void addListener(byte id, FloatOutput fo) {
        floatOutputs.put(id, fo);
    }

    /**
     * Add a BooleanOutput to listen for incoming boolean data on the specified
     * id.
     *
     * @param id the id to listen on
     * @param bo the BooleanOutput to add as a listener
     */
    public void addListener(byte id, BooleanOutput bo) {
        booleanOutputs.put(id, bo);
    }

    /**
     * Add an EventConsumer to listen for incoming events on the specified id.
     *
     * @param id the id to listen on
     * @param ec the EventConsumer to add as a listener
     */
    public void addListener(byte id, EventConsumer ec) {
        eventConsumers.put(id, ec);
    }

    /**
     * Send a single byte over the XBee network, to be received by an
     * OutputStream at the specified address and id.
     *
     * @param id the id to send to
     * @param addr the address to send to
     * @param data the data to send
     */
    public void sendByte(byte id, int[] addr, int data) {
        try {
            radio.sendPacketUnverified(addr, new int[]{HEADER_RAW, id, (byte) data});
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not send packet: ", e);
        }
    }

    /**
     * Send a series of bytes over the XBee network, to be received by an
     * OutputStream at the specified address and id.
     *
     * @param id the id to send to
     * @param addr the address to send to
     * @param data the data to send
     */
    public void sendBytes(byte id, int[] addr, byte[] data) {
        int[] pkt = new int[data.length + 2];
        System.arraycopy(data, 0, pkt, 2, data.length);
        pkt[0] = HEADER_RAW;
        pkt[1] = id;
        try {
            radio.sendPacketUnverified(addr, pkt);
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not send packet: ", e);
        }
    }

    /**
     * Send a float over the XBee network, to be received by an FloatOutput at
     * the specified address and id.
     *
     * @param id the id to send to
     * @param addr the address to send to
     * @param data the data to send
     */
    public void sendFloat(byte id, int[] addr, float data) {
        int i = Float.floatToIntBits(data);
        byte b1 = (byte) (i >> 24);
        byte b2 = (byte) (i >> 16);
        byte b3 = (byte) (i >> 8);
        byte b4 = (byte) i;

        try {
            radio.sendPacketUnverified(addr, new int[]{HEADER_FLOAT, id, b1, b2, b3, b4});
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not send packet: ", e);
        }
    }

    /**
     * Send a boolean over the XBee network, to be received by a BooleanOutput
     * at the specified address and id.
     *
     * @param id the id to send to
     * @param addr the address to send to
     * @param data the data to send
     */
    public void sendBoolean(byte id, int[] addr, boolean data) {
        byte f = data ? (byte) 0x01 : (byte) 0x00;
        try {
            radio.sendPacketUnverified(addr, new int[]{HEADER_BOOLEAN, id, f});
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not send packet: ", e);
        }
    }

    /**
     * Trigger an event over the XBee network, to be received by an
     * EventConsumer at the specified address and id.
     *
     * @param id the id to send to
     * @param addr the address to send to
     */
    public void sendEvent(byte id, int[] addr) {
        try {
            radio.sendPacketUnverified(addr, new int[]{HEADER_EVENT, id});
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not send packet: ", e);
        }
    }

    /**
     * Create an OutputStream that can be used to send data on the specified id
     * to the specified address on the XBee network. The write(byte[] b) method
     * should be used to send string data.
     *
     * @param id the id to send data on
     * @param addr the address to send the data to
     * @return an OutputStream that will send data to the specified id and
     * address
     */
    public OutputStream createOutputStream(final byte id, final int[] addr) {
        return new OutputStream() {
            @Override
            public void write(int data) throws IOException {
                sendByte(id, addr, data);
            }

            @Override
            public void write(byte[] data) throws IOException {
                sendBytes(id, addr, data);
            }
        };
    }

    /**
     * Create a FloatOutput that can be used to send float data on the specified
     * id to the specified address on the XBee network.
     *
     * @param id the id to send data on
     * @param addr the address to send the data to
     * @return a FloatOutput that will send data to the specified id and address
     */
    public FloatOutput createFloatOutput(final byte id, final int[] addr) {
        return new FloatOutput() {
            @Override
            public void writeValue(float data) {
                sendFloat(id, addr, data);
            }
        };
    }

    /**
     * Create a BooleanOutput that can be used to send boolean data on the
     * specified id to the specified address on the XBee network.
     *
     * @param id the id to send data on
     * @param addr the address to send the data to
     * @return a BooleanOutput that will send data to the specified id and
     * address.
     */
    public BooleanOutput createBooleanOutput(final byte id, final int[] addr) {
        return new BooleanOutput() {
            @Override
            public void writeValue(boolean data) {
                sendBoolean(id, addr, data);
            }
        };
    }

    /**
     * Create an EventConsumer that can be used to send events on the specified
     * id to the specified address on the XBee network.
     *
     * @param id the id to send events on
     * @param addr the address to send the events to
     * @return an EventConsumer that will send events to the specified id and
     * address.
     */
    public EventConsumer createEventConsumer(final byte id, final int[] addr) {
        return new EventConsumer() {
            @Override
            public void eventFired() {
                sendEvent(id, addr);
            }
        };
    }

    private void processResponseMain(XBeeResponse r) {
        if (!(r instanceof ZNetRxResponse)) {
            return;
        }

        ZNetRxResponse msg = (ZNetRxResponse) r;

        Logger.info("Recieved: " + Arrays.toString(msg.getData()));

        if (secure) {
            boolean ok = false;
            for (int[] addr : allowedAddresses) {
                if (Arrays.equals(addr, msg.getRemoteAddress64().getAddress())) {
                    ok = true;
                }
            }
            if (!ok) {
                Logger.log(LogLevel.INFO, "Blocked message from address: " + Arrays.toString(msg.getRemoteAddress64().getAddress()));
                return;
            }
        }

        int[] p = msg.getData();

        if (p.length < 2) {
            Logger.log(LogLevel.INFO, "Dropped packet due to incomplete header.");
            return;
        }

        byte header = (byte) p[0];
        byte id = (byte) p[1];

        byte[] data = null;
        if (p.length > 2) {
            data = new byte[p.length - 2];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) p[i + 2];
            }
        } else if (header != HEADER_EVENT) {
            Logger.log(LogLevel.INFO, "Dropped packet due to incomplete header.");
        }

        switch (header) {
            case HEADER_RAW:
                if (outputStreams.containsKey(id)) {
                    try {
                        outputStreams.get(id).write(data);
                    } catch (IOException e) {
                        Logger.log(LogLevel.INFO, "IOException thrown by user code: ", e);
                    }
                } else {
                    Logger.log(LogLevel.INFO, "Dropped packet (no listener registered).");
                }
                break;
            case HEADER_FLOAT:
                if (floatOutputs.containsKey(id)) {
                    if (data.length < 4) {
                        Logger.log(LogLevel.INFO, "Dropped packet due to incomplete float data.");
                    }

                    int d = (data[2] & 0xff) << 24;

                    d = d | (data[3] & 0xff) << 16;
                    d = d | (data[4] & 0xff) << 8;
                    d = d | (data[5] & 0xff);

                    float f = Float.intBitsToFloat(d);

                    floatOutputs.get(id).writeValue(f);
                } else {
                    Logger.log(LogLevel.INFO, "Dropped packet (no listener registered).");
                }
                break;
            case HEADER_BOOLEAN:
                if (booleanOutputs.containsKey(id)) {
                    if (data.length < 1) {
                        Logger.log(LogLevel.INFO, "Dropped packet due to incomplete boolean data.");
                        boolean b = (data[0] == (byte) 1);
                        booleanOutputs.get(id).writeValue(b);
                    }
                } else {
                    Logger.log(LogLevel.INFO, "Dropped packet (no listener registered).");
                }
                break;
            case HEADER_EVENT:
                if (eventConsumers.containsKey(id)) {
                    eventConsumers.get(id).eventFired();
                } else {
                    Logger.log(LogLevel.INFO, "Dropped packet (no listener registered).");
                }
                break;
            default:
                Logger.log(LogLevel.INFO, "Dropped packet due to bad type header: " + header);
        }
    }

    private class NodePacketListener implements PacketListener {
        @Override
        public void processResponse(XBeeResponse xbr) {
            processResponseMain(xbr);
        }
    }
}

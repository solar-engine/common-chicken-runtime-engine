/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.chan.*;
import ccre.cluck.rpc.RPCManager;
import ccre.event.*;
import ccre.holders.CompoundFloatTuner;
import ccre.holders.FloatTuner;
import ccre.log.*;
import ccre.util.CArrayList;
import ccre.util.CHashMap;
import ccre.util.UniqueIds;
import ccre.util.Utils;
import ccre.workarounds.ThrowablePrinter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * A CluckNode is the core hub of the Cluck networking system on a device. It
 * handles message routing, publishing, and subscribing.
 *
 * Usually the main instance of this is kept in CluckGlobals.
 *
 * @author skeggsc
 */
public final class CluckNode {

    /**
     * The ID representing a PING message.
     */
    public static final byte RMT_PING = 0;
    /**
     * The ID representing an EventConsumer firing message.
     */
    public static final byte RMT_EVENTCONSUMER = 1;
    /**
     * The ID representing an EventSource subscription message.
     */
    public static final byte RMT_EVENTSOURCE = 2;
    /**
     * The ID representing an EventSource response message.
     */
    public static final byte RMT_EVENTSOURCERESP = 3;
    /**
     * The ID representing a logging message.
     */
    public static final byte RMT_LOGTARGET = 4;
    /**
     * The ID representing a BooleanInputProducer subscription message.
     */
    public static final byte RMT_BOOLPROD = 5;
    /**
     * The ID representing a BooleanInputProducer response message.
     */
    public static final byte RMT_BOOLPRODRESP = 6;
    /**
     * The ID representing a BooleanOutput write message.
     */
    public static final byte RMT_BOOLOUTP = 7;
    /**
     * The ID representing a FloatInputProducer subscription message.
     */
    public static final byte RMT_FLOATPROD = 8;
    /**
     * The ID representing a FloatInputProducer response message.
     */
    public static final byte RMT_FLOATPRODRESP = 9;
    /**
     * The ID representing a FloatOutput write message.
     */
    public static final byte RMT_FLOATOUTP = 10;
    /**
     * The ID representing an OutputStream write message.
     */
    public static final byte RMT_OUTSTREAM = 11;
    /**
     * The ID representing a network infrastructure modification notification.
     */
    public static final byte RMT_NOTIFY = 12;
    /**
     * The ID representing a remote procedure invocation.
     */
    public static final byte RMT_INVOKE = 13;
    /**
     * The ID representing a response to a remote procedure invocation.
     */
    public static final byte RMT_INVOKE_REPLY = 14;
    /**
     * The ID representing a notification that a link doesn't exist.
     */
    public static final byte RMT_NEGATIVE_ACK = 15;
    private static final String[] remoteNames = new String[]{"Ping", "EventConsumer", "EventSource", "EventSourceResponse", "LogTarget",
        "BooleanInputProducer", "BooleanInputProducerResponse", "BooleanOutput", "FloatInputProducer", "FloatInputProducerResponse",
        "FloatOutput", "OutputStream", "Notify", "RemoteProcedure", "RemoteProcedureReply", "NonexistenceNotification"};

    /**
     * Convert an RMT ID to a string.
     *
     * @param type The RMT_* message ID.
     * @return The version representing the name of the message type.
     */
    public static String rmtToString(int type) {
        if (type >= 0 && type < remoteNames.length) {
            return remoteNames[type];
        } else {
            return "Unknown #" + type;
        }
    }
    /**
     * A map of the current link names to the CluckLinks.
     */
    public final CHashMap<String, CluckLink> links = new CHashMap<String, CluckLink>();
    /**
     * An estimated total of how many bytes have gone through this node, as if
     * the messages were serialized through CluckProtocol.
     */
    private int estimatedByteCount;

    /**
     * Returns an estimated total of how many bytes have gone through this node,
     * as if the messages were serialized through CluckProtocol.
     *
     * @return The number of binary octets.
     */
    public int getEstimatedByteCount() {
        return estimatedByteCount;
    }
    /**
     * The time when the last error message was printed about a link not
     * existing.
     */
    private long lastMissingLinkError = 0;
    /**
     * The link name of the last error message about a link not existing.
     */
    private String lastMissingLink = null;
    /**
     * The official RPCManager for this node.
     */
    private RPCManager rpcManager = null;

    /**
     * Notify everyone on the network that the network structure has been
     * modified - for example, when a connection is opened or closed.
     */
    public void notifyNetworkModified() {
        transmit("*", "#modsrc", new byte[]{RMT_NOTIFY});
    }

    /**
     * Transmit a message to the specified other link (relative to this node),
     * with the specified return address (relative to this node).
     *
     * Paths are /-separated, with each element being a link to follow.
     *
     * @param target The target path.
     * @param source The source path.
     * @param data The message data to transmit.
     */
    public void transmit(String target, String source, byte[] data) {
        transmit(target, source, data, null);
    }

    /**
     * Transmit a message to the specified other link (relative to this node),
     * with the specified return address (relative to this node). If this is a
     * broadcast, then don't include the specified link (to prevent infinite
     * loops).
     *
     * Paths are /-separated, with each element being a link to follow.
     *
     * @param target The target path.
     * @param source The source path.
     * @param data The message data to transmit.
     * @param denyLink The link for broadcasts to not follow.
     */
    public void transmit(String target, String source, byte[] data, CluckLink denyLink) {
        estimatedByteCount += 24 + (target == null ? 0 : target.length()) + (source == null ? 0 : source.length()) + data.length; // 24 is the estimated packet overhead with a CluckTCPClient.
        if (target == null) {
            if (data.length == 0 || data[0] != RMT_NEGATIVE_ACK) {
                Logger.log(LogLevel.WARNING, "Received message addressed to unreceving node (source: " + source + ")");
            }
        } else if ("*".equals(target)) {
            broadcast(source, data, denyLink);
        } else {
            int slash = target.indexOf('/');
            String base, rest;
            if (slash == -1) {
                base = target;
                rest = null;
            } else {
                base = target.substring(0, slash);
                rest = target.substring(slash + 1);
            }
            CluckLink link = links.get(base);
            if (link != null) {
                if (link.transmit(rest, source, data) == false) {
                    links.remove(base); // Remove it if the link says that it's done.
                }
            } else {
                reportMissingLink(data, source, target, base);
            }
        }
    }

    /**
     * Broadcast a message to all receiving nodes.
     *
     * This is the same as <code>transmit("*", source, data, denyLink)</code>.
     *
     * @param source The source of the message.
     * @param data The contents of the message.
     * @param denyLink The link to not send broadcasts to.
     * @see #transmit(java.lang.String, java.lang.String, byte[],
     * ccre.cluck.CluckLink)
     */
    public void broadcast(String source, byte[] data, CluckLink denyLink) {
        for (Iterator<String> it = links.iterator(); it.hasNext();) {
            String key = it.next();
            CluckLink cl = links.get(key);
            if (cl != null && cl != denyLink) {
                if (cl.transmit("*", source, data) == false) {
                    it.remove(); // Remove it if the link says that it's done.
                }
            }
        }
    }

    private void reportMissingLink(byte[] data, String source, String target, String base) {
        // Warnings about lost RMT_NEGATIVE_ACK messages or research messages are annoying, so don't send these,
        // and don't warn about the same message path too quickly.
        if ((data.length == 0 || data[0] != RMT_NEGATIVE_ACK) && target.indexOf("/rsch-") == -1
                && (!base.equals(lastMissingLink) || System.currentTimeMillis() >= lastMissingLinkError + 1000)) {
            lastMissingLink = base;
            lastMissingLinkError = System.currentTimeMillis();
            Logger.log(LogLevel.WARNING, "No link for " + target + "(" + base + ") from " + source + "!");
            transmit(source, target, new byte[]{RMT_NEGATIVE_ACK});
        }
    }

    /**
     * Set up the specified CluckRemoteListener to start receiving updates about
     * what remote nodes are broadcasting their availability.
     *
     * After this is set up, use cycleSearchRemotes to recheck for more remote
     * nodes.
     *
     * @param localRecv The local link name for the listener. Should be unique.
     * @param listener The listener to notify with all found remotes.
     * @see #cycleSearchRemotes(java.lang.String)
     */
    public void startSearchRemotes(String localRecv, final CluckRemoteListener listener) {
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (data.length == 2 && data[0] == RMT_PING) {
                    listener.handle(source, data[1]);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }
        }.attach(localRecv);
        transmit("*", localRecv, new byte[]{RMT_PING});
    }

    /**
     * Recheck for remote nodes, as in startSearchRemotes.
     *
     * @param localRecv The local receiving address, which must be the same as
     * the one passed to startSearchRemotes originally.
     * @see #startSearchRemotes(java.lang.String,
     * ccre.cluck.CluckRemoteListener)
     */
    public void cycleSearchRemotes(String localRecv) {
        transmit("*", localRecv, new byte[]{RMT_PING});
    }

    /**
     * Get a snapshot of the list of remote nodes of the specified remote type
     * (or null for all types).
     *
     * @param remoteType The remote type to search for, or null for all types.
     * @param timeout How long to wait for responses.
     * @return The snapshot of remotes.
     * @throws InterruptedException
     */
    public String[] searchRemotes(final Integer remoteType, int timeout) throws InterruptedException {
        final CArrayList<String> discovered = new CArrayList<String>();
        String localRecv = UniqueIds.global.nextHexId("rsch");
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (data.length == 2 && data[0] == RMT_PING) {
                    if (remoteType == null || remoteType == data[1]) {
                        synchronized (discovered) {
                            discovered.add(source);
                        }
                    }
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }
        }.attach(localRecv);
        try {
            transmit("*", localRecv, new byte[]{RMT_PING});
            Thread.sleep(timeout);
        } finally {
            links.remove(localRecv);
        }
        synchronized (discovered) {
            Iterator<String> it = discovered.iterator();
            String[] out = new String[discovered.size()];
            int i = 0;
            while (it.hasNext()) {
                String str = it.next();
                if (i >= out.length) {
                    Logger.severe("Lost remote due to concurrent modification!");
                    break;
                }
                out[i++] = str;
            }
            if (i < out.length) {
                Logger.severe("Damaged remotes due to concurrent modification!");
            }
            return out;
        }
    }

    /**
     * Get the name of the specified link.
     *
     * @param link The link to get the name for.
     * @return The link name.
     */
    public String getLinkName(CluckLink link) {
        if (link == null) {
            throw new NullPointerException();
        }
        for (String key : links) {
            if (links.get(key) == link) {
                return key;
            }
        }
        throw new RuntimeException("No such link!");
    }

    /**
     * Add the specified link at the specified link name.
     *
     * @param link The link.
     * @param linkName The link name.
     * @throws IllegalStateException if the specified link name is already used.
     */
    public void addLink(CluckLink link, String linkName) throws IllegalStateException {
        if (link == null) {
            throw new NullPointerException();
        }
        if (links.get(linkName) != null) {
            throw new IllegalStateException("Link name already used!");
        }
        links.put(linkName, link);
    }

    /**
     * Adds the specified link at the specified link name, replacing the current
     * link if necessary.
     *
     * @param link The link.
     * @param linkName The link name.
     */
    public void addOrReplaceLink(CluckLink link, String linkName) {
        if (link == null) {
            throw new NullPointerException();
        }
        if (links.get(linkName) != null) {
            Logger.fine("Replaced current link on: " + linkName);
        }
        links.put(linkName, link);
    }

    /**
     * Publish an EventConsumer on the network.
     *
     * @param name The name for the EventConsumer.
     * @param consum The EventConsumer.
     */
    public void publish(String name, final EventConsumer consum) {
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_EVENTCONSUMER)) {
                    consum.eventFired();
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_EVENTCONSUMER);
            }
        }.attach(name);
    }

    /**
     * Subscribe to an EventConsumer from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the EventConsumer.
     */
    public EventConsumer subscribeEC(final String path) {
        return new EventConsumer() {
            public void eventFired() {
                transmit(path, null, new byte[]{RMT_EVENTCONSUMER});
            }
        };
    }

    /**
     * Publish an EventSource on the network.
     *
     * @param name The name for the EventSource.
     * @param source The EventSource.
     */
    public void publish(final String name, EventSource source) {
        final CArrayList<String> remotes = new CArrayList<String>();
        source.addListener(new EventConsumer() {
            public void eventFired() {
                for (String remote : remotes) {
                    transmit(remote, name, new byte[]{RMT_EVENTSOURCERESP});
                }
            }
        });
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && data[0] == RMT_NEGATIVE_ACK) {
                    if (remotes.remove(src)) {
                        Logger.warning("Connection cancelled to " + src + " on " + name);
                    } else {
                        Logger.warning("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_EVENTSOURCE) && !remotes.contains(src)) {
                    remotes.add(src);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_EVENTSOURCE);
            }
        }.attach(name);
    }
    /**
     * A counter for (nearly) unique local IDs. This is combined with other
     * factors, so race conditions shouldn't be an issue.
     *
     * @see #nextLocalID()
     */
    private static int localIDs = 0;

    /**
     * Gets the next value from the unique local ID counter.
     *
     * @return A unique ID.
     */
    private static synchronized int nextLocalID() {
        return localIDs++;
    }

    /**
     * Subscribe to an EventSource from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the EventSource.
     */
    public EventSource subscribeES(final String path) {
        final String linkName = "srcES-" + path.hashCode() + "-" + nextLocalID();
        final BooleanStatus sent = new BooleanStatus();
        final Event e = new Event() {
            @Override
            public boolean addListener(EventConsumer cns) {
                boolean out = super.addListener(cns);
                if (!sent.readValue()) {
                    sent.writeValue(true);
                    transmit(path, linkName, new byte[]{RMT_EVENTSOURCE});
                }
                return out;
            }
        };
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_EVENTSOURCERESP)) {
                    e.produce();
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    if (sent.readValue()) {
                        CluckNode.this.transmit(path, linkName, new byte[]{RMT_EVENTSOURCE});
                    }
                }
            }
        }.attach(linkName);
        return e;
    }

    /**
     * Publish a LoggingTarget on the network.
     *
     * @param name The name for the LoggingTarget.
     * @param lt The LoggingTarget.
     */
    public void publish(String name, final LoggingTarget lt) {
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_LOGTARGET)) {
                    int l1, l2;
                    if (data.length < 10) {
                        Logger.warning("Not enough data to Logging Target!");
                        return;
                    }
                    l1 = Utils.bytesToInt(data, 2);
                    l2 = Utils.bytesToInt(data, 6);
                    if (l1 + l2 + 10 != data.length) {
                        Logger.warning("Bad data length to Logging Target!");
                        return;
                    }
                    String message = new String(data, 10, l1); // TODO: Figure out how to use UTF-8 on the robot.
                    String extended = l2 == 0 ? null : new String(data, 10 + l1, l2);
                    lt.log(LogLevel.fromByte(data[1]), message, extended);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_LOGTARGET);
            }
        }.attach(name);
    }

    private static long lastReportedRemoteLoggingError = 0;

    /**
     * Subscribe to a LoggingTarget from the network at the specified path, with
     * only sending data for at least a minimum logging level.
     *
     * @param path The path to subscribe to.
     * @param minimum The minimum logging level to send over the network.
     * @return the LoggingTarget.
     */
    public LoggingTarget subscribeLT(final String path, final LogLevel minimum) {
        return new LoggingTarget() {
            public void log(LogLevel level, String message, Throwable throwable) {
                log(level, message, ThrowablePrinter.toStringThrowable(throwable));
            }

            public void log(LogLevel level, String message, String extended) {
                try {
                    if (level.atLeastAsImportant(minimum)) {
                        byte[] msg = message.getBytes();
                        byte[] ext = extended == null ? new byte[0] : extended.getBytes();
                        byte[] out = new byte[10 + msg.length + ext.length];
                        out[0] = RMT_LOGTARGET;
                        out[1] = LogLevel.toByte(level);
                        int lm = msg.length;
                        out[2] = (byte) (lm >> 24);
                        out[3] = (byte) (lm >> 16);
                        out[4] = (byte) (lm >> 8);
                        out[5] = (byte) (lm);
                        int le = ext.length;
                        out[6] = (byte) (le >> 24);
                        out[7] = (byte) (le >> 16);
                        out[8] = (byte) (le >> 8);
                        out[9] = (byte) (le);
                        System.arraycopy(msg, 0, out, 10, msg.length);
                        System.arraycopy(ext, 0, out, 10 + msg.length, ext.length);
                        transmit(path, null, out);
                    }
                } catch (Throwable thr) {
                    if (System.currentTimeMillis() - lastReportedRemoteLoggingError > 500) {
                        Logger.log(LogLevel.SEVERE, "Error during remote log", thr);
                        lastReportedRemoteLoggingError = System.currentTimeMillis();
                    }
                }
            }
        };
    }

    /**
     * Publish a BooleanInput on the network. This is similar to publishing a
     * BooleanInputProducer, but will send values to clients when they connect.
     *
     * @param name The name for the BooleanInput.
     * @param prod The BooleanInput.
     */
    public void publish(final String name, final BooleanInput prod) {
        final CArrayList<String> remotes = new CArrayList<String>();
        prod.addTarget(new BooleanOutput() {
            public void writeValue(boolean value) {
                for (String remote : remotes) {
                    transmit(remote, name, new byte[]{RMT_BOOLPRODRESP, value ? (byte) 1 : 0});
                }
            }
        });
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && data[0] == RMT_NEGATIVE_ACK) {
                    if (remotes.remove(src)) {
                        Logger.warning("Connection cancelled to " + src + " on " + name);
                    } else {
                        Logger.warning("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_BOOLPROD)) {
                    remotes.add(src);
                    CluckNode.this.transmit(src, name, new byte[]{RMT_BOOLPRODRESP, prod.readValue() ? (byte) 1 : 0});
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_BOOLPROD);
            }
        }.attach(name);
    }

    /**
     * Publish a BooleanInputProducer on the network. This is similar to
     * publishing a BooleanInput, but will NOT send values to clients when they
     * connect.
     *
     * @param name The name for the BooleanInputProducer.
     * @param prod The BooleanInputProducer.
     */
    public void publish(final String name, final BooleanInputProducer prod) {
        final CArrayList<String> remotes = new CArrayList<String>();
        prod.addTarget(new BooleanOutput() {
            public void writeValue(boolean value) {
                for (String remote : remotes) {
                    transmit(remote, name, new byte[]{RMT_BOOLPRODRESP, value ? (byte) 1 : 0});
                }
            }
        });
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && data[0] == RMT_NEGATIVE_ACK) {
                    if (remotes.remove(src)) {
                        Logger.warning("Connection cancelled to " + src + " on " + name);
                    } else {
                        Logger.warning("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_BOOLPROD)) {
                    remotes.add(src);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_BOOLPROD);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a BooleanInputProducer from the network at the specified
     * path.
     *
     * @param path The path to subscribe to.
     * @param subscribeByDefault Should this request the value from the remote
     * by default, as opposed to waiting until this is needed. If this is false,
     * then readValue() won't work until you run addTarget().
     * @return the BooleanInputProducer.
     */
    public BooleanInput subscribeBIP(final String path, boolean subscribeByDefault) {
        final String linkName = "srcBIP-" + path.hashCode() + "-" + nextLocalID();
        final BooleanStatus sent = new BooleanStatus(subscribeByDefault);
        final BooleanStatus bs = new BooleanStatus() {
            @Override
            public void addTarget(BooleanOutput out) {
                super.addTarget(out);
                if (!sent.readValue()) {
                    sent.writeValue(true);
                    transmit(path, linkName, new byte[]{RMT_BOOLPROD});
                }
            }
        };
        if (subscribeByDefault) {
            transmit(path, linkName, new byte[]{RMT_BOOLPROD});
        }
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_BOOLPRODRESP)) {
                    if (data.length < 2) {
                        Logger.warning("Not enough bytes for boolean producer response!");
                        return;
                    }
                    bs.writeValue(data[1] != 0);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    if (sent.readValue()) {
                        CluckNode.this.transmit(path, linkName, new byte[]{RMT_BOOLPROD});
                    }
                }
            }
        }.attach(linkName);
        return bs;
    }

    /**
     * Publish a BooleanOutput on the network.
     *
     * @param name The name for the BooleanOutput.
     * @param out The BooleanOutput.
     */
    public void publish(String name, final BooleanOutput out) {
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_BOOLOUTP)) {
                    if (data.length < 2) {
                        Logger.warning("Not enough bytes for boolean output!");
                        return;
                    }
                    out.writeValue(data[1] != 0);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_BOOLOUTP);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a BooleanOutput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the BooleanOutput.
     */
    public BooleanOutput subscribeBO(final String path) {
        return new BooleanOutput() {
            public void writeValue(boolean b) {
                transmit(path, null, new byte[]{RMT_BOOLOUTP, b ? (byte) 1 : 0});
            }
        };
    }

    /**
     * Publish a FloatInput on the network. This is similar to publishing a
     * FloatInputProducer, but will send values to clients when they connect.
     *
     * @param name The name for the FloatInput.
     * @param prod The FloatInput.
     * @see #publish(java.lang.String, ccre.chan.FloatInputProducer)
     */
    public void publish(final String name, final FloatInput prod) {
        final CArrayList<String> remotes = new CArrayList<String>();
        prod.addTarget(new FloatOutput() {
            public void writeValue(float value) {
                for (String remote : remotes) {
                    int iver = Float.floatToIntBits(value);
                    transmit(remote, name, new byte[]{RMT_FLOATPRODRESP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
                }
            }
        });
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && data[0] == RMT_NEGATIVE_ACK) {
                    if (remotes.remove(src)) {
                        Logger.warning("Connection cancelled to " + src + " on " + name);
                    } else {
                        Logger.warning("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_FLOATPROD)) {
                    remotes.add(src);
                    int iver = Float.floatToIntBits(prod.readValue());
                    CluckNode.this.transmit(src, name, new byte[]{RMT_FLOATPRODRESP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_FLOATPROD);
            }
        }.attach(name);
    }

    /**
     * Publish a FloatInputProducer on the network. This is similar to
     * publishing a FloatInput, but will NOT send values to clients when they
     * connect.
     *
     * @param name The name for the FloatInputProducer.
     * @param prod The FloatInputProducer.
     * @see #publish(java.lang.String, ccre.chan.FloatInput)
     */
    public void publish(final String name, final FloatInputProducer prod) {
        final CArrayList<String> remotes = new CArrayList<String>();
        prod.addTarget(new FloatOutput() {
            public void writeValue(float value) {
                for (String remote : remotes) {
                    int iver = Float.floatToIntBits(value);
                    transmit(remote, name, new byte[]{RMT_FLOATPRODRESP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
                }
            }
        });
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && data[0] == RMT_NEGATIVE_ACK) {
                    if (remotes.remove(src)) {
                        Logger.warning("Connection cancelled to " + src + " on " + name);
                    } else {
                        Logger.warning("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_FLOATPROD)) {
                    remotes.add(src);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_FLOATPROD);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a FloatInputProducer (or FloatInput) from the network at the
     * specified path.
     *
     * @param path The path to subscribe to.
     * @param subscribeByDefault Should this request the value from the remote
     * by default, as opposed to waiting until this is needed. If this is false,
     * then readValue() won't work until you run addTarget().
     * @return the FloatInputProducer. (or FloatInput).
     */
    public FloatInput subscribeFIP(final String path, boolean subscribeByDefault) {
        final String linkName = "srcFIP-" + path.hashCode() + "-" + nextLocalID();
        final BooleanStatus sent = new BooleanStatus(subscribeByDefault);
        final FloatStatus fs = new FloatStatus() {
            @Override
            public void addTarget(FloatOutput out) {
                super.addTarget(out);
                if (!sent.readValue()) {
                    sent.writeValue(true);
                    transmit(path, linkName, new byte[]{RMT_FLOATPROD});
                }
            }
        };
        if (subscribeByDefault) {
            transmit(path, linkName, new byte[]{RMT_FLOATPROD});
        }
        new CluckSubscriber(this) {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_FLOATPRODRESP)) {
                    if (data.length < 5) {
                        Logger.warning("Not enough bytes for float producer response!");
                        return;
                    }
                    fs.writeValue(Utils.bytesToFloat(data, 1));
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    if (sent.readValue()) {
                        CluckNode.this.transmit(path, linkName, new byte[]{RMT_FLOATPROD});
                    }
                }
            }
        }.attach(linkName);
        return fs;
    }

    /**
     * Publish a FloatOutput on the network.
     *
     * @param name The name for the FloatOutput.
     * @param out The FloatOutput.
     */
    public void publish(String name, final FloatOutput out) {
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_FLOATOUTP)) {
                    if (data.length < 5) {
                        Logger.warning("Not enough bytes for float output!");
                        return;
                    }
                    out.writeValue(Utils.bytesToFloat(data, 1));
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_FLOATOUTP);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a FloatOutput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the FloatOutput.
     */
    public FloatOutput subscribeFO(final String path) {
        return new FloatOutput() {
            public void writeValue(float f) {
                int iver = Float.floatToIntBits(f);
                transmit(path, null, new byte[]{RMT_FLOATOUTP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
            }
        };
    }

    /**
     * Publish a FloatTuner on the network.
     *
     * @param name The name for the FloatTuner.
     * @param tune The FloatTuner.
     */
    public void publish(final String name, final FloatTuner tune) {
        publish(name + ".input", (FloatInput) tune);
        publish(name + ".output", (FloatOutput) tune);
        FloatInputProducer chan = tune.getAutomaticChannel();
        if (chan != null) {
            publish(name + ".auto", chan);
        }
    }

    /**
     * Subscribe to a FloatTuner from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @param subscribeByDefault This is sent to the subscriptions to
     * FloatInputs, so see subscribeFIP.
     * @return the FloatTuner.
     * @see #subscribeFIP(java.lang.String, boolean)
     */
    public FloatTuner subscribeTF(String path, boolean subscribeByDefault) {
        FloatInput tuneIn = subscribeFIP(path + ".input", subscribeByDefault);
        FloatOutput tuneOut = subscribeFO(path + ".output");
        final FloatInput autoIn = subscribeFIP(path + ".auto", subscribeByDefault);
        final CompoundFloatTuner comp = new CompoundFloatTuner(tuneIn, tuneOut);
        autoIn.addTarget(new FloatOutput() {
            public void writeValue(float value) {
                comp.setAuto(autoIn);
                autoIn.removeTarget(this);
            }
        });
        return comp;
    }

    /**
     * Publish a BooleanStatus on the network. This is provided to match the
     * publishability of FloatStatuses, since it was confusing that you could
     * publish one but not the other.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param name The name for the BooleanStatus.
     * @param stat The BooleanStatus to publish.
     */
    public void publish(final String name, BooleanStatus stat) {
        publish(name + ".input", (BooleanInput) stat);
        publish(name + ".output", (BooleanOutput) stat);
    }

    /**
     * Publish an OutputStream on the network.
     *
     * @param name The name for the OutputStream.
     * @param out The OutputStream.
     */
    public void publish(String name, final OutputStream out) {
        new CluckSubscriber(this) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_OUTSTREAM)) {
                    if (data.length > 1) {
                        try {
                            out.write(data, 1, data.length - 1);
                        } catch (IOException ex) {
                            Logger.log(LogLevel.WARNING, "IO Exception during network transfer!", ex);
                        }
                    }
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_OUTSTREAM);
            }
        }.attach(name);
    }

    /**
     * Subscribe to an OutputStream from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the OutputStream.
     */
    public OutputStream subscribeOS(final String path) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                transmit(path, null, new byte[]{RMT_OUTSTREAM, (byte) b});
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                byte[] newbyteout = new byte[len + 1];
                newbyteout[0] = RMT_OUTSTREAM;
                System.arraycopy(b, off, newbyteout, 1, len);
                transmit(path, null, newbyteout);
            }
        };
    }

    /**
     * Get the official RPCManager for this node.
     *
     * @return The RPCManager for this node.
     * @see ccre.cluck.rpc.RPCManager
     */
    public synchronized RPCManager getRPCManager() {
        if (rpcManager == null) {
            rpcManager = new RPCManager(this);
        }
        return rpcManager;
    }
}

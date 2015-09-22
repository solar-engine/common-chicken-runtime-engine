/*
 * Copyright 2013-2015 Colby Skeggs
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

import static ccre.cluck.CluckNode.RMT_BOOLINPUT;
import static ccre.cluck.CluckNode.RMT_BOOLINPUTRESP;
import static ccre.cluck.CluckNode.RMT_BOOLOUTP;
import static ccre.cluck.CluckNode.RMT_EVENTINPUT;
import static ccre.cluck.CluckNode.RMT_EVENTINPUTRESP;
import static ccre.cluck.CluckNode.RMT_EVENTOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATINPUT;
import static ccre.cluck.CluckNode.RMT_FLOATINPUTRESP;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_LEGACY_BOOLINPUT_UNSUB;
import static ccre.cluck.CluckNode.RMT_LEGACY_EVENTINPUT_UNSUB;
import static ccre.cluck.CluckNode.RMT_LEGACY_FLOATINPUT_UNSUB;
import static ccre.cluck.CluckNode.RMT_LOGTARGET;
import static ccre.cluck.CluckNode.RMT_NEGATIVE_ACK;
import static ccre.cluck.CluckNode.RMT_OUTSTREAM;
import static ccre.cluck.CluckNode.RMT_PING;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.rpc.RemoteProcedure;
import ccre.cluck.rpc.SimpleProcedure;
import ccre.log.LogLevel;
import ccre.log.LoggingTarget;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.util.UniqueIds;
import ccre.util.Utils;

/**
 * A file that handles publishing and subscribing of basic channels.
 *
 * @author skeggsc
 */
public class CluckPublisher {
    private static Logger logger = LoggerFactory.getLogger(CluckPublisher.class);

    private static long lastReportedRemoteLoggingError = 0;

    /**
     * Start a search process on the specified network. This will tell the
     * listener each time a new remote is discovered, and the EventOutput
     * returned from this method will cause the network to be rechecked for new
     * elements.
     *
     * Note that unlike the previous implementation, this implementation will
     * not send a checking message until the EventOutput is fired.
     *
     * @param node the network to search.
     * @param listener the listener to tell about new nodes.
     * @return the EventOutput that rechecks the network.
     */
    public static EventOutput setupSearching(final CluckNode node, final CluckRemoteListener listener) {
        final String local = UniqueIds.global.nextHexId("search-");
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (data.length == 2 && data[0] == RMT_PING) {
                    listener.handle(source, data[1]);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }
        }.attach(local);
        return new EventOutput() {
            public void event() {
                node.broadcast(local, new byte[] { RMT_PING }, null);
            }
        };
    }

    /**
     * Publish an EventOutput on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the EventOutput.
     * @param consumer The EventOutput.
     */
    public static void publish(final CluckNode node, String name, final EventOutput consumer) {
        new CluckRMTSubscriber(node, RMT_EVENTOUTP) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                consumer.event();
            }
        }.attach(name);
    }

    /**
     * Subscribe to an EventOutput from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @return the EventOutput.
     */
    public static EventOutput subscribeEO(final CluckNode node, final String path) {
        return new SubscribedEventOutput(node, path);
    }

    /**
     * Publish an EventInput on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the EventInput.
     * @param input The EventInput.
     */
    public static void publish(final CluckNode node, final String name, EventInput input) {
        final CopyOnWriteArrayList<String> remotes = new CopyOnWriteArrayList<String>();
        input.send(new EventOutput() {
            public void event() {
                for (String remote : remotes) {
                    node.transmit(remote, name, new byte[] { RMT_EVENTINPUTRESP });
                }
            }
        });
        new CluckSubscriber(node) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && (data[0] == RMT_NEGATIVE_ACK || data[0] == RMT_LEGACY_EVENTINPUT_UNSUB)) {
                    if (remotes.remove(src)) {
                        logger.warn("Connection cancelled to " + src + " on " + name);
                    } else {
                        logger.warn("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_EVENTINPUT) && !remotes.contains(src)) {
                    remotes.add(src);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_EVENTINPUT);
            }
        }.attach(name);
    }

    /**
     * Subscribe to an EventInput from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @return the EventInput.
     */
    public static EventInput subscribeEI(final CluckNode node, final String path) {
        final SubscribedEventInput result = new SubscribedEventInput(node, path);
        new EventInputReceiver(node, result, path).attach();
        return result;
    }

    /**
     * Publish a LoggingTarget on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the LoggingTarget.
     * @param lt The LoggingTarget.
     */
    public static void publish(final CluckNode node, String name, final LoggingTarget lt) {
        new CluckRMTSubscriber(node, RMT_LOGTARGET, 10) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                int len1 = Utils.bytesToInt(data, 2);
                int len2 = Utils.bytesToInt(data, 6);
                if (len1 + len2 + 10 != data.length) {
                    logger.warn("Bad data length to Logging Target!");
                    return;
                }
                String message = new String(data, 10, len1);
                String extended = len2 == 0 ? null : new String(data, 10 + len1, len2);
                lt.log(LogLevel.fromByte(data[1]), message, extended);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a LoggingTarget from the network at the specified path, with
     * only sending data for at least a minimum logging level.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @param minimum The minimum logging level to send over the network.
     * @return the LoggingTarget.
     */
    public static LoggingTarget subscribeLT(final CluckNode node, final String path, final LogLevel minimum) {
        return new SubscribedLoggingTarget(minimum, node, path);
    }

    /**
     * Publish a BooleanInput on the network. This will send values to clients
     * when they connect.
     *
     * @param node The node to publish on.
     * @param name The name for the BooleanInput.
     * @param input The BooleanInput.
     */
    public static void publish(final CluckNode node, final String name, final BooleanInput input) {
        final CopyOnWriteArrayList<String> remotes = new CopyOnWriteArrayList<String>();
        input.send(new BooleanInputPublishListener(name, remotes, node));
        new CluckSubscriber(node) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && (data[0] == RMT_NEGATIVE_ACK || data[0] == RMT_LEGACY_BOOLINPUT_UNSUB)) {
                    if (remotes.remove(src)) {
                        logger.warn("Connection cancelled to " + src + " on " + name);
                    } else {
                        logger.warn("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_BOOLINPUT)) {
                    remotes.add(src);
                    node.transmit(src, name, new byte[] { RMT_BOOLINPUTRESP, input.get() ? (byte) 1 : 0 });
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_BOOLINPUT);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a BooleanInput from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @param subscribeByDefault Should this request the value from the remote
     * by default, as opposed to waiting until this is needed. If this is false,
     * then readValue() won't work until you run addTarget().
     * @return the BooleanInput.
     */
    public static BooleanInput subscribeBI(final CluckNode node, final String path, final boolean subscribeByDefault) {
        final SubscribedBooleanInput result = new SubscribedBooleanInput(node, path, subscribeByDefault);
        new BooleanInputReceiver(node, result, path).attach();
        return result;
    }

    /**
     * Publish a BooleanOutput on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the BooleanOutput.
     * @param output The BooleanOutput.
     */
    public static void publish(final CluckNode node, String name, final BooleanOutput output) {
        new CluckRMTSubscriber(node, RMT_BOOLOUTP, 2) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                output.set(data[1] != 0);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a BooleanOutput from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @return the BooleanOutput.
     */
    public static BooleanOutput subscribeBO(final CluckNode node, final String path) {
        return new SubscribedBooleanOutput(node, path);
    }

    /**
     * Publish a FloatInput on the network. This will send values to clients
     * when they connect.
     *
     * @param node The node to publish on.
     * @param name The name for the FloatInput.
     * @param input The FloatInput.
     */
    public static void publish(final CluckNode node, final String name, final FloatInput input) {
        final CopyOnWriteArrayList<String> remotes = new CopyOnWriteArrayList<String>();
        input.send(new FloatInputPublishListener(remotes, name, node));
        new CluckSubscriber(node) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && (data[0] == RMT_NEGATIVE_ACK || data[0] == RMT_LEGACY_FLOATINPUT_UNSUB)) {
                    if (remotes.remove(src)) {
                        logger.warn("Connection cancelled to " + src + " on " + name);
                    } else {
                        logger.warn("Received cancellation to nonexistent " + src + " on " + name);
                    }
                } else if (requireRMT(src, data, RMT_FLOATINPUT)) {
                    remotes.add(src);
                    int iver = Float.floatToIntBits(input.get());
                    node.transmit(src, name, new byte[] { RMT_FLOATINPUTRESP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver });
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_FLOATINPUT);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a FloatInput from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @param subscribeByDefault Should this request the value from the remote
     * by default, as opposed to waiting until this is needed. If this is false,
     * then readValue() won't work until you run addTarget().
     * @return the FloatInput.
     */
    public static FloatInput subscribeFI(final CluckNode node, final String path, final boolean subscribeByDefault) {
        final SubscribedFloatInput result = new SubscribedFloatInput(node, path, subscribeByDefault);
        new FloatInputReceiver(node, result, path).attach();
        return result;
    }

    /**
     * Publish a FloatOutput on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the FloatOutput.
     * @param out The FloatOutput.
     */
    public static void publish(final CluckNode node, String name, final FloatOutput out) {
        new CluckRMTSubscriber(node, RMT_FLOATOUTP, 5) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                out.set(Utils.bytesToFloat(data, 1));
            }
        }.attach(name);
    }

    /**
     * Subscribe to a FloatOutput from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @return the FloatOutput.
     */
    public static FloatOutput subscribeFO(final CluckNode node, final String path) {
        return new SubscribedFloatOutput(node, path);
    }

    /**
     * Publish a FloatStatus on the network.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param node The node to publish on.
     * @param name The name for the FloatStatus.
     * @param stat The FloatStatus.
     */
    public static void publish(final CluckNode node, final String name, final FloatStatus stat) {
        FloatInput statInput = stat;
        FloatOutput statOutput = stat;
        publish(node, name + ".input", statInput);
        publish(node, name + ".output", statOutput);
    }

    /**
     * Publish a BooleanStatus on the network.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param node The node to publish on.
     * @param name The name for the BooleanStatus.
     * @param stat The BooleanStatus to publish.
     */
    public static void publish(final CluckNode node, final String name, BooleanStatus stat) {
        BooleanInput statInput = stat;
        BooleanOutput statOutput = stat;
        publish(node, name + ".input", statInput);
        publish(node, name + ".output", statOutput);
    }

    /**
     * Publish an EventStatus on the network.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param node The node to publish on.
     * @param name The name for the EventStatus.
     * @param stat The EventStatus to publish.
     */
    public static void publish(final CluckNode node, final String name, EventStatus stat) {
        EventInput statInput = stat;
        EventOutput statOutput = stat;
        publish(node, name + ".input", statInput);
        publish(node, name + ".output", statOutput);
    }

    /**
     * Publish an OutputStream on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the OutputStream.
     * @param out The OutputStream.
     */
    public static void publish(final CluckNode node, String name, final OutputStream out) {
        new CluckRMTSubscriber(node, RMT_OUTSTREAM) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                if (data.length > 1) {
                    try {
                        out.write(data, 1, data.length - 1);
                    } catch (IOException ex) {
                        logger.warn("IO Exception during network transfer!", ex);
                    }
                }
            }
        }.attach(name);
    }

    /**
     * Subscribe to an OutputStream from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @return the OutputStream.
     */
    public static OutputStream subscribeOS(final CluckNode node, final String path) {
        return new SubscribedObjectStream(node, path);
    }

    /**
     * Publish an RConfable device on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the RConfable.
     * @param device The RConfable.
     */
    public static void publishRConf(CluckNode node, String name, final RConfable device) {
        node.getRPCManager().publish(name + "-rpcq", new RemoteProcedure() {
            public void invoke(byte[] in, OutputStream out) {
                try {
                    RConf.Entry[] data;
                    try {
                        data = device.queryRConf();
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    short count = (short) data.length;
                    if (count != data.length) {
                        logger.warn("Too many fields in RConf query response!");
                        count = Short.MAX_VALUE;
                    }
                    try {
                        out.write(new byte[] { (byte) (count >> 8), (byte) count });
                        for (int i = 0; i < count; i++) {
                            // plus one for the type
                            int len = data[i].contents.length + 1;
                            out.write(new byte[] { (byte) (len >> 24), (byte) (len >> 16), (byte) (len >> 8), (byte) len, data[i].type });
                            out.write(data[i].contents);
                        }
                    } catch (IOException e) {
                        logger.warn("IOException during response to RConf query!", e);
                    }
                } finally {
                    try {
                        out.close();
                    } catch (IOException e) {
                        logger.warn("IOException during RConf close!", e);
                    }
                }
            }
        });
        node.getRPCManager().publish(name + "-rpcs", new RemoteProcedure() {
            public void invoke(byte[] in, OutputStream out) {
                try {
                    if (in.length < 2) {
                        logger.warn("Invalid message to RConf signal node!");
                        return;
                    }
                    byte[] data = new byte[in.length - 2];
                    System.arraycopy(in, 2, data, 0, data.length);
                    try {
                        boolean success = device.signalRConf(((in[0] & 0xFF) << 8) | (in[1] & 0xFF), data);
                        try {
                            out.write(success ? (byte) 1 : (byte) 0);
                        } catch (IOException e) {
                            logger.warn("IOException during response to RConf signal!", e);
                        }
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    try {
                        out.close();
                    } catch (IOException e) {
                        logger.warn("IOException during response to RConf signal!", e);
                    }
                }
            }
        });
    }

    /**
     * Subscribe to an RConfable device from the network at the specified path.
     *
     * @param node The node to subscribe from.
     * @param path The path to subscribe to.
     * @param timeout The maximum wait time for the RPC calls.
     * @return the RConfable.
     */
    public static RConfable subscribeRConf(CluckNode node, String path, final int timeout) {
        final RemoteProcedure query = node.getRPCManager().subscribe(path + "-rpcq", timeout);
        final RemoteProcedure signal = node.getRPCManager().subscribe(path + "-rpcs", timeout);
        return new SubscribedRConfable(query, timeout, signal);
    }

    private CluckPublisher() {
    }

    private static final class SubscribedRConfable implements RConfable, Serializable {
        private static final long serialVersionUID = 6975429475672969797L;
        private final RemoteProcedure query;
        private final int timeout;
        private final RemoteProcedure signal;

        private SubscribedRConfable(RemoteProcedure query, int timeout, RemoteProcedure signal) {
            this.query = query;
            this.timeout = timeout;
            this.signal = signal;
        }

        public boolean signalRConf(int field, byte[] data) throws InterruptedException {
            byte[] ndata = new byte[data.length + 2];
            if (field != (field & 0xFFFF)) {
                logger.warn("Out of range field in RConf query response!");
                return false;
            }
            ndata[0] = (byte) (field >> 8);
            ndata[1] = (byte) field;
            System.arraycopy(data, 0, ndata, 2, data.length);
            byte[] result = SimpleProcedure.invoke(signal, ndata, timeout);
            if (result == SimpleProcedure.TIMED_OUT) {
                return false;
            } else if (result.length >= 1 && result[0] == 0) {
                return false;
            }
            return true;
        }

        public Entry[] queryRConf() throws InterruptedException {
            byte[] data = SimpleProcedure.invoke(query, new byte[0], timeout);
            if (data == SimpleProcedure.TIMED_OUT) {
                return null;
            }
            if (data.length < 2) {
                logger.warn("Too-short (1) RConf query response!");
                return null;
            }
            Entry[] out = new Entry[((data[0] & 0xFF) << 8) | (data[1] & 0xFF)];
            int ptr = 2;
            for (int i = 0; i < out.length; i++) {
                if (data.length - ptr < 4) {
                    logger.warn("Too-short (2) RConf query response!");
                    return null;
                }
                int len = ((data[ptr] & 0xFF) << 24) | ((data[ptr + 1] & 0xFF) << 16) | ((data[ptr + 2] & 0xFF) << 8) | (data[ptr + 3] & 0xFF);
                byte type = data[ptr + 4];
                byte[] part = new byte[len - 1];
                ptr += 5;
                if (data.length - ptr < part.length) {
                    logger.warn("Too-short (3) RConf query response!");
                    return null;
                }
                System.arraycopy(data, ptr, part, 0, part.length);
                ptr += part.length;
                out[i] = new RConf.Entry(type, part);
            }
            if (ptr != data.length) {
                logger.warn("Too-long RConf query response!");
                return null;
            }
            return out;
        }
    }

    private static final class FloatInputPublishListener implements FloatOutput, Serializable {
        private static final long serialVersionUID = 1432024738866192130L;
        private final CopyOnWriteArrayList<String> remotes;
        private final String name;
        private final CluckNode node;

        private FloatInputPublishListener(CopyOnWriteArrayList<String> remotes, String name, CluckNode node) {
            this.remotes = remotes;
            this.name = name;
            this.node = node;
        }

        public void set(float value) {
            for (String remote : remotes) {
                int iver = Float.floatToIntBits(value);
                node.transmit(remote, name, new byte[] { RMT_FLOATINPUTRESP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver });
            }
        }
    }

    private static final class BooleanInputPublishListener implements BooleanOutput, Serializable {
        private static final long serialVersionUID = -7563622859541688219L;
        private final String name;
        private final CopyOnWriteArrayList<String> remotes;
        private final CluckNode node;

        private BooleanInputPublishListener(String name, CopyOnWriteArrayList<String> remotes, CluckNode node) {
            this.name = name;
            this.remotes = remotes;
            this.node = node;
        }

        public void set(boolean value) {
            for (String remote : remotes) {
                node.transmit(remote, name, new byte[] { RMT_BOOLINPUTRESP, value ? (byte) 1 : 0 });
            }
        }
    }

    private static class SubscribedLoggingTarget implements LoggingTarget, Serializable {

        private static final long serialVersionUID = 5342629979840268661L;
        private final LogLevel minimum;
        private final CluckNode node;
        private final String path;

        SubscribedLoggingTarget(LogLevel minimum, CluckNode node, String path) {
            this.minimum = minimum;
            this.node = node;
            this.path = path;
        }

        public void log(LogLevel level, String message, Throwable throwable) {
            log(level, message, Utils.toStringThrowable(throwable));
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
                    node.transmit(path, null, out);
                }
            } catch (Throwable thr) {
                // We use System.currentTimeMillis() instead of
                // Time.currentTimeMillis() because this is only to prevent
                // message spam.
                if (System.currentTimeMillis() - lastReportedRemoteLoggingError > 500) {
                    logger.error("[LOCAL] Error during remote log", thr);
                    lastReportedRemoteLoggingError = System.currentTimeMillis();
                }
            }
        }
    }

    private static class SubscribedFloatInput extends FloatStatus {

        private static final long serialVersionUID = 1031666017588055705L;

        private boolean sent;
        private final CluckNode node;
        private final String path;
        private transient String linkName;
        private final boolean canUnsubscribe;

        SubscribedFloatInput(CluckNode node, String path, boolean subscribeByDefault) {
            this.sent = subscribeByDefault;
            this.node = node;
            this.path = path;
            generateLinkName();
            this.canUnsubscribe = !subscribeByDefault;
            if (subscribeByDefault) {
                node.transmit(path, linkName, new byte[] { RMT_FLOATINPUT });
            }
        }

        @Override
        public synchronized void onUpdate(EventOutput out) {
            super.onUpdate(out);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[] { RMT_FLOATINPUT });
            }
        }

        @Override
        public synchronized EventOutput onUpdateR(EventOutput out) {
            EventOutput base = super.onUpdateR(out);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[] { RMT_FLOATINPUT });
            }
            return base.combine(() -> {
                synchronized (SubscribedFloatInput.this) {
                    if (canUnsubscribe && sent && !this.hasConsumers()) {
                        sent = false;
                        node.transmit(path, linkName, new byte[] { RMT_NEGATIVE_ACK });
                    }
                }
            });
        }

        private synchronized boolean shouldResend() {
            return sent;
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            generateLinkName();
            new FloatInputReceiver(node, this, path).attach();
        }

        private void generateLinkName() {
            linkName = UniqueIds.global.nextHexId("srcFI");
        }

        public String getLinkName() {
            return linkName;
        }
    }

    private static class FloatInputReceiver extends CluckRMTSubscriber {

        private final SubscribedFloatInput result;
        private final String path;
        private final String linkName;

        FloatInputReceiver(CluckNode node, SubscribedFloatInput result, String path) {
            super(node, RMT_FLOATINPUTRESP, 5);
            this.result = result;
            this.path = path;
            this.linkName = result.getLinkName();
        }

        @Override
        protected void receiveValid(String src, byte[] data) {
            if (!path.equals(src)) {
                logger.warn("Bad source to " + linkName + ": " + src + " instead of " + path);
            } else {
                result.set(Utils.bytesToFloat(data, 1));
            }
        }

        @Override
        protected void receiveBroadcast(String source, byte[] data) {
            if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                if (result.shouldResend()) {
                    node.transmit(path, linkName, new byte[] { RMT_FLOATINPUT });
                }
            }
        }

        public void attach() {
            attach(linkName);
        }
    }

    private static class SubscribedBooleanInput extends BooleanStatus {

        private static final long serialVersionUID = 6685907502662588221L;

        private boolean sent;
        private final CluckNode node;
        private final String path;
        private transient String linkName;
        private final boolean canUnsubscribe;

        SubscribedBooleanInput(CluckNode node, String path, boolean subscribeByDefault) {
            this.sent = subscribeByDefault;
            this.node = node;
            this.path = path;
            this.canUnsubscribe = !subscribeByDefault;
            generateLinkName();
            if (subscribeByDefault) {
                node.transmit(path, linkName, new byte[] { RMT_BOOLINPUT });
            }
        }

        @Override
        public synchronized void onUpdate(EventOutput out) {
            super.onUpdate(out);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[] { RMT_BOOLINPUT });
            }
        }

        @Override
        public synchronized EventOutput onUpdateR(EventOutput cns) {
            EventOutput base = super.onUpdateR(cns);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[] { RMT_BOOLINPUT });
            }
            return base.combine(() -> {
                synchronized (SubscribedBooleanInput.this) {
                    if (canUnsubscribe && sent && !this.hasConsumers()) {
                        sent = false;
                        node.transmit(path, linkName, new byte[] { RMT_NEGATIVE_ACK });
                    }
                }
            });
        }

        private synchronized boolean shouldResend() {
            return sent;
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            generateLinkName();
            new BooleanInputReceiver(node, this, path).attach();
        }

        private void generateLinkName() {
            linkName = UniqueIds.global.nextHexId("srcBI");
        }

        public String getLinkName() {
            return linkName;
        }
    }

    private static class BooleanInputReceiver extends CluckRMTSubscriber {

        private final SubscribedBooleanInput result;
        private final String path;
        private final String linkName;

        BooleanInputReceiver(CluckNode node, SubscribedBooleanInput result, String path) {
            super(node, RMT_BOOLINPUTRESP, 2);
            this.result = result;
            this.path = path;
            this.linkName = result.getLinkName();
        }

        @Override
        protected void receiveValid(String src, byte[] data) {
            if (!path.equals(src)) {
                logger.warn("Bad source to " + linkName + ": " + src + " instead of " + path);
            } else {
                result.set(data[1] != 0);
            }
        }

        @Override
        protected void receiveBroadcast(String source, byte[] data) {
            if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                if (result.shouldResend()) {
                    node.transmit(path, linkName, new byte[] { RMT_BOOLINPUT });
                }
            }
        }

        public void attach() {
            attach(linkName);
        }
    }

    private static class SubscribedEventInput extends EventStatus {

        private static final long serialVersionUID = -4051785233205840392L;

        private boolean sent;
        private final CluckNode node;
        private final String path;
        private transient String linkName;

        SubscribedEventInput(CluckNode node, String path) {
            this.node = node;
            this.path = path;
            generateLinkName();
        }

        @Override
        public synchronized void onUpdate(EventOutput cns) {
            super.onUpdate(cns);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[] { RMT_EVENTINPUT });
            }
        }

        @Override
        public synchronized EventOutput onUpdateR(EventOutput cns) {
            EventOutput base = super.onUpdateR(cns);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[] { RMT_EVENTINPUT });
            }
            return base.combine(() -> {
                synchronized (SubscribedEventInput.this) {
                    if (sent && !this.hasConsumers()) {
                        sent = false;
                        node.transmit(path, linkName, new byte[] { RMT_NEGATIVE_ACK });
                    }
                }
            });
        }

        private synchronized boolean shouldResend() {
            return sent;
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            generateLinkName();
            new EventInputReceiver(node, this, path).attach();
        }

        private void generateLinkName() {
            this.linkName = UniqueIds.global.nextHexId("srcES");
        }

        public String getLinkName() {
            return linkName;
        }
    }

    private static class EventInputReceiver extends CluckRMTSubscriber {

        private final SubscribedEventInput result;
        private final String path;
        private final String linkName;

        EventInputReceiver(CluckNode node, SubscribedEventInput result, String path) {
            super(node, RMT_EVENTINPUTRESP);
            this.result = result;
            this.path = path;
            this.linkName = result.getLinkName();
        }

        @Override
        protected void receiveValid(String src, byte[] data) {
            if (!path.equals(src)) {
                logger.warn("Bad source to " + linkName + ": " + src + " instead of " + path);
            } else {
                result.produce();
            }
        }

        @Override
        protected void receiveBroadcast(String source, byte[] data) {
            if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                if (result.shouldResend()) {
                    node.transmit(path, linkName, new byte[] { RMT_EVENTINPUT });
                }
            }
        }

        public void attach() {
            this.attach(linkName);
        }
    }

    private static class SubscribedEventOutput implements EventOutput, Serializable {

        private static final long serialVersionUID = 5103577228341124318L;
        private final CluckNode node;
        private final String path;

        SubscribedEventOutput(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

        public void event() {
            node.transmit(path, null, new byte[] { RMT_EVENTOUTP });
        }
    }

    private static class SubscribedBooleanOutput implements BooleanOutput, Serializable {

        private static final long serialVersionUID = -4068385997161728204L;
        private final CluckNode node;
        private final String path;

        SubscribedBooleanOutput(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

        public void set(boolean b) {
            node.transmit(path, null, new byte[] { RMT_BOOLOUTP, b ? (byte) 1 : 0 });
        }
    }

    private static class SubscribedFloatOutput implements FloatOutput, Serializable {

        private static final long serialVersionUID = -4377296771561862860L;
        private final CluckNode node;
        private final String path;

        SubscribedFloatOutput(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

        public void set(float f) {
            int iver = Float.floatToIntBits(f);
            node.transmit(path, null, new byte[] { RMT_FLOATOUTP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver });
        }
    }

    private static class SubscribedObjectStream extends OutputStream implements Serializable {

        private static final long serialVersionUID = -9002013295388072459L;
        private final CluckNode node;
        private final String path;

        SubscribedObjectStream(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

        @Override
        public void write(int b) throws IOException {
            node.transmit(path, null, new byte[] { RMT_OUTSTREAM, (byte) b });
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            byte[] newbyteout = new byte[len + 1];
            newbyteout[0] = RMT_OUTSTREAM;
            System.arraycopy(b, off, newbyteout, 1, len);
            node.transmit(path, null, newbyteout);
        }
    }

}

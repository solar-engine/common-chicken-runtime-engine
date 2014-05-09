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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import static ccre.cluck.CluckNode.RMT_BOOLOUTP;
import static ccre.cluck.CluckNode.RMT_BOOLPROD;
import static ccre.cluck.CluckNode.RMT_BOOLPRODRESP;
import static ccre.cluck.CluckNode.RMT_EVENTCONSUMER;
import static ccre.cluck.CluckNode.RMT_EVENTSOURCE;
import static ccre.cluck.CluckNode.RMT_EVENTSOURCERESP;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD;
import static ccre.cluck.CluckNode.RMT_FLOATPRODRESP;
import static ccre.cluck.CluckNode.RMT_LOGTARGET;
import static ccre.cluck.CluckNode.RMT_NEGATIVE_ACK;
import static ccre.cluck.CluckNode.RMT_OUTSTREAM;
import ccre.concurrency.ConcurrentDispatchArray;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.util.UniqueIds;
import ccre.util.Utils;
import ccre.workarounds.ThrowablePrinter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A file that handles publishing and subscribing of basic channels.
 *
 * @author skeggsc
 */
public class CluckPublisher {

    private static long lastReportedRemoteLoggingError = 0;

    /**
     * Publish an EventConsumer on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the EventConsumer.
     * @param consumer The EventConsumer.
     */
    public static void publish(final CluckNode node, String name, final EventOutput consumer) {
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_EVENTCONSUMER)) {
                    consumer.event();
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_EVENTCONSUMER); // TODO: Can I make these anonymous classes simpler?
            }
        }.attach(name);
    }

    /**
     * Subscribe to an EventConsumer from the network at the specified path.
     *
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @return the EventConsumer.
     */
    public static EventOutput subscribeEC(final CluckNode node, final String path) {
        return new EventOutput() {
            public void event() {
                node.transmit(path, null, new byte[]{RMT_EVENTCONSUMER});
            }
        };
    }

    /**
     * Publish an EventSource on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the EventSource.
     * @param source The EventSource.
     */
    public static void publish(final CluckNode node, final String name, EventInput source) {
        final ConcurrentDispatchArray<String> remotes = new ConcurrentDispatchArray<String>();
        source.send(new EventOutput() {
            public void event() {
                for (String remote : remotes) {
                    node.transmit(remote, name, new byte[]{RMT_EVENTSOURCERESP});
                }
            }
        });
        new CluckSubscriber(node) {
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
     * Subscribe to an EventSource from the network at the specified path.
     *
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @return the EventSource.
     */
    public static EventInput subscribeES(final CluckNode node, final String path) {
        final String linkName = "srcES-" + path.hashCode() + "-" + UniqueIds.global.nextHexId();
        final BooleanStatus sent = new BooleanStatus();
        final EventStatus e = new EventStatus() {
            @Override
            public void send(EventOutput cns) {
                super.send(cns);
                if (!sent.get()) {
                    sent.set(true);
                    node.transmit(path, linkName, new byte[]{RMT_EVENTSOURCE});
                }
            }
        };
        new CluckSubscriber(node) {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_EVENTSOURCERESP)) {
                    e.produce();
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    if (sent.get()) {
                        node.transmit(path, linkName, new byte[]{RMT_EVENTSOURCE});
                    }
                }
            }
        }.attach(linkName);
        return e;
    }

    /**
     * Publish a LoggingTarget on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the LoggingTarget.
     * @param lt The LoggingTarget.
     */
    public static void publish(final CluckNode node, String name, final LoggingTarget lt) {
        new CluckSubscriber(node) {
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
                    String message = new String(data, 10, l1);
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

    /**
     * Subscribe to a LoggingTarget from the network at the specified path, with
     * only sending data for at least a minimum logging level.
     *
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @param minimum The minimum logging level to send over the network.
     * @return the LoggingTarget.
     */
    public static LoggingTarget subscribeLT(final CluckNode node, final String path, final LogLevel minimum) {
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
                        node.transmit(path, null, out);
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
     * Publish a BooleanInput on the network. This will send values to clients
     * when they connect.
     *
     * @param node The node to publish on.
     * @param name The name for the BooleanInput.
     * @param input The BooleanInput.
     */
    public static void publish(final CluckNode node, final String name, final BooleanInput input) {
        final ConcurrentDispatchArray<String> remotes = new ConcurrentDispatchArray<String>();
        input.send(new BooleanOutput() {
            public void set(boolean value) {
                for (String remote : remotes) {
                    node.transmit(remote, name, new byte[]{RMT_BOOLPRODRESP, value ? (byte) 1 : 0});
                }
            }
        });
        new CluckSubscriber(node) {
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
                    node.transmit(src, name, new byte[]{RMT_BOOLPRODRESP, input.get() ? (byte) 1 : 0});
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_BOOLPROD);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a BooleanInput from the network at the specified path.
     *
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @param shouldSubscribeByDefault Should this request the value from the
     * remote by default, as opposed to waiting until this is needed. If this is
     * false, then readValue() won't work until you run addTarget().
     * @return the BooleanInput.
     */
    public static BooleanInput subscribeBI(final CluckNode node, final String path, boolean shouldSubscribeByDefault) {
        final String linkName = "srcBI-" + path.hashCode() + "-" + UniqueIds.global.nextHexId();
        final BooleanStatus sent = new BooleanStatus(shouldSubscribeByDefault);
        final BooleanStatus result = new BooleanStatus() {
            @Override
            public void send(BooleanOutput out) {
                super.send(out);
                if (!sent.get()) {
                    sent.set(true);
                    node.transmit(path, linkName, new byte[]{RMT_BOOLPROD});
                }
            }
        };
        if (shouldSubscribeByDefault) {
            node.transmit(path, linkName, new byte[]{RMT_BOOLPROD});
        }
        new CluckSubscriber(node) {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_BOOLPRODRESP)) {
                    if (data.length < 2) {
                        Logger.warning("Not enough bytes for boolean producer response!");
                        return;
                    }
                    result.set(data[1] != 0);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    if (sent.get()) {
                        node.transmit(path, linkName, new byte[]{RMT_BOOLPROD});
                    }
                }
            }
        }.attach(linkName);
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
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_BOOLOUTP)) {
                    if (data.length < 2) {
                        Logger.warning("Not enough bytes for boolean output!");
                        return;
                    }
                    output.set(data[1] != 0);
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
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @return the BooleanOutput.
     */
    public static BooleanOutput subscribeBO(final CluckNode node, final String path) {
        return new BooleanOutput() {
            public void set(boolean b) {
                node.transmit(path, null, new byte[]{RMT_BOOLOUTP, b ? (byte) 1 : 0});
            }
        };
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
        final ConcurrentDispatchArray<String> remotes = new ConcurrentDispatchArray<String>();
        input.send(new FloatOutput() {
            public void set(float value) {
                for (String remote : remotes) {
                    int iver = Float.floatToIntBits(value);
                    node.transmit(remote, name, new byte[]{RMT_FLOATPRODRESP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
                }
            }
        });
        new CluckSubscriber(node) {
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
                    int iver = Float.floatToIntBits(input.get());
                    node.transmit(src, name, new byte[]{RMT_FLOATPRODRESP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_FLOATPROD);
            }
        }.attach(name);
    }

    /**
     * Subscribe to a FloatInput from the network at the specified path.
     *
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @param subscribeByDefault Should this request the value from the remote
     * by default, as opposed to waiting until this is needed. If this is false,
     * then readValue() won't work until you run addTarget().
     * @return the FloatInput.
     */
    public static FloatInput subscribeFI(final CluckNode node, final String path, boolean subscribeByDefault) {
        final String linkName = "srcFI-" + path.hashCode() + "-" + UniqueIds.global.nextHexId();
        final BooleanStatus sent = new BooleanStatus(subscribeByDefault);
        final FloatStatus result = new FloatStatus() {
            @Override
            public void send(FloatOutput out) {
                super.send(out);
                if (!sent.get()) {
                    sent.set(true);
                    node.transmit(path, linkName, new byte[]{RMT_FLOATPROD});
                }
            }
        };
        if (subscribeByDefault) {
            node.transmit(path, linkName, new byte[]{RMT_FLOATPROD});
        }
        new CluckSubscriber(node) {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_FLOATPRODRESP)) {
                    if (data.length < 5) {
                        Logger.warning("Not enough bytes for float producer response!");
                        return;
                    }
                    result.set(Utils.bytesToFloat(data, 1));
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    if (sent.get()) {
                        node.transmit(path, linkName, new byte[]{RMT_FLOATPROD});
                    }
                }
            }
        }.attach(linkName);
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
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_FLOATOUTP)) {
                    if (data.length < 5) { // TODO: Can this be part of the superclass?
                        Logger.warning("Not enough bytes for float output!");
                        return;
                    }
                    out.set(Utils.bytesToFloat(data, 1));
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
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @return the FloatOutput.
     */
    public static FloatOutput subscribeFO(final CluckNode node, final String path) {
        return new FloatOutput() {
            public void set(float f) {
                int iver = Float.floatToIntBits(f); // TODO: Can float->byte and similar operations be in a utility class?
                node.transmit(path, null, new byte[]{RMT_FLOATOUTP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
            }
        };
    }

    /**
     * Publish a FloatStatus on the network.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param node The node to publish on.
     * @param name The name for the FloatStatus.
     * @param tune The FloatStatus.
     */
    public static void publish(final CluckNode node, final String name, final FloatStatus tune) {
        publish(node, name + ".input", (FloatInput) tune);
        publish(node, name + ".output", (FloatOutput) tune);
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
        publish(node, name + ".input", (BooleanInput) stat);
        publish(node, name + ".output", (BooleanOutput) stat);
    }

    /**
     * Publish an OutputStream on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the OutputStream.
     * @param out The OutputStream.
     */
    public static void publish(final CluckNode node, String name, final OutputStream out) {
        new CluckSubscriber(node) {
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
     * @param node The node to publish on.
     * @param path The path to subscribe to.
     * @return the OutputStream.
     */
    public static OutputStream subscribeOS(final CluckNode node, final String path) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                node.transmit(path, null, new byte[]{RMT_OUTSTREAM, (byte) b});
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                byte[] newbyteout = new byte[len + 1];
                newbyteout[0] = RMT_OUTSTREAM;
                System.arraycopy(b, off, newbyteout, 1, len);
                node.transmit(path, null, newbyteout);
            }
        };
    }

    private CluckPublisher() {
    }
}

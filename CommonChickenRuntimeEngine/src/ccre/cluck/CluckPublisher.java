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
import static ccre.cluck.CluckNode.RMT_BOOLPROD_UNSUB;
import static ccre.cluck.CluckNode.RMT_EVENTINPUT;
import static ccre.cluck.CluckNode.RMT_EVENTINPUTRESP;
import static ccre.cluck.CluckNode.RMT_EVENTINPUT_UNSUB;
import static ccre.cluck.CluckNode.RMT_EVENTOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD;
import static ccre.cluck.CluckNode.RMT_FLOATPRODRESP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD_UNSUB;
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
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * A file that handles publishing and subscribing of basic channels.
 *
 * @author skeggsc
 */
public class CluckPublisher {

    private static long lastReportedRemoteLoggingError = 0;

    /**
     * Publish an EventOutput on the network.
     *
     * @param node The node to publish on.
     * @param name The name for the EventOutput.
     * @param consumer The EventOutput.
     */
    public static void publish(final CluckNode node, String name, final EventOutput consumer) {
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_EVENTOUTP)) {
                    consumer.event();
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_EVENTOUTP); // TODO: Can I make these anonymous classes simpler?
            }
        }.attach(name);
    }

    /**
     * Subscribe to an EventOutput from the network at the specified path.
     *
     * @param node The node to publish on.
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
     * @param source The EventInput.
     */
    public static void publish(final CluckNode node, final String name, EventInput source) {
        final ConcurrentDispatchArray<String> remotes = new ConcurrentDispatchArray<String>();
        source.send(new EventOutput() {
            public void event() {
                for (String remote : remotes) {
                    node.transmit(remote, name, new byte[]{RMT_EVENTINPUTRESP});
                }
            }
        });
        new CluckSubscriber(node) {
            @Override
            protected void receive(String src, byte[] data) {
                if (data.length != 0 && (data[0] == RMT_NEGATIVE_ACK || data[0] == RMT_EVENTINPUT_UNSUB)) {
                    if (remotes.remove(src)) {
                        Logger.warning("Connection cancelled to " + src + " on " + name);
                    } else {
                        Logger.warning("Received cancellation to nonexistent " + src + " on " + name);
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
     * @param node The node to publish on.
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
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_LOGTARGET, 10)) {
                    int len1 = Utils.bytesToInt(data, 2);
                    int len2 = Utils.bytesToInt(data, 6);
                    if (len1 + len2 + 10 != data.length) {
                        Logger.warning("Bad data length to Logging Target!");
                        return;
                    }
                    String message = new String(data, 10, len1);
                    String extended = len2 == 0 ? null : new String(data, 10 + len1, len2);
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
                if (data.length != 0 && (data[0] == RMT_NEGATIVE_ACK || data[0] == RMT_BOOLPROD_UNSUB)) {
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
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_BOOLOUTP, 2)) {
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
                if (data.length != 0 && (data[0] == RMT_NEGATIVE_ACK || data[0] == RMT_FLOATPROD_UNSUB)) {
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
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_FLOATOUTP, 5)) {
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
        return new SubscribedFloatOutput(node, path);
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
                if (requireRMT(source, data, RMT_OUTSTREAM) && data.length > 1) {
                    try {
                        out.write(data, 1, data.length - 1);
                    } catch (IOException ex) {
                        Logger.warning("IO Exception during network transfer!", ex);
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
        return new SubscribedObjectStream(node, path);
    }

    private CluckPublisher() {
    }

    private static class SubscribedLoggingTarget implements LoggingTarget, Serializable {

        private final LogLevel minimum;
        private final CluckNode node;
        private final String path;

        SubscribedLoggingTarget(LogLevel minimum, CluckNode node, String path) {
            this.minimum = minimum;
            this.node = node;
            this.path = path;
        }

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
                    Logger.severe("Error during remote log", thr);
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
                node.transmit(path, linkName, new byte[]{RMT_FLOATPROD});
            }
        }

        @Override
        public synchronized void send(FloatOutput out) {
            super.send(out);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[]{RMT_FLOATPROD});
            }
        }

        @Override
        public synchronized void unsend(FloatOutput cns) {
            super.unsend(cns);
            if (canUnsubscribe && sent && !this.hasConsumers()) {
                sent = false;
                node.transmit(path, linkName, new byte[]{RMT_FLOATPROD_UNSUB});
            }
        }

        private boolean shouldResend() {
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

        /**
         * @return the linkName
         */
        public String getLinkName() {
            return linkName;
        }
    }

    private static class FloatInputReceiver extends CluckSubscriber {

        private final SubscribedFloatInput result;
        private final String path;
        private final String linkName;

        FloatInputReceiver(CluckNode node, SubscribedFloatInput result, String path) {
            super(node);
            this.result = result;
            this.path = path;
            this.linkName = result.getLinkName();
        }

        @Override
        protected void receive(String src, byte[] data) {
            if (requireRMT(src, data, RMT_FLOATPRODRESP, 5)) {
                result.set(Utils.bytesToFloat(data, 1));
            }
        }

        @Override
        protected void receiveBroadcast(String source, byte[] data) {
            if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                if (result.shouldResend()) {
                    node.transmit(path, linkName, new byte[]{RMT_FLOATPROD});
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
                node.transmit(path, linkName, new byte[]{RMT_BOOLPROD});
            }
        }

        @Override
        public synchronized void send(BooleanOutput out) {
            super.send(out);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[]{RMT_BOOLPROD});
            }
        }

        @Override
        public synchronized void unsend(BooleanOutput cns) {
            super.unsend(cns);
            if (canUnsubscribe && sent && !this.hasConsumers()) {
                sent = false;
                node.transmit(path, linkName, new byte[]{RMT_BOOLPROD_UNSUB});
            }
        }

        private boolean shouldResend() {
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

        /**
         * @return the linkName
         */
        public String getLinkName() {
            return linkName;
        }
    }

    private static class BooleanInputReceiver extends CluckSubscriber {

        private final SubscribedBooleanInput result;
        private final String path;
        private final String linkName;

        BooleanInputReceiver(CluckNode node, SubscribedBooleanInput result, String path) {
            super(node);
            this.result = result;
            this.path = path;
            this.linkName = result.getLinkName();
        }

        @Override
        protected void receive(String src, byte[] data) {
            if (requireRMT(src, data, RMT_BOOLPRODRESP, 2)) {
                result.set(data[1] != 0);
            }
        }

        @Override
        protected void receiveBroadcast(String source, byte[] data) {
            if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                if (result.shouldResend()) {
                    node.transmit(path, linkName, new byte[]{RMT_BOOLPROD});
                }
            }
        }

        public void attach() {
            attach(linkName);
        }
    }

    private static class SubscribedEventInput extends EventStatus { // TODO: Links not removed on unload!

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
        public synchronized void send(EventOutput cns) {
            super.send(cns);
            if (!sent) {
                sent = true;
                node.transmit(path, linkName, new byte[]{RMT_EVENTINPUT});
            }
        }

        @Override
        public synchronized void unsend(EventOutput cns) {
            super.unsend(cns);
            if (sent && !this.hasConsumers()) {
                sent = false;
                node.transmit(path, linkName, new byte[]{RMT_EVENTINPUT_UNSUB});
            }
        }

        private boolean shouldResend() {
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

        /**
         * @return the linkName
         */
        public String getLinkName() {
            return linkName;
        }
    }

    private static class EventInputReceiver extends CluckSubscriber {

        private final SubscribedEventInput result;
        private final String path;
        private final String linkName;

        EventInputReceiver(CluckNode node, SubscribedEventInput result, String path) {
            super(node);
            this.result = result;
            this.path = path;
            this.linkName = result.getLinkName();
        }

        @Override
        protected void receive(String src, byte[] data) {
            if (requireRMT(src, data, RMT_EVENTINPUTRESP)) {
                result.produce();
            }
        }

        @Override
        protected void receiveBroadcast(String source, byte[] data) {
            if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                if (result.shouldResend()) {
                    node.transmit(path, linkName, new byte[]{RMT_EVENTINPUT});
                }
            }
        }

        public void attach() {
            this.attach(linkName);
        }
    }

    private static class SubscribedEventOutput implements EventOutput, Serializable {

        private final CluckNode node;
        private final String path;

        SubscribedEventOutput(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

        public void event() {
            node.transmit(path, null, new byte[]{RMT_EVENTOUTP});
        }
    }

    private static class SubscribedBooleanOutput implements BooleanOutput, Serializable {

        private final CluckNode node;
        private final String path;

        SubscribedBooleanOutput(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

        public void set(boolean b) {
            node.transmit(path, null, new byte[]{RMT_BOOLOUTP, b ? (byte) 1 : 0});
        }
    }

    private static class SubscribedFloatOutput implements FloatOutput, Serializable {

        private final CluckNode node;
        private final String path;

        SubscribedFloatOutput(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

        public void set(float f) {
            int iver = Float.floatToIntBits(f); // TODO: Can float->byte and similar operations be in a utility class?
            node.transmit(path, null, new byte[]{RMT_FLOATOUTP, (byte) (iver >> 24), (byte) (iver >> 16), (byte) (iver >> 8), (byte) iver});
        }
    }

    private static class SubscribedObjectStream extends OutputStream implements Serializable {

        private final CluckNode node;
        private final String path;

        SubscribedObjectStream(CluckNode node, String path) {
            this.node = node;
            this.path = path;
        }

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
    }

}

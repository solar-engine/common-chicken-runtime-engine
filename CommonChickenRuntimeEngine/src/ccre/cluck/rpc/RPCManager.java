/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.cluck.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.cluck.CluckNode;
import ccre.cluck.CluckSubscriber;
import ccre.time.Time;
import ccre.util.UniqueIds;

/**
 * A manager for the RPC subsystem in Cluck.
 *
 * @author skeggsc
 */
public final class RPCManager implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(RPCManager.class);

    private static final long serialVersionUID = -2530136013743162226L;
    private final CluckNode node;
    /**
     * The local end of the RPC binding. See the methods for publishing and
     * subscribing to RemoteProcedures.
     */
    private final String localRPCBinding;
    private final HashMap<String, OutputStream> bindings = new HashMap<String, OutputStream>();
    private final HashMap<String, Long> timeouts = new HashMap<String, Long>();

    /**
     * Create a new RPCManager for the specified node.
     *
     * @param node The node to attach to.
     */
    public RPCManager(CluckNode node) {
        this.node = node;
        localRPCBinding = UniqueIds.global.nextHexId("rpc-endpoint");
        final HashMap<String, OutputStream> localBindings = this.bindings;
        final HashMap<String, Long> localTimeouts = this.timeouts;
        new CluckSubscriber(node) {
            @Override
            protected void receive(String source, byte[] data) {
                logger.warn("Message to RPC endpoint!");
            }

            @Override
            protected void handleOther(String dest, String source, byte[] data) {
                if (requireRMT(source, data, CluckNode.RMT_INVOKE_REPLY)) {
                    checkRPCTimeouts();
                    OutputStream stream;
                    synchronized (RPCManager.this) {
                        stream = localBindings.get(dest);
                    }
                    if (stream == null) {
                        logger.warn("No RPC binding for: " + dest);
                    } else {
                        try {
                            stream.write(data, 1, data.length - 1);
                            stream.close();
                        } catch (IOException ex) {
                            logger.warn("Exception in RPC response write!", ex);
                        }
                        synchronized (RPCManager.this) {
                            localBindings.remove(dest);
                            localTimeouts.remove(dest);
                        }
                    }
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }
        }.attach(localRPCBinding);
    }

    /**
     * Publish a RemoteProcedure on the network.
     *
     * @param name The name for the RemoteProcedure.
     * @param proc The RemoteProcedure.
     */
    public void publish(final String name, final RemoteProcedure proc) {
        new CluckSubscriber(node) {
            @Override
            protected void receive(final String source, byte[] data) {
                if (requireRMT(source, data, CluckNode.RMT_INVOKE)) {
                    checkRPCTimeouts();
                    byte[] sdata = new byte[data.length - 1];
                    System.arraycopy(data, 1, sdata, 0, sdata.length);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream() {
                        private boolean sent;

                        @Override
                        public void close() {
                            if (sent) {
                                throw new IllegalStateException("Already sent!");
                            }
                            sent = true;
                            node.transmit(source, name, toByteArray());
                        }
                    };
                    baos.write(CluckNode.RMT_INVOKE_REPLY);
                    proc.invoke(sdata, baos);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, CluckNode.RMT_INVOKE);
            }
        }.attach(name);
    }

    /**
     * Check to see if any RPC calls have timed out and cancel them if they
     * have. This is only called when another RPC event occurs, so it may take a
     * while for the timeout to happen.
     */
    void checkRPCTimeouts() {
        long now = Time.currentTimeMillis();
        ArrayList<String> toRemove = new ArrayList<String>();
        synchronized (this) {
            for (String key : timeouts.keySet()) {
                long value = timeouts.get(key);
                if (value < now) {
                    toRemove.add(key);
                }
            }
            for (String rmt : toRemove) {
                timeouts.remove(rmt);
                try {
                    bindings.remove(rmt).close();
                } catch (IOException ex) {
                    logger.warn("Exception during timeout close!", ex);
                }
            }
        }
    }

    /**
     * Subscribe to a RemoteProcedure from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @param timeoutAfter How long should calls wait before they are canceled
     * due to timeout.
     * @return the RemoteProcedure.
     */
    public RemoteProcedure subscribe(final String path, final int timeoutAfter) {
        return new SubscribedProcedure(path, timeoutAfter);
    }

    private void putNewInvokeBinding(String path, String localname, long timeoutAfter, OutputStream out, byte[] toSend) {
        synchronized (this) {
            timeouts.put(localname, Time.currentTimeMillis() + timeoutAfter);
            bindings.put(localname, out);
        }
        node.transmit(path, localRPCBinding + "/" + localname, toSend);
    }

    private class SubscribedProcedure implements RemoteProcedure, Serializable {

        private static final long serialVersionUID = 624324992717097477L;
        private final String path;
        private final int timeoutAfter;

        SubscribedProcedure(String path, int timeoutAfter) {
            this.path = path;
            this.timeoutAfter = timeoutAfter;
        }

        public void invoke(byte[] in, OutputStream out) {
            checkRPCTimeouts();
            String localname = UniqueIds.global.nextHexId(path);
            byte[] toSend = new byte[in.length + 1];
            toSend[0] = CluckNode.RMT_INVOKE;
            System.arraycopy(in, 0, toSend, 1, in.length);
            putNewInvokeBinding(path, localname, timeoutAfter, out, toSend);
        }
    }

    private Object writeReplace() {
        return new SerializedRPCManager(this.node);
    }

    private static class SerializedRPCManager implements Serializable {

        private static final long serialVersionUID = 8452028108928413549L;
        private final CluckNode node;

        public SerializedRPCManager(CluckNode node) {
            this.node = node;
        }

        private Object readResolve() {
            return node.getRPCManager();
        }
    }
}

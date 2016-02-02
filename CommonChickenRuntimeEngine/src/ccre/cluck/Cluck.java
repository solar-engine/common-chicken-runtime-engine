/*
 * Copyright 2013-2016 Colby Skeggs
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

import java.io.OutputStream;

import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventIO;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.tcp.CluckTCPClient;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.log.LoggingTarget;
import ccre.rconf.RConfable;

/**
 * A storage location for the current CluckNode, CluckTCPServer, and
 * CluckTCPClient.
 *
 * @author skeggsc
 */
public final class Cluck {

    /**
     * The current CluckNode.
     */
    private static final CluckNode node = new CluckNode();
    /**
     * The current CluckTCPClient.
     */
    private static CluckTCPClient client;

    /**
     * Get the current global CluckNode.
     *
     * @return The global CluckNode.
     */
    public static synchronized CluckNode getNode() {
        return node;
    }

    /**
     * Get the current global CluckTCPClient.
     *
     * @return The global CluckTCPClient.
     */
    public static synchronized CluckTCPClient getClient() {
        return client;
    }

    /**
     * Set up a server on the default port.
     *
     * @return the server that was set up.
     */
    public static synchronized CluckTCPServer setupServer() {
        CluckTCPServer server = new CluckTCPServer(node);
        server.start();
        return server;
    }

    /**
     * Set up a server on the specified port.
     *
     * @param port the port number to listen on
     * @return the server that was set up.
     */
    public static synchronized CluckTCPServer setupServer(int port) {
        CluckTCPServer server = new CluckTCPServer(node, port);
        server.start();
        return server;
    }

    /**
     * Set up a client pointing at the specified remote address, with the
     * specified name for this link and hint for what the remote end should call
     * this link.
     *
     * @param remote The remote address.
     * @param linkName The local link name.
     * @param hintedRemoteName The hint for what the remote server should call
     * this.
     */
    public static synchronized void setupClient(String remote, String linkName, String hintedRemoteName) {
        if (client != null) {
            throw new IllegalStateException("Client already set up!");
        }
        client = new CluckTCPClient(remote, node, linkName, hintedRemoteName);
        client.start();
    }

    /**
     * Publish an EventOutput on the network.
     *
     * @param name The name for the EventOutput.
     * @param consumer The EventOutput.
     */
    public static void publish(String name, final EventOutput consumer) {
        CluckPublisher.publish(node, name, consumer);
    }

    /**
     * Subscribe to an EventOutput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the EventOutput.
     */
    public static EventOutput subscribeEO(final String path) {
        return CluckPublisher.subscribeEO(node, path);
    }

    /**
     * Publish an EventInput on the network.
     *
     * @param name The name for the EventInput.
     * @param source The EventInput.
     */
    public static void publish(final String name, EventInput source) {
        CluckPublisher.publish(node, name, source);
    }

    /**
     * Subscribe to an EventInput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the EventInput.
     */
    public static EventInput subscribeEI(final String path) {
        return CluckPublisher.subscribeEI(node, path);
    }

    /**
     * Publish a LoggingTarget on the network.
     *
     * @param name The name for the LoggingTarget.
     * @param lt The LoggingTarget.
     */
    public static void publish(String name, final LoggingTarget lt) {
        CluckPublisher.publish(node, name, lt);
    }

    /**
     * Subscribe to a LoggingTarget from the network at the specified path, with
     * only sending data for at least a minimum logging level.
     *
     * @param path The path to subscribe to.
     * @return the LoggingTarget.
     */
    public static LoggingTarget subscribeLT(final String path) {
        return CluckPublisher.subscribeLT(node, path);
    }

    /**
     * Publish a BooleanInput on the network. This will send values to clients
     * when they connect.
     *
     * @param name The name for the BooleanInput.
     * @param input The BooleanInput.
     */
    public static void publish(final String name, final BooleanInput input) {
        CluckPublisher.publish(node, name, input);
    }

    /**
     * Subscribe to a BooleanInput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @param shouldSubscribeByDefault Should this request the value from the
     * remote by default, as opposed to waiting until this is needed. If this is
     * false, then readValue() won't work until you run addTarget().
     * @return the BooleanInput.
     */
    public static BooleanInput subscribeBI(final String path, boolean shouldSubscribeByDefault) {
        return CluckPublisher.subscribeBI(node, path, shouldSubscribeByDefault);
    }

    /**
     * Publish a BooleanOutput on the network.
     *
     * @param name The name for the BooleanOutput.
     * @param output The BooleanOutput.
     */
    public static void publish(String name, final BooleanOutput output) {
        CluckPublisher.publish(node, name, output);
    }

    /**
     * Subscribe to a BooleanOutput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the BooleanOutput.
     */
    public static BooleanOutput subscribeBO(final String path) {
        return CluckPublisher.subscribeBO(node, path);
    }

    /**
     * Publish a FloatInput on the network. This will send values to clients
     * when they connect.
     *
     * @param name The name for the FloatInput.
     * @param input The FloatInput.
     */
    public static void publish(final String name, final FloatInput input) {
        CluckPublisher.publish(node, name, input);
    }

    /**
     * Subscribe to a FloatInput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @param subscribeByDefault Should this request the value from the remote
     * by default, as opposed to waiting until this is needed. If this is false,
     * then readValue() won't work until you run addTarget().
     * @return the FloatInput.
     */
    public static FloatInput subscribeFI(final String path, boolean subscribeByDefault) {
        return CluckPublisher.subscribeFI(node, path, subscribeByDefault);
    }

    /**
     * Publish a FloatOutput on the network.
     *
     * @param name The name for the FloatOutput.
     * @param out The FloatOutput.
     */
    public static void publish(String name, FloatOutput out) {
        CluckPublisher.publish(node, name, out);
    }

    /**
     * Publish an RConfable device on the network.
     *
     * @param name The name for the RConfable.
     * @param device The RConfable.
     */
    public static void publishRConf(String name, RConfable device) {
        CluckPublisher.publishRConf(node, name, device);
    }

    /**
     * Subscribe to a FloatOutput from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the FloatOutput.
     */
    public static FloatOutput subscribeFO(final String path) {
        return CluckPublisher.subscribeFO(node, path);
    }

    /**
     * Publish a FloatStatus on the network.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param name The name for the FloatStatus.
     * @param stat The FloatStatus.
     */
    public static void publish(String name, FloatIO stat) {
        CluckPublisher.publish(node, name, stat);
    }

    /**
     * Publish a BooleanStatus on the network.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param name The name for the BooleanStatus.
     * @param stat The BooleanStatus to publish.
     */
    public static void publish(String name, BooleanIO stat) {
        CluckPublisher.publish(node, name, stat);
    }

    /**
     * Publish an EventStatus on the network.
     *
     * No corresponding subscribe is provided yet.
     *
     * @param name The name for the EventStatus.
     * @param stat The EventStatus to publish.
     */
    public static void publish(String name, EventIO stat) {
        CluckPublisher.publish(node, name, stat);
    }

    /**
     * Publish an OutputStream on the network.
     *
     * @param name The name for the OutputStream.
     * @param out The OutputStream.
     */
    public static void publish(String name, final OutputStream out) {
        CluckPublisher.publish(node, name, out);
    }

    /**
     * Subscribe to an OutputStream from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @return the OutputStream.
     */
    public static OutputStream subscribeOS(final String path) {
        return CluckPublisher.subscribeOS(node, path);
    }

    /**
     * Subscribe to an RConfable device from the network at the specified path.
     *
     * @param path The path to subscribe to.
     * @param timeout The maximum wait time for the RPC calls.
     * @return the RConfable.
     */
    public static RConfable subscribeRConf(String path, int timeout) {
        return CluckPublisher.subscribeRConf(node, path, timeout);
    }

    private Cluck() {
    }
}

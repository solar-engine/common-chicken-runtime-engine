/*
 * Copyright 2014-2015 Colby Skeggs.
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
package ccre.supercanvas.components.palette;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.cluck.CluckRemoteListener;
import ccre.cluck.rpc.RemoteProcedure;
import ccre.log.LogLevel;
import ccre.log.LoggingTarget;
import ccre.rconf.RConfable;
import ccre.supercanvas.SuperCanvasComponent;
import ccre.supercanvas.components.channels.BooleanControlComponent;
import ccre.supercanvas.components.channels.BooleanDisplayComponent;
import ccre.supercanvas.components.channels.EventControlComponent;
import ccre.supercanvas.components.channels.EventDisplayComponent;
import ccre.supercanvas.components.channels.FloatControlComponent;
import ccre.supercanvas.components.channels.FloatDisplayComponent;
import ccre.supercanvas.components.channels.LoggingTargetControlComponent;
import ccre.supercanvas.components.channels.OutputStreamControlComponent;
import ccre.supercanvas.components.channels.RConfComponent;
import ccre.supercanvas.components.channels.RPCControlComponent;
import ccre.timers.PauseTimer;
import ccre.util.UniqueIds;

/**
 * A palette that contains all the visible objects on the network.
 *
 * @author skeggsc
 */
public class NetworkPaletteComponent extends PaletteComponent<Collection<NetworkPaletteElement>> {
    private static Logger logger = LoggerFactory.getLogger(NetworkPaletteComponent.class);

    /**
     * Fake RMT number used for merged event channels.
     */
    public static final int F_RMT_EVENTS = -1;
    /**
     * Fake RMT number used for merged float channels.
     */
    public static final int F_RMT_FLOATS = -2;
    /**
     * Fake RMT number used for merged boolean channels.
     */
    public static final int F_RMT_BOOLEANS = -3;
    /**
     * Fake RMT number used for detected rconf endpoints.
     */
    public static final int F_RMT_RCONF = -4;
    private static final long serialVersionUID = -2162354007005983283L;

    static SuperCanvasComponent createComponent(String name, Object target, int type, int x, int y) {
        switch (type) {
        case CluckNode.RMT_BOOLOUTP:
            return new BooleanControlComponent(x, y, name, (BooleanOutput) target);
        case CluckNode.RMT_BOOLINPUT:
            return new BooleanDisplayComponent(x, y, name, (BooleanInput) target);
        case CluckNode.RMT_EVENTOUTP:
            return new EventControlComponent(x, y, name, (EventOutput) target);
        case CluckNode.RMT_EVENTINPUT:
            return new EventDisplayComponent(x, y, name, (EventInput) target);
        case CluckNode.RMT_FLOATOUTP:
            return new FloatControlComponent(x, y, name, (FloatOutput) target);
        case CluckNode.RMT_FLOATINPUT:
            return new FloatDisplayComponent(x, y, name, (FloatInput) target);
        case F_RMT_EVENTS:
            return new EventControlComponent(x, y, name, (EventInput) ((Object[]) target)[0], (EventOutput) ((Object[]) target)[1]);
        case F_RMT_FLOATS:
            return new FloatControlComponent(x, y, name, (FloatInput) ((Object[]) target)[0], (FloatOutput) ((Object[]) target)[1]);
        case F_RMT_BOOLEANS:
            return new BooleanControlComponent(x, y, name, (BooleanInput) ((Object[]) target)[0], (BooleanOutput) ((Object[]) target)[1]);
        case F_RMT_RCONF:
            return new RConfComponent(x, y, name, (RConfable) target);
        case CluckNode.RMT_OUTSTREAM:
            return new OutputStreamControlComponent(x, y, name, (OutputStream) target);
        case CluckNode.RMT_LOGTARGET:
            return new LoggingTargetControlComponent(x, y, name, (LoggingTarget) target);
        case CluckNode.RMT_INVOKE:
            return new RPCControlComponent(x, y, name, (RemoteProcedure) target);
        default:
            LoggerFactory.getLogger(NetworkPaletteComponent.class).warn("Could not display RMT of " + CluckNode.rmtToString(type));
            return null;
        }
    }

    private static Object subscribeByType(String path, int type) {
        switch (type) {
        case CluckNode.RMT_BOOLOUTP:
            return Cluck.subscribeBO(path);
        case CluckNode.RMT_BOOLINPUT:
            return Cluck.subscribeBI(path, false);
        case CluckNode.RMT_EVENTOUTP:
            return Cluck.subscribeEO(path);
        case CluckNode.RMT_EVENTINPUT:
            return Cluck.subscribeEI(path);
        case CluckNode.RMT_FLOATOUTP:
            return Cluck.subscribeFO(path);
        case CluckNode.RMT_FLOATINPUT:
            return Cluck.subscribeFI(path, false);
        case CluckNode.RMT_INVOKE:
            return Cluck.getNode().getRPCManager().subscribe(path, 500); // Is this a good amount of time?
        case CluckNode.RMT_LOGTARGET:
            return Cluck.subscribeLT(path, LogLevel.TRACE); // Is this a good level?
        case CluckNode.RMT_OUTSTREAM:
            return Cluck.subscribeOS(path);
        default:
            LoggerFactory.getLogger(NetworkPaletteComponent.class).warn("Could not subscribe to RMT of " + CluckNode.rmtToString(type));
            return null;
        }
    }

    private transient PauseTimer researcher;

    /**
     * Create a new NetworkPaletteComponent at the specified position.
     *
     * @param cx The X coordinate.
     * @param cy The Y coordinate.
     */
    public NetworkPaletteComponent(int cx, int cy) {
        super(cx, cy, Collections.synchronizedSet(new TreeSet<NetworkPaletteElement>()));
        start();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        start();
    }

    @Override
    protected boolean onInteractWithTitleBar() {
        researcher.event();
        return true;
    }

    private void start() {
        this.researcher = new PauseTimer(500);
        final EventOutput searcher = CluckPublisher.setupSearching(Cluck.getNode(), new CluckRemoteListener() {
            @Override
            public synchronized void handle(String remote, int remoteType) {
                for (NetworkPaletteElement e : entries) {
                    if (e.getName().equals(remote)) {
                        if (e.getType() != remoteType) {
                            logger.warn("Mismatched remote type in search!");
                        }
                        return;
                    }
                }
                Object sub = subscribeByType(remote, remoteType);
                if (sub != null) {
                    entries.add(new NetworkPaletteElement(remote, sub, remoteType));
                }
                String pairName, base;
                int expect, type;
                boolean isExpectInput;
                if (remote.endsWith(".input")) {
                    isExpectInput = false;
                    base = remote.substring(0, remote.length() - 6);
                    pairName = base + ".output";
                    switch (remoteType) {
                    case CluckNode.RMT_BOOLINPUT:
                        expect = CluckNode.RMT_BOOLOUTP;
                        type = F_RMT_BOOLEANS;
                        break;
                    case CluckNode.RMT_FLOATINPUT:
                        expect = CluckNode.RMT_FLOATOUTP;
                        type = F_RMT_FLOATS;
                        break;
                    case CluckNode.RMT_EVENTINPUT:
                        expect = CluckNode.RMT_EVENTOUTP;
                        type = F_RMT_EVENTS;
                        break;
                    default:
                        return;
                    }
                } else if (remote.endsWith(".output")) {
                    isExpectInput = true;
                    base = remote.substring(0, remote.length() - 7);
                    pairName = base + ".input";
                    switch (remoteType) {
                    case CluckNode.RMT_BOOLOUTP:
                        expect = CluckNode.RMT_BOOLINPUT;
                        type = F_RMT_BOOLEANS;
                        break;
                    case CluckNode.RMT_FLOATOUTP:
                        expect = CluckNode.RMT_FLOATINPUT;
                        type = F_RMT_FLOATS;
                        break;
                    case CluckNode.RMT_EVENTOUTP:
                        expect = CluckNode.RMT_EVENTINPUT;
                        type = F_RMT_EVENTS;
                        break;
                    default:
                        return;
                    }
                } else {
                    if ((remote.endsWith("-rpcq") || remote.endsWith("-rpcs")) && remoteType == CluckNode.RMT_INVOKE) {
                        base = remote.substring(0, remote.length() - 5);
                        pairName = base + (remote.endsWith("s") ? "-rpcq" : "-rpcs");
                        for (NetworkPaletteElement e : entries) {
                            if (pairName.equals(e.getName()) && e.type == CluckNode.RMT_INVOKE) {
                                entries.add(new NetworkPaletteElement(base, Cluck.subscribeRConf(base, 500), F_RMT_RCONF));
                                break;
                            }
                        }
                    }
                    return;
                }
                Object pair = null;
                for (NetworkPaletteElement e : entries) {
                    if (pairName.equals(e.getName()) && e.type == expect) {
                        pair = e.target;
                    }
                }
                if (pair != null) {
                    Object[] send = isExpectInput ? new Object[] { pair, sub } : new Object[] { sub, pair };
                    entries.add(new NetworkPaletteElement(base, send, type));
                }
            }
        });
        researcher.triggerAtEnd(new EventOutput() {
            @Override
            public void event() {
                entries.clear();
                searcher.event();
            }
        });
        Cluck.getNode().subscribeToStructureNotifications(UniqueIds.global.nextHexId("notification-subscriber"), researcher);
        searcher.event();
    }

    /**
     * Try to find the named Cluck reference and create a new component at the
     * given position, if found.
     *
     * @param x the X-position of the new component.
     * @param y the Y-position of the new component.
     * @param ref the Cluck reference to search for.
     * @return the new SuperCanvasComponent, or null if the reference cannot be
     * found.
     */
    public SuperCanvasComponent getComponentFor(int x, int y, String ref) {
        for (NetworkPaletteElement elem : entries) {
            if (ref.equals(elem.getName())) {
                return elem.fetch(x, y);
            }
        }
        return null;
    }
}

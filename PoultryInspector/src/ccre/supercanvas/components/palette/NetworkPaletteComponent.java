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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckConstants;
import ccre.cluck.CluckPublisher;
import ccre.cluck.CluckRemoteListener;
import ccre.cluck.rpc.RemoteProcedure;
import ccre.log.Logger;
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
        case CluckConstants.RMT_BOOLOUTP:
            return new BooleanControlComponent(x, y, name, (BooleanOutput) target);
        case CluckConstants.RMT_BOOLINPUT:
            return new BooleanDisplayComponent(x, y, name, (BooleanInput) target);
        case CluckConstants.RMT_EVENTOUTP:
            return new EventControlComponent(x, y, name, (EventOutput) target);
        case CluckConstants.RMT_EVENTINPUT:
            return new EventDisplayComponent(x, y, name, (EventInput) target);
        case CluckConstants.RMT_FLOATOUTP:
            return new FloatControlComponent(x, y, name, (FloatOutput) target);
        case CluckConstants.RMT_FLOATINPUT:
            return new FloatDisplayComponent(x, y, name, (FloatInput) target);
        case F_RMT_EVENTS:
            return new EventControlComponent(x, y, name, (EventInput) ((Object[]) target)[0], (EventOutput) ((Object[]) target)[1]);
        case F_RMT_FLOATS:
            return new FloatControlComponent(x, y, name, (FloatInput) ((Object[]) target)[0], (FloatOutput) ((Object[]) target)[1]);
        case F_RMT_BOOLEANS:
            return new BooleanControlComponent(x, y, name, (BooleanInput) ((Object[]) target)[0], (BooleanOutput) ((Object[]) target)[1]);
        case F_RMT_RCONF:
            return new RConfComponent(x, y, name, (RConfable) target);
        case CluckConstants.RMT_OUTSTREAM:
            return new OutputStreamControlComponent(x, y, name, (OutputStream) target);
        case CluckConstants.RMT_LOGTARGET:
            return new LoggingTargetControlComponent(x, y, name, (LoggingTarget) target);
        case CluckConstants.RMT_INVOKE:
            return new RPCControlComponent(x, y, name, (RemoteProcedure) target);
        default:
            Logger.warning("Could not display RMT of " + CluckConstants.rmtToString(type));
            return null;
        }
    }

    private static Object subscribeByType(String path, int type) {
        switch (type) {
        case CluckConstants.RMT_BOOLOUTP:
            return Cluck.subscribeBO(path);
        case CluckConstants.RMT_BOOLINPUT:
            return Cluck.subscribeBI(path, false);
        case CluckConstants.RMT_EVENTOUTP:
            return Cluck.subscribeEO(path);
        case CluckConstants.RMT_EVENTINPUT:
            return Cluck.subscribeEI(path);
        case CluckConstants.RMT_FLOATOUTP:
            return Cluck.subscribeFO(path);
        case CluckConstants.RMT_FLOATINPUT:
            return Cluck.subscribeFI(path, false);
        case CluckConstants.RMT_INVOKE:
            return Cluck.getNode().getRPCManager().subscribe(path, 500); // Is this a good amount of time?
        case CluckConstants.RMT_LOGTARGET:
            return Cluck.subscribeLT(path);
        case CluckConstants.RMT_OUTSTREAM:
            return Cluck.subscribeOS(path);
        default:
            Logger.warning("Could not subscribe to RMT of " + CluckConstants.rmtToString(type));
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
                            Logger.warning("Mismatched remote type in search!");
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
                    case CluckConstants.RMT_BOOLINPUT:
                        expect = CluckConstants.RMT_BOOLOUTP;
                        type = F_RMT_BOOLEANS;
                        break;
                    case CluckConstants.RMT_FLOATINPUT:
                        expect = CluckConstants.RMT_FLOATOUTP;
                        type = F_RMT_FLOATS;
                        break;
                    case CluckConstants.RMT_EVENTINPUT:
                        expect = CluckConstants.RMT_EVENTOUTP;
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
                    case CluckConstants.RMT_BOOLOUTP:
                        expect = CluckConstants.RMT_BOOLINPUT;
                        type = F_RMT_BOOLEANS;
                        break;
                    case CluckConstants.RMT_FLOATOUTP:
                        expect = CluckConstants.RMT_FLOATINPUT;
                        type = F_RMT_FLOATS;
                        break;
                    case CluckConstants.RMT_EVENTOUTP:
                        expect = CluckConstants.RMT_EVENTINPUT;
                        type = F_RMT_EVENTS;
                        break;
                    default:
                        return;
                    }
                } else {
                    if ((remote.endsWith("-rpcq") || remote.endsWith("-rpcs")) && remoteType == CluckConstants.RMT_INVOKE) {
                        base = remote.substring(0, remote.length() - 5);
                        pairName = base + (remote.endsWith("s") ? "-rpcq" : "-rpcs");
                        for (NetworkPaletteElement e : entries) {
                            if (pairName.equals(e.getName()) && e.type == CluckConstants.RMT_INVOKE) {
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
        researcher.triggerAtEnd(() -> {
            entries.clear();
            searcher.event();
        });
        Cluck.getNode().subscribeToStructureNotifications(UniqueIds.global.nextHexId("notification-subscriber"), researcher);
        searcher.safeEvent();
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

/*
 * Copyright 2014 Colby Skeggs.
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
package supercanvas;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckRemoteListener;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.ctrl.PauseTimer;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.UniqueIds;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * A palette that contains all the visible objects on the network.
 *
 * @author skeggsc
 */
public class NetworkPaletteComponent extends PaletteComponent<Collection<NetworkPaletteElement>> {

    static SuperCanvasComponent createComponent(String name, Object target, int type, int x, int y) {
        switch (type) {
            case CluckNode.RMT_BOOLOUTP:
                return new BooleanControlComponent(x, y, name, (BooleanOutput) target);
            case CluckNode.RMT_BOOLPROD:
                return new BooleanDisplayComponent(x, y, name, (BooleanInput) target);
            case CluckNode.RMT_EVENTOUTP:
                return new EventControlComponent(x, y, name, (EventOutput) target);
            case CluckNode.RMT_EVENTINPUT:
                return new EventDisplayComponent(x, y, name, (EventInput) target);
            case CluckNode.RMT_FLOATOUTP:
                return new FloatControlComponent(x, y, name, (FloatOutput) target);
            case CluckNode.RMT_FLOATPROD:
                return new FloatDisplayComponent(x, y, name, (FloatInput) target);
            case CluckNode.RMT_INVOKE: // TODO: These three.
            case CluckNode.RMT_LOGTARGET:
            case CluckNode.RMT_OUTSTREAM:
            default:
                Logger.warning("Could not display RMT of " + CluckNode.rmtToString(type));
                return null;
        }
    }

    private static Object subscribeByType(String path, int type) {
        switch (type) {
            case CluckNode.RMT_BOOLOUTP:
                return Cluck.subscribeBO(path);
            case CluckNode.RMT_BOOLPROD:
                return Cluck.subscribeBI(path, false);
            case CluckNode.RMT_EVENTOUTP:
                return Cluck.subscribeEO(path);
            case CluckNode.RMT_EVENTINPUT:
                return Cluck.subscribeEI(path);
            case CluckNode.RMT_FLOATOUTP:
                return Cluck.subscribeFO(path);
            case CluckNode.RMT_FLOATPROD:
                return Cluck.subscribeFI(path, false);
            case CluckNode.RMT_INVOKE:
                return Cluck.getNode().getRPCManager().subscribe(path, 500); // TODO: Is this a good amount of time?
            case CluckNode.RMT_LOGTARGET:
                return Cluck.subscribeLT(path, LogLevel.FINEST); // TODO: Is this a good level?
            case CluckNode.RMT_OUTSTREAM:
                return Cluck.subscribeOS(path);
            default:
                Logger.warning("Could not subscribe to RMT of " + CluckNode.rmtToString(type));
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

    private void start() {
        final String searchLinkName = UniqueIds.global.nextHexId("researcher");
        this.researcher = new PauseTimer(500);
        researcher.triggerAtEnd(new CollapsingWorkerThread("Cluck-Researcher") {
            @Override
            protected void doWork() throws Throwable {
                entries.clear();
                Cluck.getNode().cycleSearchRemotes(searchLinkName);
            }
        });
        Cluck.getNode().subscribeToStructureNotifications(UniqueIds.global.nextHexId("notification-subscriber"), researcher);
        Cluck.getNode().startSearchRemotes(searchLinkName, new CluckRemoteListener() {
            @Override
            public synchronized void handle(String remote, int remoteType) {
                for (NetworkPaletteElement e : entries) {
                    if (e.getName().equals(remote)) {
                        if (e.getType() != remoteType) {
                            Logger.warning("Mismatched remote type in search@");
                        }
                        return;
                    }
                }
                Object sub = subscribeByType(remote, remoteType);
                if (sub != null) {
                    entries.add(new NetworkPaletteElement(remote, sub, remoteType));
                }
            }
        });
    }
}

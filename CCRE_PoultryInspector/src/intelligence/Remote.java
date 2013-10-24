/*
 * Copyright 2013 Colby Skeggs
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
package intelligence;

import ccre.cluck.CluckNode;
import static ccre.cluck.CluckNode.RMT_BOOLOUTP;
import static ccre.cluck.CluckNode.RMT_BOOLPROD;
import static ccre.cluck.CluckNode.RMT_EVENTCONSUMER;
import static ccre.cluck.CluckNode.RMT_EVENTSOURCE;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD;
import static ccre.cluck.CluckNode.RMT_LOGTARGET;
import static ccre.cluck.CluckNode.RMT_OUTSTREAM;
import ccre.log.LogLevel;
import ccre.log.Logger;
import java.awt.Color;

public class Remote implements Comparable<Remote> {
    protected final int type;
    protected final String remote;
    protected final CluckNode node;
    protected Object checkout;

    protected Remote(String remote, int remoteType, CluckNode node) {
        this.remote = remote;
        this.type = remoteType;
        this.node = node;
    }

    @Override
    public int compareTo(Remote o) {
        return remote.compareTo(o.remote);
    }

    @Override
    public String toString() {
        return remote + " : " + CluckNode.rmtToString(type);
    }

    public Color getColor() {
        switch (type) {
            case RMT_EVENTCONSUMER:
            case RMT_EVENTSOURCE:
                return Color.MAGENTA;
            case RMT_LOGTARGET:
                return Color.YELLOW;
            case RMT_BOOLPROD:
            case RMT_BOOLOUTP:
                return Color.RED;
            case RMT_FLOATPROD:
            case RMT_FLOATOUTP:
                return Color.ORANGE;
            case RMT_OUTSTREAM:
                return Color.BLUE;
            default:
                return Color.BLACK;
        }
    }
    
    protected void checkout() {
        switch (type) {
            case RMT_EVENTCONSUMER:
                checkout = node.subscribeEC(remote);
                break;
            case RMT_EVENTSOURCE:
                checkout = node.subscribeES(remote);
                break;
            case RMT_LOGTARGET:
                checkout = node.subscribeLT(remote, LogLevel.FINEST);
                break;
            case RMT_BOOLPROD:
                checkout = node.subscribeBIP(remote);
                break;
            case RMT_BOOLOUTP:
                checkout = node.subscribeBO(remote);
                break;
            case RMT_FLOATPROD:
                checkout = node.subscribeFIP(remote);
                break;
            case RMT_FLOATOUTP:
                checkout = node.subscribeFO(remote);
                break;
            case RMT_OUTSTREAM:
                checkout = node.subscribeOS(remote);
                break;
            default:
                Logger.severe("No checkout for type: " + CluckNode.rmtToString(type));
        }
    }

}

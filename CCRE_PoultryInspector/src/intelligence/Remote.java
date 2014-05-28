/*
 * Copyright 2013-2014 Colby Skeggs, Gregor Peach (Folder compatability)
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

import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import static ccre.cluck.CluckNode.RMT_BOOLOUTP;
import static ccre.cluck.CluckNode.RMT_BOOLPROD;
import static ccre.cluck.CluckNode.RMT_EVENTINPUT;
import static ccre.cluck.CluckNode.RMT_EVENTOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD;
import static ccre.cluck.CluckNode.RMT_INVOKE;
import static ccre.cluck.CluckNode.RMT_LOGTARGET;
import static ccre.cluck.CluckNode.RMT_OUTSTREAM;
import ccre.log.LogLevel;
import ccre.log.Logger;
import java.awt.Color;

/**
 * A representation for a Remote object.
 *
 * @author skeggsc
 */
public class Remote implements Comparable<Remote> {

    /**
     * The RMT of the Remote.
     */
    protected final int type;
    /**
     * Is this remote in a folder?
     */
    protected boolean inFolder = false;
    /**
     * The remote path.
     */
    protected final String path;
    /**
     * The subscribed version of the object.
     */
    protected Object checkout;
    /**
     * The paired remote, if this is one half of a FloatStatus or BooleanStatus.
     */
    protected Remote paired;

    /**
     * Create a new remote with a specified remote address, Cluck node, and
     * remote type.
     *
     * @param remote The path.
     * @param remoteType The RMT type.
     */
    protected Remote(String remote, int remoteType) {
        this.path = remote;
        this.type = remoteType;
    }

    @Override
    public int compareTo(Remote o) {
        return path.compareTo(o.path);
    }

    @Override
    public String toString() {
        return (inFolder ? "  " : "") + path + " : " + CluckNode.rmtToString(type);
    }

    /**
     * Subscribe this remote and stick it in the checkout field.
     *
     * @return The current checked-out object.
     */
    protected Object checkout() {
        if (checkout != null) {
            return checkout;
        }
        switch (type) {
            case RMT_EVENTOUTP:
                checkout = Cluck.subscribeEO(path);
                break;
            case RMT_EVENTINPUT:
                checkout = Cluck.subscribeEI(path);
                break;
            case RMT_LOGTARGET:
                checkout = Cluck.subscribeLT(path, LogLevel.FINEST);
                break;
            case RMT_BOOLPROD:
                checkout = Cluck.subscribeBI(path, false);
                break;
            case RMT_BOOLOUTP:
                checkout = Cluck.subscribeBO(path);
                break;
            case RMT_FLOATPROD:
                checkout = Cluck.subscribeFI(path, false);
                break;
            case RMT_FLOATOUTP:
                checkout = Cluck.subscribeFO(path);
                break;
            case RMT_OUTSTREAM:
                checkout = Cluck.subscribeOS(path);
                break;
            case RMT_INVOKE:
                checkout = Cluck.getNode().getRPCManager().subscribe(path, 1000);
                break;
            default:
                Logger.severe("No checkout for type: " + CluckNode.rmtToString(type));
        }
        return checkout;
    }
}

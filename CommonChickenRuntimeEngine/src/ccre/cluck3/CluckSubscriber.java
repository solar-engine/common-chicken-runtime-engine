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
package ccre.cluck3;

import ccre.log.Logger;

public abstract class CluckSubscriber implements CluckLink {

    private CluckNode node;
    private String linkName;

    public final boolean transmit(String dest, String source, byte[] data) {
        if (dest == null) {
            receive(source, data);
        } else if ("*".equals(dest)) {
            receiveBroadcast(source, data);
        } else {
            handleOther(dest, source, data);
        }
        return true;
    }

    public void attach(CluckNode node, String name) {
        if (node == null || name == null) {
            throw new NullPointerException();
        }
        if (this.node != null) {
            throw new IllegalStateException("Node already attached!");
        }
        this.node = node;
        this.linkName = name;
        node.addLink(this, name);
    }

    protected void handleOther(String dest, String source, byte[] data) {
        // Do nothing by default
    }

    protected boolean requireRMT(String source, byte[] data, byte rmt) {
        if (data.length == 0) {
            Logger.warning("Received null message from " + source);
            return false;
        }
        if (data[0] == CluckNode.RMT_PING && data.length == 1) {
            node.transmit(source, linkName, new byte[]{CluckNode.RMT_PING, rmt});
            return false;
        }
        if (data[0] != rmt) {
            Logger.warning("Received wrong RMT: " + data[0] + " from " + source + " (expected " + rmt + ")");
            return false;
        }
        return true;
    }

    protected void defaultBroadcastHandle(String source, byte[] data, byte rmt) {
        node.transmit(source, linkName, new byte[]{rmt});
    }

    protected abstract void receive(String source, byte[] data);

    protected abstract void receiveBroadcast(String source, byte[] data);
}

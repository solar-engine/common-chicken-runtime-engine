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
package ccre.cluck;

public class CluckNullLink implements CluckLink {

    protected CluckNullLink paired;
    protected final CluckNode node;
    public String linkName;

    public CluckNullLink(CluckNode node) {
        this.node = node;
        // Will expect other null link to be created.
    }

    public CluckNullLink(CluckNode node, CluckNullLink other) {
        paired = other;
        if (other.paired != null) {
            throw new IllegalStateException("Other link is already attached!");
        }
        this.node = node;
        other.paired = this;
    }
    public CluckNullLink(CluckNode node, String linkName) {
        this.node = node;
        this.linkName = linkName;
        node.addLink(this, linkName);
        // Will expect other null link to be created.
    }

    public CluckNullLink(CluckNode node, String linkName, CluckNullLink other) {
        paired = other;
        if (other.paired != null) {
            throw new IllegalStateException("Other link is already attached!");
        }
        this.node = node;
        this.linkName = linkName;
        other.paired = this;
        node.addLink(this, linkName);
    }

    public boolean transmit(String rest, String source, byte[] data) {
        if (paired == null) {
            throw new IllegalStateException("Must have paired Null Link!");
        }
        paired.pairTransmit(rest, source, data);
        return true;
    }

    private void pairTransmit(String rest, String source, byte[] data) {
        // Prepend link name
        if (linkName == null) {
            linkName = node.getLinkName(this);
        }
        if (source == null) {
            source = linkName;
        } else {
            source = linkName + "." + source;
        }
        node.transmit(rest, source, data, this);
    }
}

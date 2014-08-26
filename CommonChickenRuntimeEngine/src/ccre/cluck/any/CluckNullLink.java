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
package ccre.cluck.any;

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;

/**
 * A CluckNullLink is a link between two CluckNodes on the same computer, and an
 * example of how to write a link that connects two CluckNodes.
 *
 * Usage example:<br>
 * <code>
 * CluckNode alpha = new CluckNode();<br>
 * CluckNode beta = new CluckNode();<br>
 * CluckNullLink alphaLink = new CluckNullLink(alpha).name("alpha-to-beta");<br>
 * CluckNullLink betaLink = new CluckNullLink(beta).name("beta-to-alpha");<br>
 * betaLink.attach(alphaLink);
 * <br>
 * EventOutput test = new EventLogger(LogLevel.INFO, "Pseudo-networked
 * test!");<br>
 * alpha.publish("test", test);<br>
 * EventOutput test2 = beta.subscribeEC("beta-to-alpha/test");<br>
 * test2.eventFired();<br>
 * </code><br>
 * This will log "Pseudo-networked test!" at LogLevel INFO.
 *
 * Alternatively, the third through fifth lines of that example can be replaced
 * with: <code>
 * CluckNullLink.connect(alpha, "alpha-to-beta", beta, "beta-to-alpha");
 * </code> And it will work the same.
 *
 * @author skeggsc
 */
public final class CluckNullLink implements CluckLink {

    /**
     * Connect the two specified CluckNodes with a Null Link.
     *
     * @param alpha The Alpha node.
     * @param alphaToBeta The link name for connecting from alpha to beta.
     * @param beta The Beta node.
     * @param betaToAlpha The link name for connecting from beta to alpha.
     */
    public static void connect(CluckNode alpha, String alphaToBeta, CluckNode beta, String betaToAlpha) {
        new CluckNullLink(beta).name(betaToAlpha).attach(new CluckNullLink(alpha).name(alphaToBeta));
    }

    /**
     * The other end of this CluckNullLink.
     */
    private CluckNullLink paired;
    /**
     * The CluckNode attached to this end of the link.
     */
    private final CluckNode node;
    /**
     * The link name of this link.
     */
    private String linkName;

    /**
     * Create a new link attached to the specified CluckNode.
     *
     * @param node The node to attach to.
     */
    public CluckNullLink(CluckNode node) {
        this.node = node;
    }

    /**
     * Add this link to the attached CluckNode under the specified name.
     *
     * @param linkName The link name to use.
     * @return This link, for method chaining.
     */
    public CluckNullLink name(String linkName) {
        this.linkName = linkName;
        node.addLink(this, linkName);
        return this;
    }

    /**
     * Attach this null link with the other null link. Only do this once per
     * pair!
     *
     * @param pairWith The other null link.
     * @return This link, for method chaining.
     */
    public CluckNullLink attach(CluckNullLink pairWith) {
        if (paired != null) {
            throw new IllegalStateException("Link is already attached!");
        }
        paired = pairWith.internalPair(this);
        return this;
    }

    private CluckNullLink internalPair(CluckNullLink other) {
        if (paired != null) {
            throw new IllegalStateException("Other link is already attached!");
        }
        paired = other;
        return this;
    }

    public boolean send(String rest, String source, byte[] data) {
        if (paired == null) {
            return true;
        }
        paired.pairTransmit(rest, source, data);
        return true;
    }

    private void pairTransmit(String rest, String source, byte[] data) {
        // Prepend link name
        if (linkName == null) {
            linkName = node.getLinkName(this);
        }
        String sourceToSend;
        if (source == null) {
            sourceToSend = linkName;
        } else {
            sourceToSend = linkName + "/" + source;
        }
        node.transmit(rest, sourceToSend, data, this);
    }
}

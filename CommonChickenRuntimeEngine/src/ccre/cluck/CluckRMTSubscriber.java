/*
 * Copyright 2015 Colby Skeggs
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

/**
 * A wrapper around CluckSubscriber to handle its common use scenario.
 * 
 * @author skeggsc
 */
public abstract class CluckRMTSubscriber extends CluckSubscriber {

    private final byte rmt;
    private final int minLen;

    /**
     * Create a new CluckRMTSubscriber attached to node with rmt as the RMT.
     * 
     * @param node the node to attach to.
     * @param rmt the RMT number to handle.
     */
    public CluckRMTSubscriber(CluckNode node, byte rmt) {
        super(node);
        this.rmt = rmt;
        this.minLen = 1;
    }

    /**
     * Create a new CluckRMTSubscriber attached to node with rmt as the RMT and a minimum length.
     * 
     * @param node the node to attach to.
     * @param rmt the RMT number to handle.
     * @param minLen the minimum length.
     */
    public CluckRMTSubscriber(CluckNode node, byte rmt, int minLen) {
        super(node);
        this.rmt = rmt;
        this.minLen = minLen;
    }

    @Override
    protected final void receive(String source, byte[] data) {
        if (requireRMT(source, data, rmt, minLen)) {
            receiveValid(source, data);
        } else {
            receiveInvalid(source, data);
        }
    }

    /**
     * Called when a message with a valid RMT is received.
     * 
     * @param source the source from which the message originated.
     * @param data the data contained in the message, including header.
     */
    protected abstract void receiveValid(String source, byte[] data);

    /**
     * Called when a message with an invalid RMT is received.
     * 
     * @param source the source from which the message originated.
     * @param data the data contained in the message, including header.
     */
    protected void receiveInvalid(String source, byte[] data) {
        // Do nothing by default.
    }

    @Override
    protected void receiveBroadcast(String source, byte[] data) {
        defaultBroadcastHandle(source, data, rmt);
    }
}

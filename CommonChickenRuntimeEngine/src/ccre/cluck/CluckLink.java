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

/**
 * A link over which Cluck messages can be sent. This might be a link to a
 * remote CluckNode, or an attached object that can have messages sent to it.
 *
 * @author skeggsc
 */
public interface CluckLink {

    /**
     * Send a Cluck message over this link.
     *
     * Dest and source are forward-slash-separated paths.
     *
     * The destination is relative to the node at the other end of the link -
     * the name of this link has already been stripped off.
     *
     * The source is relative to the previous node - it should get the name of
     * this link on the other end added to it so that messages can be sent in
     * response.
     *
     * @param dest The destination path.
     * @param source The source path.
     * @param data The data packet.
     * @return true if more messages should be delivered, false if this should
     * be detached from the CluckNode.
     */
    public boolean transmit(String dest, String source, byte[] data);
}

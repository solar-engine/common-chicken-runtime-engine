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
 * A listener for data sent over a Cluck channel. Can be registered with a
 * specific channel name or with all channels.
 *
 * @see CluckNode
 * @author skeggsc
 */
public interface CluckChannelListener {

    /**
     * Called when data has been received on a specific channel. Should not
     * modify data because it is the same array sent to all instances of this
     * function.
     *
     * @param channel The channel that the data was received on.
     * @param data The data that was received.
     */
    public void receive(String channel, byte[] data);
}

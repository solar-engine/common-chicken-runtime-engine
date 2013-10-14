/*
 * Copyright 2013 Colby Skeggs and Vincent Miller
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
package ccre.obsidian.comms;

/**
 * An interface for mesh-based networked communication between instances.
 * 
 * @author MillerV
 */
public interface Radio {
    /**
     * Open the USB port so that the radio can be used.
     * 
     * @throws RadioException 
     */
    public void open() throws RadioException;
        
    /**
     * Close the USB port so that it can be reused.
     */
    public void close();
    
    /**
     * Send a data packet synchronously over the network to the specified
     * address, and waits indefinitely for a response.
     * 
     * @param address The Address to send the packet to.
     * @param message The packet to send.
     * @return The Packet received.
     * @throws RadioException 
     */
    public Packet sendPacketSync(Address address, int[] message) throws RadioException;
    
    /**
     * Send a data packet synchronously over the network to the specified
     * address, and waits for a response until the timeout is reached.
     * 
     * @param address The Address to send the packet to.
     * @param message The packet to send.
     * @param timeout The timeout, in milliseconds.
     * @return The Packet received.
     * @throws RadioException
     * @throws RadioTimeoutException 
     */
    public Packet sendPacketSync(Address address, int[] message, int timeout) throws RadioException, RadioTimeoutException;
    
    /**
     * Send a data packet asynchronously over the network the the specified
     * address.
     * 
     * @param address The Address to send the packet to.
     * @param message The papcket to send.
     * @throws RadioException 
     */
    public void sendPacketAsync(Address address, int[] message) throws RadioException;
    
    /**
     * Wait for an incoming packet until the specified timeout is reached.
     * 
     * @param timeout The timeout, in milliseconds.
     * @return The Packet received.
     * @throws RadioTimeoutException 
     */
    public Packet recievePacket(int timeout) throws RadioTimeoutException;
    
    /**
     * Wait indefinitely for an incoming packet.
     * 
     * @return The Packet received.
     * @throws RadioException 
     */
    public Packet recievePacket() throws RadioException;
    
    /**
     * Add a PacketListener on this Radio instance, whose processResponse
     * method will be called whenever a packet is received.
     * 
     * @param listener The PacketListener to add.
     */
    public void addPacketListener(PacketListener listener);
    
    /**
     * A packet received by the Radio object.
     */
    public interface Packet {
        /**
         * Get the raw bytes of the packet, including escape characters.
         * 
         * @return An array of the bytes contained in the packet.
         */
        public int[] getRawPacketBytes();
        
        /**
         * Get the un-escaped bytes of the packet.
         * 
         * @return An array of the un-escaped bytes contained in the packet.
         */
        public int[] getProcessedPacketBytes();
        
        /**
         * @return Whether there was an error in parsing the bytes.
         */
        public boolean isError();
        
        /**
         * @return The Address at which the packet originated.
         */
        public Address getRemoteAddress64();
    }
    
    /**
     * An address of a single point on the network, used to target packets and
     * identify their origin.
     */
    public interface Address {
        /**
         * @return An int[] representation of this address.
         */
        public int[] getAddress();
    }
    
    /**
     * A listener for packets received by a point on the network.
     */
    public interface PacketListener {
        /**
         * Process a Packet received by the Radio, taking whatever action is
         * required by the user.
         * 
         * @param p The packet received by the Radio.
         */
        public void processResponse(Packet p);
    }
}
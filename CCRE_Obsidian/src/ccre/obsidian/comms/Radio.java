/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian.comms;

/**
 *
 * @author MillerV
 */
public interface Radio {
    public void open() throws RadioException;
        
    public void close();
    
    public Packet sendPacketSync(Address address, int[] message) throws RadioException;
    
    public Packet sendPacketSync(Address address, int[] message, int timeout) throws RadioException, RadioTimeoutException;
    
    public void sendPacketAsync(Address address, int[] message) throws RadioException;
    
    public Packet recievePacket(int timeout) throws RadioTimeoutException;
    
    public Packet recievePacket() throws RadioException;
    
    public void addPacketListener(PacketListener listener);
    
    public interface Packet {
        public int getChecksum();
        
        public int[] getRawPacketBytes();
        
        public int[] getProcessedPacketBytes();
        
        public boolean isError();
        
        public Address getRemoveAddress64();
    }
    
    public interface Address {
        public int[] getAddress();
    }
    
    public interface PacketListener {
        public void processResponse(Packet p);
    }
}
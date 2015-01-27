package ccre.rconf;

public interface RConfable {
    public RConf.Entry[] queryRConf() throws InterruptedException;
    
    public void signalRConf(int field, byte[] data) throws InterruptedException;
}

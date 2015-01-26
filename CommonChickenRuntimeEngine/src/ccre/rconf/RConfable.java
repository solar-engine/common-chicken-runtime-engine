package ccre.rconf;

public interface RConfable {
    public RConf.Entry[] queryRConf();
    
    public void signalRConf(int field, byte[] data);
}

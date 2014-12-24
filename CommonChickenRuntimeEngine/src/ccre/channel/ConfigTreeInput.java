package ccre.channel;

public interface ConfigTreeInput extends ConfigTreeInputPoll {
    public void send(ConfigTreeOutput output);
    
    public void unsend(ConfigTreeOutput output);
}

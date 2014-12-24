package ccre.channel;

import ccre.concurrency.ConcurrentDispatchArray;
import ccre.config.ConfigTree;
import ccre.util.CArrayUtils;

public class ConfigTreeStatus implements ConfigTreeInput, ConfigTreeOutput {

    private ConfigTree<?> value = new ConfigTree<String>(null, null, null);
    private ConcurrentDispatchArray<ConfigTreeOutput> consumers = null;
    
    public ConfigTreeStatus() {
    }
    
    public ConfigTreeStatus(ConfigTree<?> default_) {
        this.set(default_);
    }
     
    public ConfigTreeStatus(ConfigTreeOutput target) {
        consumers = new ConcurrentDispatchArray<ConfigTreeOutput>();
        consumers.add(target);
        target.set(new ConfigTree<String>(null, null, null));
    }
    
    public ConfigTreeStatus(ConfigTreeOutput... targets) {
        consumers = new ConcurrentDispatchArray<ConfigTreeOutput>(CArrayUtils.asList(targets));
        for (ConfigTreeOutput t : targets) {
            t.set(new ConfigTree<String>(null, null, null));
        }
    }
    
    public boolean hasConsumers() {
        return consumers != null && !consumers.isEmpty();
    }

    public synchronized ConfigTree<?> get() {
        return value;
    }
    
    
    public void set(ConfigTree<?> value) {
        if (this.value == value) {
            return;
        }
        
        this.value = value;
        if (consumers != null) {
            for (ConfigTreeOutput output: consumers) {
                output.set(value);
            }
        }
    }

    public synchronized void send(ConfigTreeOutput output) {
        if (consumers == null) {
            consumers = new ConcurrentDispatchArray<ConfigTreeOutput>();
        }
        consumers.add(output);
        output.set(value);
    }

    public synchronized void unsend(ConfigTreeOutput output) {
        if (consumers != null) {
            if (consumers.remove(output) && consumers.isEmpty()) {
                consumers = null;
            }
        }
    }


}

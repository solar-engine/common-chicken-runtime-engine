package ccre.config;

import ccre.util.UniqueIds;

public class ConfigTreeFloatNode extends ConfigTreeNode<Float> {
    public ConfigTreeFloatNode() {
        this.name = UniqueIds.global.nextHexId("float-node");
        this.value = 0.0f;
    }
    
    public ConfigTreeFloatNode(String name) {
        this.name = name;
        this.value = 0.0f;
    }
    
    public ConfigTreeFloatNode(Float value) {
        this.name = UniqueIds.global.nextHexId("float-node");
        this.value = value;
    }
    public ConfigTreeFloatNode(String name, Float value) {
        this.name = name;
        this.value = value;
    }
    
}

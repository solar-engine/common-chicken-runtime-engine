package ccre.config;

import ccre.util.UniqueIds;

public class ConfigTreeBoolNode extends ConfigTreeNode<Boolean> {
    public ConfigTreeBoolNode() {
        this.name = UniqueIds.global.nextHexId("bool-node");
        this.value = false;
    }
    
    public ConfigTreeBoolNode(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

}

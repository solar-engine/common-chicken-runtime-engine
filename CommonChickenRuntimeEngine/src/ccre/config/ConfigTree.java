package ccre.config;

public class ConfigTree<T> {
    public String name;
    public T value;
    public ConfigTree<?>[] branches;
       
    public ConfigTree(String name, T value, ConfigTree<?>[] branches) {
        this.name = name;
        this.value = value;
        this.branches = branches;
    }
    
    public ConfigTree<?> getNode(String name) {
        for (ConfigTree<?> branch: branches) {
            if (branch.name == name) {
                return branch;
            }
        }
        
        return null;
    }
        
    public String description() {
        String desc = name + ": " + value;
        
        if (branches != null && branches.length > 0){
            desc += "\nSubnodes: [\n";
            for (ConfigTree<?> branch: branches) {
                desc += branch.description() + "\n";
            }
            desc += "]";
        }
        
        return desc;
    }
}
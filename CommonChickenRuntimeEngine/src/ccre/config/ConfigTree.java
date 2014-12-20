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
    
    public ConfigTree()
    
    public String description() {
        String desc = name + ": " + value;
        
        if (branches.length > 0){
            desc += "\nSubnodes: [\n";
            for (int i = 0; i < branches.length; i++) {
                desc += branches[i].description() + "\n";
            }
            desc += "]";
        }
        
        return desc;
    }
}
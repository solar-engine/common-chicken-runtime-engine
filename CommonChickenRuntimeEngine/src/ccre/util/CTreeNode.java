package ccre.util;

public class CTreeNode<T> {
    
    public String name;
    public T value;
    public CTreeNode<?>[] branches;
       
    public CTreeNode(String name, T value, CTreeNode<?>[] branches) {
        this.name = name;
        this.value = value;
        this.branches = branches;
    }
    
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
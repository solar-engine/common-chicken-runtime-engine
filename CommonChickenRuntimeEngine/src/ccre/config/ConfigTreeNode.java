package ccre.config;

public abstract class ConfigTreeNode<T> {
    String name;
    T value;
    
    public String description() {
      return "" + value;  
    }
}

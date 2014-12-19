package ccre.util;

public class CTree<T> {
    public CTreeNode<T> root;
    
    public CTree(CTreeNode<T> root) {
        this.root = root;
    }
    
    public String description() {
        return root.description();
    }
}
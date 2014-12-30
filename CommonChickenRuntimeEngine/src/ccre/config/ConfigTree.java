package ccre.config;

import ccre.util.CArrayList;

public class ConfigTree {
    private final String name;
    private final CArrayList<ConfigTree> branches;
    private final CArrayList<ConfigTreeNode<?>> nodes;
    
    public ConfigTree(String name) {
        this.name = name;
        this.branches = new CArrayList<ConfigTree>();
        this.nodes = new CArrayList<ConfigTreeNode<?>>();
    }
    
    protected ConfigTree(String name, ConfigTree[] branches, ConfigTreeNode<?>[] values) {
        this.name = name;
        this.branches = new CArrayList<ConfigTree>(branches);
        this.nodes = new CArrayList<ConfigTreeNode<?>>(values);
    }
    
    
    public ConfigTreeNode<?> getNode(String path) {
        return getNode(path, null);   
    }
    
    private ConfigTreeNode<?> getNodeRecursive(CArrayList<String> pathComponents, ConfigTreeNode<?> default_) {
        String firstPathComponent = pathComponents.get(0);

        if (pathComponents.size() == 1) {            
            ConfigTreeNode<?> currentNode = null;
            for (ConfigTreeNode<?> node: nodes) {
                if (node.name.equals(pathComponents.get(0))) {
                    currentNode = node;
                    break;
                }
            }
            if (currentNode == null) {
                if (default_ != null){
                    nodes.add(default_);
                }
                return default_;
            }         
            return currentNode;
        }
        
        ConfigTree nextBranch = null;
        for (ConfigTree branch: branches) {
            if (branch.name.equals(firstPathComponent)) {
                nextBranch = branch;
                break;
            }
        }
        
        if (nextBranch == null) {
            nextBranch = this.getSubtree(firstPathComponent);
        }
        
        pathComponents.remove(0);
        return nextBranch.getNodeRecursive(pathComponents, default_);
    }
    
    public ConfigTreeNode<?> getNode(String path, ConfigTreeNode<?> default_) {        
        String[] pathComponents = path.split("/");
        CArrayList<String> pathComponentsArray = new CArrayList<String>(pathComponents);

        return this.getNodeRecursive(pathComponentsArray, default_);
    }
    
    private ConfigTree getNodeParent(String path) {
        CArrayList<String> parentPathComponents = new CArrayList<String>(path.split("/"));
        parentPathComponents.remove(parentPathComponents.size() - 1);
        
        ConfigTree currentBranch = this;
        for (String pathComponent: parentPathComponents) {
            for (ConfigTree branch: currentBranch.branches) {
                if (branch.name.equals(pathComponent)) {
                    currentBranch = branch;
                    break;
                }
            }
        }
        
        return currentBranch;
    }

    public ConfigTreeNode<?> setNode(String path, ConfigTreeNode<?> node) {
        ConfigTreeNode<?> currentNode = this.getNode(path);
        if (currentNode == null) {
            return this.getNode(path, node);
        } else {
            ConfigTree parent = this.getNodeParent(path);
            int index = parent.nodes.indexOf(currentNode);
            parent.nodes.set(index, node);
            return node;
        }
    }
    
    // Get float.
    public Float getFloat(String path) {
        ConfigTreeFloatNode node = (ConfigTreeFloatNode) this.getNode(path);
        return node.value;
    }
    
    // Get float. (And, if it doesn't exist, create it.)
    public Float getFloat(String path, float default_) {
        String[] pathComponents = path.split("/");
        String lastComponent = pathComponents[pathComponents.length - 1];
        ConfigTreeFloatNode node = new ConfigTreeFloatNode(lastComponent, default_);
        
        ConfigTreeFloatNode newNode = (ConfigTreeFloatNode) this.getNode(path, node);
        return newNode.value;
    }
    
    // Set float. (And, if it doesn't exist, create it.)
    public Float setFloat(String path, float value) {
        String[] pathComponents = path.split("/");
        String lastComponent = pathComponents[pathComponents.length - 1];

        ConfigTreeFloatNode node = new ConfigTreeFloatNode(lastComponent, value);
        
        ConfigTreeFloatNode newNode = (ConfigTreeFloatNode) this.setNode(path, node);
        return newNode.value;

        
    }
    
    public Boolean getBool(String path) {
        ConfigTreeBoolNode node = (ConfigTreeBoolNode) this.getNode(path);
        return node.value;
    }

    public Boolean getBoolean(String path, boolean default_) {
        String[] pathComponents = path.split("/");
        String lastComponent = pathComponents[pathComponents.length - 1];
        ConfigTreeBoolNode node = new ConfigTreeBoolNode(lastComponent, default_);
        
        ConfigTreeBoolNode newNode = (ConfigTreeBoolNode) this.getNode(path, node);
        return newNode.value;
    }
    
    public Boolean setBoolean(String path, boolean value) {
        String[] pathComponents = path.split("/");
        String lastComponent = pathComponents[pathComponents.length - 1];

        ConfigTreeBoolNode node = new ConfigTreeBoolNode(lastComponent, value);
        
        ConfigTreeBoolNode newNode = (ConfigTreeBoolNode) this.setNode(path, node);
        return newNode.value;
    }

    
    // Get subtrees.
    public ConfigTree getSubtree(String path) {
        return this.getSubtree(path, null);
    }
    
    public ConfigTree getSubtree(String path, ConfigTree default_) {
        String[] pathComponents = path.split("/");
        CArrayList<String> pathComponentsArray = new CArrayList<String>(pathComponents);
        return this.getSubtreeRecursive(pathComponentsArray, default_);
    }

    private ConfigTree getSubtreeRecursive(CArrayList<String> pathComponents, ConfigTree default_) {
        String firstPathComponent = pathComponents.get(0);

        if (pathComponents.size() == 1) {
            
            ConfigTree currentBranch = null;
            for (ConfigTree branch: branches) {
                if (branch.name == firstPathComponent) {
                    currentBranch = branch;
                }
            }
            
            if (currentBranch == null) {
                if (default_ != null) {
                    branches.add(default_);
                    return default_;
                } else {
                    ConfigTree tempConfigTree = new ConfigTree(firstPathComponent);
                    branches.add(tempConfigTree);
                    return tempConfigTree;
                }
            }         
            return currentBranch;
        }
        
        ConfigTree nextBranch = null;
        for (ConfigTree branch: branches) {
            if (branch.name == firstPathComponent) {
                nextBranch = branch;
                break;
            }
        }
        
        if (nextBranch == null) {
            nextBranch = this.getSubtree(firstPathComponent);
        }
        
        pathComponents.remove(0);
        return nextBranch.getSubtreeRecursive(pathComponents, default_);

    }
    // Set subtree.
    public ConfigTree setSubtree(String path, ConfigTree tree) {
        ConfigTree currentTree = this.getSubtree(path, tree);
        currentTree = tree;
        return currentTree;
    }
    // Utilities.
    public String description() {
        String desc = name;
        
        if (branches != null && branches.size() > 0){
            desc += "\nSubtrees: [";
            for (ConfigTree branch: branches) {
                desc += "\n" + branch.description();
            }
            desc += " ]";
        }
        if (nodes != null && nodes.size() > 0) {
            desc += "\nValues: [";
            for (ConfigTreeNode<?> node: nodes) {
                desc += "\n" + node.description();
            }
            desc += " ]";
        }
        
        return desc;
    }
}
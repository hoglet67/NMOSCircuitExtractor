package com.hoglet.nmoscircuitextractor;

public class ModuleNode extends CircuitNode {

    private String name;

    public ModuleNode(String name, String id) {
        super(NodeType.VT_MODULE, id);
        this.setName(name);
    }

    public String toString() {
        return getId() + "(" + getType().name() + ")";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

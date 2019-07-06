package com.hoglet.nmoscircuitextractor;

public class ModuleNode extends CircuitNode {

    public ModuleNode(String id) {
        super(NodeType.VT_MODULE, id);
    }

    public String toString() {
        return getId() + "(" + getType().name() + ")";
    }

}

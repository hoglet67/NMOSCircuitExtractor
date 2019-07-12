package com.hoglet.nmoscircuitextractor;

public class PinNode extends CircuitNode {
    
    public PinNode(String id) {
        super(NodeType.VT_PIN, id);
    }
    
    public String toString() {
        return getId() + "(" + getType().name() + ")";
    }
}

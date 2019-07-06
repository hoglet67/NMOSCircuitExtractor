package com.hoglet.nmoscircuitextractor;

public abstract class CircuitNode {

    public enum NodeType {
        VT_NET, VT_NET_EXT, VT_EFET, VT_EFET_VCC, VT_EFET_VSS, VT_DPULLUP, VT_EPULLUP, VT_MODULE, VT_NUM_TYPES,
    }

    private NodeType type;
    private String id;

    public CircuitNode(NodeType type, String id) {
        this.type = type;
        this.id = id;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }
    
    public String getId() {
        return id;
    }
    
    public boolean isCombinable() {
        return type == NodeType.VT_EFET || type == NodeType.VT_EFET_VSS;
    }
    
    public String toString() {
        return id;
    }
}

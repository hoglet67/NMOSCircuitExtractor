package com.hoglet.nmoscircuitextractor;

public abstract class CircuitNode {

    public enum NodeType {
        VT_NET, VT_NET_EXT, VT_EFET, VT_EFET_VCC, VT_EFET_VSS, VT_DPULLUP, VT_EPULLUP, VT_NUM_TYPES,
    }

    protected NodeType type;
    protected String id;

    public CircuitNode(NodeType type, String id) {
        this.type = type;
        this.id = id;
    }

    public NodeType getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}

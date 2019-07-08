package com.hoglet.nmoscircuitextractor;

public abstract class CircuitNode implements Comparable<CircuitNode>{

    public enum NodeType {
        VT_NET, VT_EFET_VSS, VT_EFET, VT_EFET_VCC, VT_EPULLUP, VT_DPULLUP, VT_MODULE, VT_PIN, VT_NUM_TYPES,
    }

    private NodeType type;
    private String id;
    private boolean external;

    public CircuitNode(NodeType type, String id) {
        this.type = type;
        this.id = id;
        this.external = false;
    }

    public NodeType getType() {
        return type;
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
    
    public void setExternal(boolean external) {
        this.external = external;
    }
    
    public boolean isExternal() {
        return external;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof CircuitNode) {
            CircuitNode cn = (CircuitNode) o;
            if (type == cn.type && id.equals(cn.id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (type.name() + "_" + id).hashCode();
    }

    @Override
    public int compareTo(CircuitNode o) {
        return (type.name() + "_" + id).compareTo(o.type.name() + "_" + o.id);
    }
    
}

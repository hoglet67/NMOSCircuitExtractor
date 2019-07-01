package com.hoglet.nmoscircuitextractor;

public class TransistorNode extends CircuitNode {

    protected Integer gate;
    protected Integer c1;
    protected Integer c2;

    public TransistorNode(NodeType type, String id, Integer gate, Integer c1, Integer c2) {
        super(type, id);
        this.gate = gate;
        // Canonicalize c1 and c2 such that c1 is always connected to the lower
        // numbered net
        if (c1 != null && c2 != null && c2 < c1) {
            Integer tmp = c1;
            c1 = c2;
            c2 = tmp;
        }
        this.c1 = c1;
        this.c2 = c2;
    }

    // Two transistors are deemed equal if they are of the same type and have
    // the same net connections
    public boolean equals(Object o) {
        if (!(o instanceof TransistorNode)) {
            return false;
        }
        TransistorNode tn = (TransistorNode) o;
        if (type != tn.type) {
            return false;
        }
        if (gate != null && tn.gate != null && gate.intValue() != tn.gate.intValue()) {
            return false;
        }
        if (gate == null && tn.gate != null || gate != null && tn.gate == null) {
            return false;
        }
        if (c1 != null && tn.c1 != null && c1.intValue() != tn.c1.intValue()) {
            return false;
        }
        if (c1 == null && tn.c1 != null || c1 != null && tn.c1 == null) {
            return false;
        }
        if (c2 != null && tn.c2 != null && c2.intValue() != tn.c2.intValue()) {
            return false;
        }
        if (c2 == null && tn.c2 != null || c2 != null && tn.c2 == null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = type.hashCode();
        if (gate != null) {
            hash *= 13;
            hash += gate.hashCode();
        }
        if (c1 != null) {
            hash *= 19;
            hash += c1.hashCode();
        }
        if (c2 != null) {
            hash *= 31;
            hash += c2.hashCode();
        }
        return hash;
    }
}

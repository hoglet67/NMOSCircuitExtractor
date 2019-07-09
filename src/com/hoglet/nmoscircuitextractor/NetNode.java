package com.hoglet.nmoscircuitextractor;

public class NetNode extends CircuitNode {

    private int degree;
    private boolean gateOnly;

    public NetNode(String id) {
        super(NodeType.VT_NET, id);
        degree = 0;
        gateOnly = false;
    }

    public int getDegree() {
        return degree;
    }

    public void incDegree() {
        degree++;
    }

    public boolean isGateOnly() {
        return gateOnly;
    }

    public void setGateOnly(boolean gateOnly) {
        this.gateOnly = gateOnly;
    }
}

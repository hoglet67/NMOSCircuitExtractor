package com.hoglet.nmoscircuitextractor;

public class NetNode extends CircuitNode {

    private int degree;

    public NetNode(String id) {
        super(NodeType.VT_NET, id);
        degree = 0;
    }

    public int getDegree() {
        return degree;
    }

    public void incDegree() {
        degree++;
    }

    public void setAsExternal() {
        type = NodeType.VT_NET_EXT;
    }
}

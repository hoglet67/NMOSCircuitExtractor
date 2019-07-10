package com.hoglet.nmoscircuitextractor;

public class NetNode extends CircuitNode {

    private int degree;
    private int channelConstraint;

    public NetNode(String id) {
        super(NodeType.VT_NET, id);
        degree = 0;
        channelConstraint = -1;
    }

    public int getDegree() {
        return degree;
    }

    public void incDegree() {
        degree++;
    }

    public boolean hasChannelConstraint() {
        return channelConstraint >= 0;
    }

    public int getChannelConstraint() {
        return channelConstraint;
    }

    public void setChannelConstraint(int channelConstraint) {
        this.channelConstraint = channelConstraint;
    }
}

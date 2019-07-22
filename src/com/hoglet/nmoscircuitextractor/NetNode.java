package com.hoglet.nmoscircuitextractor;

public class NetNode extends CircuitNode {

    private int degree;
    private int channelConstraint;
    private boolean digital;

    public NetNode(String id) {
        super(NodeType.VT_NET, id);
        degree = 0;
        channelConstraint = -1;
        setDigital(false);
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

    public boolean isDigital() {
        return digital;
    }

    public void setDigital(boolean digital) {
        this.digital = digital;
    }
}

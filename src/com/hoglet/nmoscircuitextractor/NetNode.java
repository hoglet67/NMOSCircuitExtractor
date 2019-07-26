package com.hoglet.nmoscircuitextractor;

public class NetNode extends CircuitNode {

    private int degree;
    private int channelConstraint;
    private boolean digital;
    private boolean external;
    private boolean global;

    public NetNode(String id) {
        super(NodeType.VT_NET, id);
        degree = 0;
        channelConstraint = -1;
        this.digital = false;
        this.external = false;
        this.global = false;
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

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}

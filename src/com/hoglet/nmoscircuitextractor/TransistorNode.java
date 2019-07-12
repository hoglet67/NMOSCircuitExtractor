package com.hoglet.nmoscircuitextractor;

public class TransistorNode extends CircuitNode {

    private String function = null;

    public TransistorNode(NodeType type, String id) {
        super(type, id);
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String toString() {
        String tag;
        if (isPass()) {
            if (isTree()) {
                tag = "tag:both";
            } else {
                tag = "tag:pass";
            }
        } else {
            if (isTree()) {
                tag = "tag:tree";
            } else {
                tag = "tag:none";
            }
        }
        return getId() + "(" + getType().name() + ") " + tag;
    }

}

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
        return getId() + "(" + getType().name() + ")" + (isTree() ? "" : "#");
    }

}

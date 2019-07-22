package com.hoglet.nmoscircuitextractor;

public class FunctionNode extends CircuitNode {

    private String function = null;

    public FunctionNode(String id) {
        super(NodeType.VT_FUNCTION, id);
    }

    public String toString() {
        return getId() + "(" + getType().name() + ")";
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

}

package com.hoglet.nmoscircuitextractor;

public class FunctionNode extends CircuitNode implements IFunction {

    private String function = null;
    private boolean init;

    public FunctionNode(String id) {
        super(NodeType.VT_FUNCTION, id);
        init = false;
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

    public boolean getInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

}

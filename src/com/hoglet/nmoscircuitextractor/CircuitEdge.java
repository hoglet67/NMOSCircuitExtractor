package com.hoglet.nmoscircuitextractor;

import org.jgrapht.graph.DefaultEdge;

public class CircuitEdge extends DefaultEdge {

    public enum EdgeType {
        GATE, CHANNEL,
    }

    private static final long serialVersionUID = -9006421010257447181L;

    private EdgeType type;

    public CircuitEdge() {
        super();
    }

    public CircuitEdge(EdgeType type) {
        this.type = type;
    }

    public void setGate() {
        this.type = EdgeType.GATE;
    }

    public void setChannel() {
        this.type = EdgeType.CHANNEL;
    }

    public EdgeType getType() {
        return type;
    }

}

package com.hoglet.nmoscircuitextractor;

import org.jgrapht.graph.DefaultEdge;

public class CircuitEdge extends DefaultEdge {

    public enum EdgeType {
        UNSPECIFIED, GATE, CHANNEL, INPUT, OUTPUT, BIDIRECTIONAL, ET_NUM_TYPES
    }

    private static final long serialVersionUID = -9006421010257447181L;

    private EdgeType type;

    public CircuitEdge() {
        this(EdgeType.UNSPECIFIED);
    }

    public CircuitEdge(EdgeType type) {
        super();
        this.type = type;
    }

    public void setType(EdgeType type) {
        this.type = type;
    }

    public EdgeType getType() {
        return type;
    }

}

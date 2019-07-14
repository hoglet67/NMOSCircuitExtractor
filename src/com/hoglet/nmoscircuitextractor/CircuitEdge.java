package com.hoglet.nmoscircuitextractor;

import org.jgrapht.graph.DefaultEdge;

public class CircuitEdge extends DefaultEdge {

    public enum EdgeType {
        UNSPECIFIED, GATE, CHANNEL, PULLUP, INPUT, OUTPUT, BIDIRECTIONAL
    }

    private static final long serialVersionUID = -9006421010257447181L;

    private EdgeType type;
    private String name;
    private int port;

    public CircuitEdge() {
        this(EdgeType.UNSPECIFIED);
        this.port = -1; // unassigned
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}

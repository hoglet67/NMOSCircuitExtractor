package com.hoglet.nmoscircuitextractor;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class ModulePort {

    private String name;
    private EdgeType type;
    private NetNode net;

    public ModulePort(String name, EdgeType type, NetNode net) {
        this.name = name;
        this.type = type;
        this.net = net;
    }

    public String getName() {
        return name;
    }

    public EdgeType getType() {
        return type;
    }

    public NetNode getNet() {
        return net;
    }

}

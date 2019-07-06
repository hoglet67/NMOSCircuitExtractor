package com.hoglet.nmoscircuitextractor;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class ModulePort {
    
    private EdgeType type;
    private NetNode net;
    
    public ModulePort(EdgeType type, NetNode net) {
        this.type = type;
        this.net = net;
    }

    public EdgeType getType() {
        return type;
    }

    public NetNode getNet() {
        return net;        
    }
}

package com.hoglet.nmoscircuitextractor;

import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;

public class Module {

    private Graph<CircuitNode, CircuitEdge> graph;
    private List<NetNode> ports = new LinkedList<NetNode>();
 
    public Module(Graph<CircuitNode, CircuitEdge> graph, List<NetNode> ports) {
        this.graph = graph;
        this.ports = ports;
    }
    
    public Graph<CircuitNode, CircuitEdge> getGraph() {
        return graph;
    }
    
    List<NetNode> getPorts() {
        return ports;
    }
}

package com.hoglet.nmoscircuitextractor;

import java.util.List;

import org.jgrapht.Graph;

public class Module {

    private String name;
    private Graph<CircuitNode, CircuitEdge> graph;
    private List<ModulePort> ports;

    public Module(String name, Graph<CircuitNode, CircuitEdge> graph, List<ModulePort> ports) {
        this.name = name;
        this.graph = graph;
        this.ports = ports;
    }

    public Graph<CircuitNode, CircuitEdge> getGraph() {
        return graph;
    }

    public List<ModulePort> getPorts() {
        return ports;
    }

    public String getName() {
        return name;
    }
}

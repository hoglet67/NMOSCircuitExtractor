package com.hoglet.nmoscircuitextractor;

import java.util.Comparator;

import org.jgrapht.Graph;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class CircuitNodeComparator implements Comparator<CircuitNode> {

    protected Graph<CircuitNode, CircuitEdge> graph;

    public CircuitNodeComparator(Graph<CircuitNode, CircuitEdge> graph) {
        this.graph = graph;
    }

    @Override
    public int compare(CircuitNode arg0, CircuitNode arg1) {
        if (arg0 instanceof NetNode && arg1 instanceof NetNode) {
            NetNode net0 = (NetNode) arg0;
            NetNode net1 = (NetNode) arg1;
            if (net0.isExternal() || net1.isExternal()) {
                NetNode net = net0.isGateOnly() ? net1 : net1.isGateOnly() ? net0 : null;
                if (net == null) {
                    return 0;
                } else {
                    int counts[] = new int[EdgeType.values().length];
                    for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
                        counts[edge.getType().ordinal()]++;
                    }
                    if (counts[EdgeType.CHANNEL.ordinal()] == 1 && counts[EdgeType.PULLUP.ordinal()] == 0
                            && counts[EdgeType.INPUT.ordinal()] == 0 && counts[EdgeType.OUTPUT.ordinal()] == 0
                            && counts[EdgeType.BIDIRECTIONAL.ordinal()] == 0) {
                        return 0;
                    } else {
                        return 1; // ???
                    }
                }
            } else {
                return net1.getDegree() - net0.getDegree();
            }
        } else {
            return arg0.getType().compareTo(arg1.getType());
        }
    }

}

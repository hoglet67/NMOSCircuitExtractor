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
            if (net0.isGlobal() || net1.isGlobal()) {
                // Case 1: the net is global, so match on name
                return net0.getId().compareTo(net1.getId());
            } else if (net0.isExternal() || net1.isExternal()) {
                // Case 2: the net is external, so match on constraints
                int channnelConstraint = -1;
                NetNode net = null;
                if (net0.hasChannelConstraint()) {
                    channnelConstraint = net0.getChannelConstraint();
                    net = net1;
                } else if (net1.hasChannelConstraint()) {
                    channnelConstraint = net1.getChannelConstraint();
                    net = net0;
                }
                if (net == null) {
                    return 0;
                } else {
                    int counts[] = new int[EdgeType.values().length];
                    for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
                        counts[edge.getType().ordinal()]++;
                    }
                    if (counts[EdgeType.CHANNEL.ordinal()] == channnelConstraint && counts[EdgeType.PULLUP.ordinal()] == 0
                            && (counts[EdgeType.INPUT.ordinal()] > 0 || counts[EdgeType.GATE.ordinal()] > 0)
                            && counts[EdgeType.OUTPUT.ordinal()] == 0 && counts[EdgeType.BIDIRECTIONAL.ordinal()] == 0) {
                        return 0;
                    } else {
                        return 1; // ???
                    }
                }
            } else {
                // Case 4: the net is internal, so match on degree
                return net1.getDegree() - net0.getDegree();
            }
        } else {
            return arg0.getType().compareTo(arg1.getType());
        }
    }

}

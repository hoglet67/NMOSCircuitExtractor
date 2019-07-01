package com.hoglet.nmoscircuitextractor;

import java.util.Comparator;

import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class CircuitNodeComparator implements Comparator<CircuitNode> {

    @Override
    public int compare(CircuitNode arg0, CircuitNode arg1) {
        if (arg0 instanceof NetNode && arg1 instanceof NetNode) {
            if (((NetNode) arg0).getType() == NodeType.VT_NET_EXT) {
                return 0;
            } else if (((NetNode) arg1).getType() == NodeType.VT_NET_EXT) {
                return 0;
            } else {
                return ((NetNode) arg1).getDegree() - ((NetNode) arg0).getDegree();
            }
        } else {
            return arg0.getType().compareTo(arg1.getType());
        }
    }

}

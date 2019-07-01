package com.hoglet.nmoscircuitextractor;

import java.util.Comparator;

public class CircuitNodeComparator implements Comparator<CircuitNode> {

    @Override
    public int compare(CircuitNode arg0, CircuitNode arg1) {
        if (arg0 instanceof NetNode && arg1 instanceof NetNode) {
            return ((NetNode) arg1).getDegree() - ((NetNode) arg0).getDegree();
        } else {
            return arg0.getType().compareTo(arg1.getType());
        }
    }

}

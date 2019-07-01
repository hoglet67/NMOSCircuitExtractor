package com.hoglet.nmoscircuitextractor;

import java.util.Comparator;

public class CircuitEdgeCompator implements Comparator<CircuitEdge> {

    @Override
    public int compare(CircuitEdge o1, CircuitEdge o2) {
        return o1.getType().compareTo(o2.getType());
    }

}

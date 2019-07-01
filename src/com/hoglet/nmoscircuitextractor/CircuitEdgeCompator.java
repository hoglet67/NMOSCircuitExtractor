package com.hoglet.nmoscircuitextractor;

import java.util.Comparator;

public class CircuitEdgeCompator implements Comparator<CircuitEdge> {

    @Override
    public int compare(CircuitEdge o1, CircuitEdge o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null && o2 != null) {
            return -1;
        } else if (o1 != null && o2 == null) {
            return 1;
        } else {
            return o1.getType().compareTo(o2.getType());
        }
    }

}

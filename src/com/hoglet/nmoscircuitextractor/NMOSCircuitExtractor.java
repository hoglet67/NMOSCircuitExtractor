package com.hoglet.nmoscircuitextractor;

import java.io.File;
import java.util.Iterator;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.IsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;

import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class NMOSCircuitExtractor {

    public static void main(String args[]) {
        try {
            // Parse the Z80 into the main graph
            File transdefs = new File("transdefs.js");
            File segdefs = new File("segdefs.js");
            CircuitGraphBuilder graphBuilder = new CircuitGraphBuilder();
            Graph<CircuitNode, CircuitEdge> graph = graphBuilder.readNetlist(transdefs, segdefs);

            // Build a small subgraph that looks like the register cell
            CircuitGraphBuilder subgraphBuilder = new CircuitGraphBuilder();
            TransistorNode tr1 = new TransistorNode(NodeType.VT_EFET_VSS, "1", 100, 101, null);
            TransistorNode tr2 = new TransistorNode(NodeType.VT_EFET_VSS, "2", 101, 100, null);
            TransistorNode tr3 = new TransistorNode(NodeType.VT_EFET, "3", null, 100, null);
            TransistorNode tr4 = new TransistorNode(NodeType.VT_EFET, "4", null, 101, null);
            TransistorNode tr5 = new TransistorNode(NodeType.VT_DPULLUP, "5", null, 100, null);
            TransistorNode tr6 = new TransistorNode(NodeType.VT_DPULLUP, "6", null, 101, null);
            subgraphBuilder.addTransistor(tr1);
            subgraphBuilder.addTransistor(tr2);
            subgraphBuilder.addTransistor(tr3);
            subgraphBuilder.addTransistor(tr4);
            subgraphBuilder.addTransistor(tr5);
            subgraphBuilder.addTransistor(tr6);
            Graph<CircuitNode, CircuitEdge> subgraph = subgraphBuilder.getGraph();

            // Look for instances of the subgraph in the main graph
            IsomorphismInspector<CircuitNode, CircuitEdge> inspector = new VF2SubgraphIsomorphismInspector<CircuitNode, CircuitEdge>(
                    graph, subgraph, new CircuitNodeComparator(), new CircuitEdgeCompator());
            Iterator<GraphMapping<CircuitNode, CircuitEdge>> it = inspector.getMappings();
            int count = 0;
            while (it.hasNext()) {
                GraphMapping<CircuitNode, CircuitEdge> mapping = it.next();
                System.out.println("Mapping: " + mapping.getVertexCorrespondence(tr1, false).getId() + " "
                        + mapping.getVertexCorrespondence(tr2, false).getId());
                count++;
            }
            System.out.println("Found " + count + " mappings");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

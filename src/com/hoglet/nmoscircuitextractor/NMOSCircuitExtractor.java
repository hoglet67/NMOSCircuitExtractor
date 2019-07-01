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
        int net_vss = 1;
        int net_vcc = 2;

        try {
            // Parse the Z80 into the main graph
            File transdefs = new File("transdefs.js");
            File segdefs = new File("segdefs.js");
            CircuitGraphBuilder graphBuilder = new CircuitGraphBuilder();
            Graph<CircuitNode, CircuitEdge> graph = graphBuilder.readNetlist(transdefs, segdefs);
            graphBuilder.dumpStats();

            // Build a small subgraph that looks like the register cell
            CircuitGraphBuilder subgraphBuilder = new CircuitGraphBuilder();
            NetNode p1 = subgraphBuilder.addExternal(200);
            NetNode p2 = subgraphBuilder.addExternal(201);
            NetNode p3 = subgraphBuilder.addExternal(202);
            TransistorNode tr1 = subgraphBuilder.addTransistor("1", 100, 101, net_vss);
            TransistorNode tr2 = subgraphBuilder.addTransistor("2", 101, 100, net_vss);
            TransistorNode tr3 = subgraphBuilder.addTransistor("3", 202, 100, 200);
            TransistorNode tr4 = subgraphBuilder.addTransistor("4", 202, 101, 201);
            TransistorNode tr5 = subgraphBuilder.addPullup("5", 100);
            TransistorNode tr6 = subgraphBuilder.addPullup("6", 101);
            Graph<CircuitNode, CircuitEdge> subgraph = subgraphBuilder.getGraph();

            // Look for instances of the subgraph in the main graph
            IsomorphismInspector<CircuitNode, CircuitEdge> inspector = new VF2SubgraphIsomorphismInspector<CircuitNode, CircuitEdge>(
                    graph, subgraph, new CircuitNodeComparator(), new CircuitEdgeCompator());
            Iterator<GraphMapping<CircuitNode, CircuitEdge>> it = inspector.getMappings();
            int count = 0;
            while (it.hasNext()) {
                GraphMapping<CircuitNode, CircuitEdge> mapping = it.next();

                // Log the mapping
                System.out.println("Port  p1 =>" + mapping.getVertexCorrespondence(p1, false).getId());
                System.out.println("Port  p2 =>" + mapping.getVertexCorrespondence(p2, false).getId());
                System.out.println("Port  p3 =>" + mapping.getVertexCorrespondence(p3, false).getId());
                System.out.println("Port tr1 =>" + mapping.getVertexCorrespondence(tr1, false).getId());
                System.out.println("Port tr2 =>" + mapping.getVertexCorrespondence(tr2, false).getId());

                // Remove the components
                for (CircuitNode subcn : subgraph.vertexSet()) {
                    if (subcn.getType() != NodeType.VT_NET_EXT) {
                        CircuitNode cn = mapping.getVertexCorrespondence(subcn, false);
                        graph.removeVertex(cn);
                    }
                }

                count++;
            }
            System.out.println("Found " + count + " mappings");

            graphBuilder.dumpStats();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

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
            graphBuilder.dumpStats();

            Module mod = ModuleGen.registerModule();

            // Look for instances of the subgraph in the main graph
            IsomorphismInspector<CircuitNode, CircuitEdge> inspector = new VF2SubgraphIsomorphismInspector<CircuitNode, CircuitEdge>(
                    graph, mod.getGraph(), new CircuitNodeComparator(), new CircuitEdgeCompator());
            Iterator<GraphMapping<CircuitNode, CircuitEdge>> it = inspector.getMappings();
            int count = 0;
            while (it.hasNext()) {
                GraphMapping<CircuitNode, CircuitEdge> mapping = it.next();

                // Log the mapping
                for (NetNode subp : mod.getPorts()) {
                    System.out.println("Port  " + subp.id + " =>" + mapping.getVertexCorrespondence(subp, false).getId());
                }
                
                // Remove the components
                for (CircuitNode subcn : mod.getGraph().vertexSet()) {
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

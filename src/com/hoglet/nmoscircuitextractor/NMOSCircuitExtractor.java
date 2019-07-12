package com.hoglet.nmoscircuitextractor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;
import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class NMOSCircuitExtractor {

    public static void main(String args[]) {
        try {

            boolean validate = true;

            Set<NetNode> ignoreWarnings = new HashSet<NetNode>();

            // Parse the Z80 into the main graph
            File transdefs = new File("transdefs.js");
            File segdefs = new File("segdefs.js");
            File nodenames = new File("nodenames.js");
            CircuitGraphBuilder builder = new CircuitGraphBuilder();
            builder.readNetlist(transdefs, segdefs, nodenames);

            // Note, in these pin definitions, the edge indicates the connection
            // of the pin to the internal circuitry, so is the opposite of what
            // might be expected. e.g. An input pin is connected with an OUTPUT
            // edge
            // because it drives internal signal.

            builder.addPin("clk", EdgeType.OUTPUT);
            builder.addPin("ab0", EdgeType.INPUT);
            builder.addPin("ab1", EdgeType.INPUT);
            builder.addPin("ab2", EdgeType.INPUT);
            builder.addPin("ab3", EdgeType.INPUT);
            builder.addPin("ab4", EdgeType.INPUT);
            builder.addPin("ab5", EdgeType.INPUT);
            builder.addPin("ab6", EdgeType.INPUT);
            builder.addPin("ab7", EdgeType.INPUT);
            builder.addPin("ab8", EdgeType.INPUT);
            builder.addPin("ab9", EdgeType.INPUT);
            builder.addPin("ab10", EdgeType.INPUT);
            builder.addPin("ab11", EdgeType.INPUT);
            builder.addPin("ab12", EdgeType.INPUT);
            builder.addPin("ab13", EdgeType.INPUT);
            builder.addPin("ab14", EdgeType.INPUT);
            builder.addPin("ab15", EdgeType.INPUT);
            builder.addPin("_reset", EdgeType.OUTPUT);
            builder.addPin("_wait", EdgeType.OUTPUT);
            builder.addPin("_int", EdgeType.OUTPUT);
            builder.addPin("_nmi", EdgeType.OUTPUT);
            builder.addPin("_busrq", EdgeType.OUTPUT);
            builder.addPin("_m1", EdgeType.INPUT);
            builder.addPin("_rd", EdgeType.INPUT);
            builder.addPin("_wr", EdgeType.INPUT);
            builder.addPin("_mreq", EdgeType.INPUT);
            builder.addPin("_iorq", EdgeType.INPUT);
            builder.addPin("_rfsh", EdgeType.INPUT);
            builder.addPin("db0", EdgeType.BIDIRECTIONAL);
            builder.addPin("db1", EdgeType.BIDIRECTIONAL);
            builder.addPin("db2", EdgeType.BIDIRECTIONAL);
            builder.addPin("db3", EdgeType.BIDIRECTIONAL);
            builder.addPin("db4", EdgeType.BIDIRECTIONAL);
            builder.addPin("db5", EdgeType.BIDIRECTIONAL);
            builder.addPin("db6", EdgeType.BIDIRECTIONAL);
            builder.addPin("db7", EdgeType.BIDIRECTIONAL);
            builder.addPin("_halt", EdgeType.INPUT);
            builder.addPin("_busak", EdgeType.INPUT);

            // See docs/InitialWarnings.txt for what each of these is
            ignoreWarnings.add(new NetNode("45"));
            ignoreWarnings.add(new NetNode("46"));
            ignoreWarnings.add(new NetNode("47"));
            ignoreWarnings.add(new NetNode("230"));
            ignoreWarnings.add(new NetNode("1061"));
            ignoreWarnings.add(new NetNode("2775"));
            ignoreWarnings.add(new NetNode("2776"));

            CircuitGraphReducer reducer = new CircuitGraphReducer(builder.getGraph(), ignoreWarnings);
            reducer.dumpStats();
            if (validate) {
                reducer.validateGraph();
            }
            reducer.dumpGraph(new File("netlist1.txt"));

            // Remove some known modules
            System.out.println("Replacing modules");
            for (Module mod : ModuleGen.getModules()) {
                reducer.replaceModule(mod);
            }
            reducer.dumpStats();
            if (validate) {
                reducer.validateGraph();
            }
            // Log the final graph
            reducer.dumpGraph(new File("netlist2.txt"));

            // Try to detect gates
            System.out.println("Combining transistors into gates");
            reducer.detectGates();
            reducer.dumpStats();
            if (validate) {
                reducer.validateGraph();
            }
            reducer.dumpGraph(new File("netlist3.txt"));

            System.out.println("List of remaining VT_FET with tree attribute set");
            for (CircuitNode cn : builder.getGraph().vertexSet()) {
                if (cn.getType() == NodeType.VT_EFET) {
                    if (cn.isTree()) {
                        System.out.println(cn);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

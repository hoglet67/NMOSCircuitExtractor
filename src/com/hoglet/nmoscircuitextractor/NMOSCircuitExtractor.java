package com.hoglet.nmoscircuitextractor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class NMOSCircuitExtractor {

    public static void main(String args[]) {
        try {

            Set<String> ignoreWarnings = new HashSet<String>();
            ignoreWarnings.add("45");
            ignoreWarnings.add("46");
            ignoreWarnings.add("47");
            ignoreWarnings.add("230");
            ignoreWarnings.add("1061");
            ignoreWarnings.add("2775");
            ignoreWarnings.add("2776");

            // Parse the Z80 into the main graph
            File transdefs = new File("transdefs.js");
            File segdefs = new File("segdefs.js");
            File nodenames = new File("nodenames.js");
            CircuitGraphBuilder builder = new CircuitGraphBuilder(ignoreWarnings);
            builder.readNetlist(transdefs, segdefs, nodenames);
            builder.buildPullupSet();
            
            // Note, in these pin definitions, the original number is no longer used
            builder.addPin("clk", EdgeType.OUTPUT, 3);
            builder.addPin("ab0", EdgeType.INPUT, 5);
            builder.addPin("ab1", EdgeType.INPUT, 6);
            builder.addPin("ab2", EdgeType.INPUT, 7);
            builder.addPin("ab3", EdgeType.INPUT, 8);
            builder.addPin("ab4", EdgeType.INPUT, 9);
            builder.addPin("ab5", EdgeType.INPUT, 10);
            builder.addPin("ab6", EdgeType.INPUT, 11);
            builder.addPin("ab7", EdgeType.INPUT, 12);
            builder.addPin("ab8", EdgeType.INPUT, 13);
            builder.addPin("ab9", EdgeType.INPUT, 14);
            builder.addPin("ab10", EdgeType.INPUT, 15);
            builder.addPin("ab11", EdgeType.INPUT, 16);
            builder.addPin("ab12", EdgeType.INPUT, 17);
            builder.addPin("ab13", EdgeType.INPUT, 18);
            builder.addPin("ab14", EdgeType.INPUT, 19);
            builder.addPin("ab15", EdgeType.INPUT, 20);
            builder.addPin("_reset", EdgeType.OUTPUT, 21);
            builder.addPin("_wait", EdgeType.OUTPUT, 22);
            builder.addPin("_int", EdgeType.OUTPUT, 23);
            builder.addPin("_nmi", EdgeType.OUTPUT, 24);
            builder.addPin("_busrq", EdgeType.OUTPUT, 25);
            builder.addPin("_m1", EdgeType.INPUT, 26);
            builder.addPin("_rd", EdgeType.INPUT, 27);
            builder.addPin("_wr", EdgeType.INPUT, 28);
            builder.addPin("_mreq", EdgeType.INPUT, 29);
            builder.addPin("_iorq", EdgeType.INPUT, 30);
            builder.addPin("_rfsh", EdgeType.INPUT, 31);
            builder.addPin("db0", EdgeType.BIDIRECTIONAL, 32);
            builder.addPin("db1", EdgeType.BIDIRECTIONAL, 33);
            builder.addPin("db2", EdgeType.BIDIRECTIONAL, 34);
            builder.addPin("db3", EdgeType.BIDIRECTIONAL, 35);
            builder.addPin("db4", EdgeType.BIDIRECTIONAL, 36);
            builder.addPin("db5", EdgeType.BIDIRECTIONAL, 37);
            builder.addPin("db6", EdgeType.BIDIRECTIONAL, 38);
            builder.addPin("db7", EdgeType.BIDIRECTIONAL, 39);
            builder.addPin("_halt", EdgeType.INPUT, 40);
            builder.addPin("_busak", EdgeType.INPUT, 41);
            builder.dumpStats();
            builder.validateGraph();
            builder.dumpGraph(new File("netlist1.txt"));

            // Remove some known modules
            System.out.println("Replacing modules");
            for (Module mod : ModuleGen.getModules()) {
                builder.replaceModule(mod);
            }
            builder.dumpStats();
            builder.validateGraph();
            // Log the final graph
            builder.dumpGraph(new File("netlist2.txt"));

            // Try to detect gates
            System.out.println("Combining transistors into gates");
            builder.detectGates();
            builder.dumpStats();
            builder.validateGraph();
            builder.dumpGraph(new File("netlist3.txt"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

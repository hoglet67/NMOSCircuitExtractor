package com.hoglet.nmoscircuitextractor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class NMOSCircuitExtractor {

    public static void main(String args[]) {
        try {

            boolean validate = true;

            Set<NetNode> ignoreWarnings = new HashSet<NetNode>();

            // Parse the Z80 into the main graph
            String net_vss = "vss";
            String net_vcc = "vcc";
            ModuleGen moduleGen = new ModuleGen(net_vss, net_vcc);

            File transdefs = new File("netlist/transdefs.js");
            File segdefs = new File("netlist/segdefs.js");
            File nodenames = new File("netlist/nodenames.js");
            CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
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
            ignoreWarnings.add(new NetNode("n45"));
            ignoreWarnings.add(new NetNode("n46"));
            ignoreWarnings.add(new NetNode("n47"));
            ignoreWarnings.add(new NetNode("n230"));
            ignoreWarnings.add(new NetNode("n1061"));
            ignoreWarnings.add(new NetNode("n2775"));
            ignoreWarnings.add(new NetNode("n2776"));

            CircuitGraphReducer reducer = new CircuitGraphReducer(builder.getGraph(), net_vss, net_vcc, ignoreWarnings);
            reducer.dumpStats();
            if (validate) {
                reducer.validateGraph(false);
            }
            reducer.dumpGraph(new File("netlist1.txt"));

            // Replace some known modules
            System.out.println("Replacing basic modules");
            for (Module mod : moduleGen.getBasicModules()) {
                reducer.replaceModule(mod);
            }
            reducer.dumpStats();
            if (validate) {
                reducer.validateGraph(false);
            }
            reducer.dumpGraph(new File("netlist2.txt"));

            // Mark symmetric modules
            Module mod = moduleGen.crossCoupledTransistors2Module();
            reducer.markModules(mod, "100"); // Mark the left node

            // Try to detect gates
            System.out.println("Combining transistors into gates");
            reducer.detectGates();

            // Remove enhancement pullups
            reducer.removeEnhancementPullups();

            reducer.dumpStats();
            if (validate) {
                reducer.validateGraph(false);
            }
            reducer.dumpGraph(new File("netlist3.txt"));

            reducer.replaceDigitalNodes();
            reducer.markDigitalNets();
            reducer.dumpStats();
            if (validate) {
                reducer.validateGraph(true);
            }
            reducer.dumpGraph(new File("netlist4.txt"));

            // Generate verilog output
            CircuitGraphWriter writer = new CircuitGraphWriter(builder.getGraph(), net_vss, net_vcc);
            writer.writeVerilog(new File("verilog/chip_z80.v"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

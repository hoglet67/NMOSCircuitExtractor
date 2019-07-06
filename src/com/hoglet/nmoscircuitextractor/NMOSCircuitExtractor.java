package com.hoglet.nmoscircuitextractor;

import java.io.File;

public class NMOSCircuitExtractor {

    public static void main(String args[]) {
        try {
            // Parse the Z80 into the main graph
            File transdefs = new File("transdefs.js");
            File segdefs = new File("segdefs.js");
            CircuitGraphBuilder builder = new CircuitGraphBuilder();
            builder.readNetlist(transdefs, segdefs);
            builder.buildPullupSet();
            builder.dumpStats();
            builder.validateGraph();

            // Remove some known modules
            System.out.println("Replacing modules");
            for (Module mod : ModuleGen.getModules()) {
                builder.replaceModule(mod);
                builder.dumpStats();
                builder.validateGraph();
            }

            // Try to detect gates
            System.out.println("Combining transistors into gates");
            builder.detectGates();
            builder.dumpStats();
            builder.validateGraph();

            // Log the final graph
            builder.dumpGraph();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

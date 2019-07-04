package com.hoglet.nmoscircuitextractor;

import java.io.File;

public class NMOSCircuitExtractor {

    public static void main(String args[]) {
        try {
            // Parse the Z80 into the main graph
            File transdefs = new File("transdefs.js");
            File segdefs = new File("segdefs.js");
            CircuitGraphBuilder graphBuilder = new CircuitGraphBuilder();
            graphBuilder.readNetlist(transdefs, segdefs);
            graphBuilder.dumpStats();

            // Remove some known modules
            graphBuilder.replaceModule(ModuleGen.registerModule());
            graphBuilder.replaceModule(ModuleGen.invertingSuperBufferModule());
            graphBuilder.replaceModule(ModuleGen.noninvertingSuperBufferModule());
            graphBuilder.dumpStats();

            // Try to detect gates
            graphBuilder.detectGates();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

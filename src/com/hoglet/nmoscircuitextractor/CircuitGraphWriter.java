package com.hoglet.nmoscircuitextractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.Graph;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;
import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class CircuitGraphWriter {

    protected Graph<CircuitNode, CircuitEdge> graph;
    protected String net_vss;
    protected String net_vcc;
    protected Set<Integer> nodeSizes;

    protected static String WTYPE = "signed [`W-1:0]";

    public CircuitGraphWriter(Graph<CircuitNode, CircuitEdge> graph, String net_vss, String net_vcc) {
        this.graph = graph;
        this.net_vss = net_vss;
        this.net_vcc = net_vcc;
        this.nodeSizes = new TreeSet<Integer>();
    }

    public void writeHeader(PrintStream ps) {
        ps.println("`include \"common.v\"");
        ps.println();
        ps.println("module chip_z80(");
        ps.println("    input eclk");
        ps.println("  , input erst");
        Map<String, String> pinMap = new TreeMap<String, String>();
        for (CircuitNode node : graph.vertexSet()) {
            if (node.getType() == NodeType.VT_PIN) {
                CircuitEdge edge = graph.outgoingEdgesOf(node).iterator().next();
                String name = "pad_" + node.getId();
                if (edge.getType() == EdgeType.INPUT) {
                    pinMap.put(name, "output");
                } else if (edge.getType() == EdgeType.OUTPUT) {
                    pinMap.put(name, "input ");
                } else {
                    pinMap.put(name + "_i", "input ");
                    pinMap.put(name + "_o", "output");
                    pinMap.put(name + "_t", "output");
                }
            }
        }
        for (Map.Entry<String, String> entry : pinMap.entrySet()) {
            ps.println("  , " + entry.getValue() + " " + entry.getKey());
        }
        ps.println(");");
        ps.println();

        ps.println("function v;   // convert an analog node value to 2-level");
        ps.println("input [`W-1:0] x;");
        ps.println("begin");
        ps.println("  v = ~x[`W-1];");
        ps.println("end");
        ps.println("endfunction");
        ps.println();
        ps.println("function [`W-1:0] a;   // convert a 2-level node value to analog");
        ps.println("input x;");
        ps.println("begin");
        ps.println("  a = x ? `HI2 : `LO2;");
        ps.println("end");
        ps.println("endfunction");
        ps.println();
    }

    public void writePadNode(PrintStream ps, CircuitNode node) {
        CircuitEdge edge = graph.outgoingEdgesOf(node).iterator().next();
        CircuitNode net = graph.getEdgeTarget(edge);
        String name = net.getId();
        switch (edge.getType()) {
        case INPUT:
            ps.println("    pad_output " + name + "_pad (pad_" + name + ", " + name + "_val);");
            break;
        case OUTPUT:
            ps.println("    pad_input " + name + "_pad (pad_" + name + ", " + name + "_val, " + name + "_port" + edge.getPort()
                    + ");");
            break;
        case BIDIRECTIONAL:
            ps.println("    pad_bidirectional " + name + "_pad (pad_" + name + "_i, pad_" + name + "_o, pad_" + name + "_t, " + name
                    + "_val, " + name + "_port" + edge.getPort() + ");");
            break;
        default:
        }
    }

    public void writeTransistorNode(PrintStream ps, TransistorNode node) {
        switch (node.getType()) {
        case VT_EFET:
            ps.print("    transistor_nmos");
            break;
        case VT_EFET_VSS:
            ps.print("    transistor_nmos_vss");
            break;
        case VT_EFET_VCC:
            ps.print("    transistor_nmos_vcc");
            break;
        default:
            throw new RuntimeException("unxpected node type" + node);
        }
        ps.print(" " + node.getId() + "(" + node.getFunction() + ",");
        Set<CircuitEdge> edges = getValidEdges(node);
        for (CircuitEdge edge : edges) {
            CircuitNode net = graph.getEdgeTarget(edge);
            ps.print(net + "_val" + ", ");
        }
        boolean first = true;
        for (CircuitEdge edge : edges) {
            if (!first) {
                ps.println(", ");
            }
            CircuitNode net = graph.getEdgeTarget(edge);
            ps.print(net + "_port" + edge.getPort());
            first = false;
        }
        ps.println(");");
    }

    private Set<CircuitEdge> getValidEdges(CircuitNode node) {
        Set<CircuitEdge> edges1 = node.getType() == NodeType.VT_NET ? graph.incomingEdgesOf(node) : graph.outgoingEdgesOf(node);
        Set<CircuitEdge> edges2 = new HashSet<CircuitEdge>();
        for (CircuitEdge edge : edges1) {
            if (edge.getType() != EdgeType.GATE && edge.getType() != EdgeType.INPUT) {
                edges2.add(edge);
            }
        }
        return edges2;
    }

    public void writeWiresAndNetNodes(PrintStream ps) {

        Set<NetNode> nets = new TreeSet<NetNode>();
        for (CircuitNode node : graph.vertexSet()) {
            if (node.getType() == NodeType.VT_NET) {
                nets.add((NetNode) node);
            }
        }

        for (NetNode net : nets) {
            String id = net.getId();
            Set<CircuitEdge> edges = getValidEdges(net);
            if (edges.size() == 0) {
                System.out.println("Skipping driverless net: " + net);
                continue;
            }
            for (int i = 0; i < edges.size(); i++) {
                ps.println("    wire " + WTYPE + " " + id + "_port" + i + ";");
            }
            ps.println("    wire " + WTYPE + " " + id + "_val;");
            ps.println("    wire " + id + " = v(" + id + "_val);");
        }
        ps.println();

        // net_node_2 id_node (eclk, erst, id_port_0, id_port_1, ... id_val);

        for (NetNode net : nets) {
            Set<CircuitEdge> edges = getValidEdges(net);
            if (edges.size() == 0) {
                System.out.println("Skipping driverless net: " + net);
                continue;
            }
            ps.print("    net_node_" + edges.size() + "_init" + (net.isMark() ? "1" : "0") + " " + net + "_node (eclk, erst, ");
            int portnum = 0;
            for (CircuitEdge edge : edges) {
                edge.setPort(portnum);
                ps.print(net.getId() + "_port" + portnum + ", ");
                portnum++;
            }
            ps.println(net.getId() + "_val);");
            nodeSizes.add(portnum);
        }
    }

    public void writePullupNode(PrintStream ps, CircuitNode node) {
        CircuitEdge edge = graph.outgoingEdgesOf(node).iterator().next();
        CircuitNode net = graph.getEdgeTarget(edge);
        if (node.isMark()) {
            ps.print("    transistor_pullup_strong " + node.getId() + " (");
        } else {
            ps.print("    transistor_pullup " + node.getId() + " (");
        }
        ps.print(net + "_val" + ", ");
        ps.print(net + "_port" + edge.getPort());
        ps.println(");");
        // ps.println(" assign " + net + "_port" + edge.getPort() + " = 2'b10;
        // // pullup " + net);
    }

    public void writeDeviceNodes(PrintStream ps) {
        for (CircuitNode node : graph.vertexSet()) {
            switch (node.getType()) {
            case VT_NET:
                // do nothing
                break;
            case VT_PIN:
                writePadNode(ps, node);
                break;
            case VT_EFET:
            case VT_EFET_VSS:
            case VT_EFET_VCC:
                writeTransistorNode(ps, (TransistorNode) node);
                break;
            case VT_DPULLUP:
            case VT_EPULLUP:
                writePullupNode(ps, node);
                break;
            default:
                throw new RuntimeException("Not implemented yet");
            }
        }
        ps.println();
    }

    public void writeModulesDave(PrintStream ps) {
        for (int n : nodeSizes) {

            ps.print("module net_node_" + n + "(input eclk, input erst, input " + WTYPE + " ");
            for (int i = 0; i < n; i++) {
                ps.print("i" + i + ", ");
            }
            ps.println("output reg " + WTYPE + " out);");
            ps.print("wire sel = (|i0)");
            for (int i = 1; i < n; i++) {
                ps.print(" | (|i" + i + ")");
            }
            ps.println(";");
            ps.print("wire val = ~(i0[0]");
            for (int i = 1; i < n; i++) {
                ps.print(" | i" + i + "[0]");
            }
            ps.println(");");
            ps.println("    always @(posedge eclk) begin");
            ps.println("        if (erst) begin");
            ps.println("            out <= 2'b01;");
            ps.println("        end else if (sel) begin");
            ps.println("            out <= { val, ~val};");
            ps.println("        end");
            ps.println("    end");
            ps.println("endmodule");

        }
        ps.println();
    }

    // module spice_node_2(input eclk,ereset, input signed [`W-1:0] i0,i1,
    // output reg signed [`W-1:0] v);
    // wire signed [`W-1:0] i = i0+i1;
    //
    // always @(posedge eclk)
    // if (ereset)
    // v <= 0;
    // else
    // v <= v + i;
    //
    // endmodule

    public void writeModulesPeter(PrintStream ps) {
        for (int init = 0; init < 2; init++) {
            for (int n : nodeSizes) {
                ps.print("module net_node_" + n + "_init" + init + "(input eclk, input erst, input " + WTYPE + " ");
                for (int i = 0; i < n; i++) {
                    ps.print("i" + i + ", ");
                }
                ps.println("output reg " + WTYPE + " v);");
                ps.print("wire " + WTYPE + " i = i0");
                for (int i = 1; i < n; i++) {
                    ps.print("+i" + i);
                }
                ps.println(";");
                ps.println("    always @(posedge eclk)");
                ps.println("        if (erst)");
                if (init == 0) {
                    ps.println("            v <= `LO2;");
                } else {
                    ps.println("            v <= `HI2;");
                }
                ps.println("        else");
                ps.println("            v <= v + i;");
                ps.println("endmodule");
            }
            ps.println();
        }
    }

    public void writeFooter(PrintStream ps) {
        ps.println("endmodule");
        ps.println();
    }

    public void writeVerilog(PrintStream ps) {
        System.out.println("Writing verilog...");
        writeHeader(ps);
        writeWiresAndNetNodes(ps);
        writeDeviceNodes(ps);
        writeFooter(ps);
        writeModulesPeter(ps);
        System.out.println("Writing verilog done");
    }

    public void writeVerilog(File file) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            writeVerilog(ps);
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

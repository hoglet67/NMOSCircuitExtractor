package com.hoglet.nmoscircuitextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.IsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;
import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class CircuitGraphBuilder {
    protected String net_vss = "vss";
    protected String net_vcc = "vcc";

    protected Map<String, NetNode> netMap = new HashMap<String, NetNode>();
    protected Set<String> pullupSet = new TreeSet<String>();
    protected Graph<CircuitNode, CircuitEdge> graph;

    protected Set<String> duplicate_map = new HashSet<String>();
    protected Set<String> ignoreWarnings = new HashSet<String>();

    public CircuitGraphBuilder(Set<String> ignoreWarnings) {
        this();
        this.ignoreWarnings.addAll(ignoreWarnings);
    }

    public CircuitGraphBuilder() {
        graph = new DefaultDirectedGraph<CircuitNode, CircuitEdge>(CircuitEdge.class);
    }

    private NetNode addNet(String net) {
        if (net == null) {
            throw new RuntimeException("Oops, tried to add null net");
        }
        if (net.equals(net_vss)) {
            throw new RuntimeException("Oops, tried to add VSS net");
        }
        if (net.equals(net_vcc)) {
            throw new RuntimeException("Oops, tried to add VCC net");
        }
        NetNode netNode = netMap.get(net);
        if (netNode == null) {
            netNode = new NetNode(net);
            graph.addVertex(netNode);
            netMap.put(net, netNode);
        }
        netNode.incDegree();
        return netNode;
    }

    private NetNode getNet(String net) {
        NetNode netNode = netMap.get(net);
        if (netNode == null) {
            throw new RuntimeException("Failed to lookup net " + net);
        }
        return netNode;
    }

    private String getHash(TransistorNode tr, String gate, String c1, String c2) {
        return tr.getType() + "_" + gate + "_" + c1 + "_" + c2;
    }

    public NetNode addExternal(Integer ext) {
        return addExternal("" + ext);
    }

    public NetNode addExternal(String ext) {
        NetNode net = addNet(ext);
        net.setAsExternal();
        return net;
    }

    public TransistorNode addPullup(String id, Integer net) {
        return addPullup(id, "" + net);
    }

    public TransistorNode addPullup(String id, String net) {
        TransistorNode tr = new TransistorNode(NodeType.VT_DPULLUP, id);
        String hash = getHash(tr, net, null, null);
        if (duplicate_map.contains(hash)) {
            System.out.println("Skipping duplicate transistor: " + tr.getId());
            return null;
        }
        duplicate_map.add(hash);
        graph.addVertex(tr);
        CircuitNode netNode = addNet(net);
        graph.addEdge(tr, netNode).setType(EdgeType.CHANNEL);
        return tr;
    }

    public void addPin(String name, EdgeType type, Integer net) {
        addPin(name, type, name);
    }

    public void addPin(String name, EdgeType type, String net) {
        PinNode pinNode = new PinNode(name);
        NetNode netNode = getNet(net);
        graph.addVertex(pinNode);
        graph.addEdge(pinNode, netNode).setType(type);
    }

    public TransistorNode addTransistor(String id, Integer gateNet, Integer channel1Net, Integer channel2Net) {
        return addTransistor(id, "" + gateNet, "" + channel1Net, "" + channel2Net);
    }

    public TransistorNode addTransistor(String id, Integer gateNet, Integer channel1Net, String channel2Net) {
        return addTransistor(id, "" + gateNet, "" + channel1Net, channel2Net);
    }

    public TransistorNode addTransistor(String id, String gateNet, String channel1Net, String channel2Net) {
        TransistorNode tr;

        // Known trap transistor (removed from netlist)
        if (gateNet.equals(net_vss) && channel1Net.equals(net_vss) && channel2Net.equals(net_vss)) {
            System.out.println("Skipping transistor (g=vss, c1=vss, c2=vss): " + id);
            return null;
        }

        if (gateNet.equals(net_vcc) && channel1Net.equals(net_vcc) && channel2Net.equals(net_vcc)) {
            System.out.println("Skipping transistor (g=vcc, c1=vcc, c2=vcc): " + id);
            return null;
        }

        if (channel1Net.equals(net_vss) && channel2Net.equals(net_vss)) {
            System.out.println("Skipping transistor (c1=vss, c2=vss): " + id);
            return null;
        }

        if (channel1Net.equals(net_vcc) && channel2Net.equals(net_vcc)) {
            System.out.println("Skipping transistor (c1=vcc, c2=vcc): " + id);
            return null;
        }

        String gate = null;
        String c1 = null;
        String c2 = null;

        // Enhancement Pullup
        if (gateNet.equals(net_vcc) && channel1Net.equals(net_vcc)) {

            tr = new TransistorNode(NodeType.VT_EPULLUP, id);
            c1 = channel2Net;

        } else if (gateNet.equals(net_vcc) && channel2Net.equals(net_vcc)) {

            tr = new TransistorNode(NodeType.VT_EPULLUP, id);
            c1 = channel1Net;

        } else {

            if (gateNet.equals(net_vss)) {
                System.out.println("Skipping transistor (g=vss): " + id);
                return null;
            }

            if (gateNet.equals(net_vcc)) {
                System.out.println("Skipping transistor (g=vcc): " + id);
                return null;
            }

            if (gateNet.equals(channel1Net)) {
                System.out.println("Skipping transistor (g=c1): " + id);
                return null;
            }

            if (gateNet.equals(channel2Net)) {
                System.out.println("Skipping transistor (g=c2): " + id);
                return null;
            }
            if (channel1Net.equals(channel2Net)) {
                System.out.println("Skipping transistor (c1=c2): " + id);
                return null;
            }

            gate = gateNet;
            if (channel1Net.equals(net_vss)) {
                tr = new TransistorNode(NodeType.VT_EFET_VSS, id);
                c1 = channel2Net;
            } else if (channel2Net.equals(net_vss)) {
                tr = new TransistorNode(NodeType.VT_EFET_VSS, id);
                c1 = channel1Net;
            } else if (channel1Net.equals(net_vcc)) {
                tr = new TransistorNode(NodeType.VT_EFET_VCC, id);
                c1 = channel2Net;
            } else if (channel2Net.equals(net_vcc)) {
                tr = new TransistorNode(NodeType.VT_EFET_VCC, id);
                c1 = channel1Net;
            } else {
                tr = new TransistorNode(NodeType.VT_EFET, id);
                c1 = channel1Net.compareTo(channel2Net) <= 0 ? channel1Net : channel2Net;
                c2 = channel1Net.compareTo(channel2Net) <= 0 ? channel2Net : channel1Net;
            }
        }
        String hash = getHash(tr, gate, c1, c2);
        if (duplicate_map.contains(hash)) {
            System.out.println("Skipping duplicate transistor: " + tr.getId());
            return null;
        }
        duplicate_map.add(hash);

        graph.addVertex(tr);
        if (gate != null) {
            CircuitNode netNode = addNet(gate);
            graph.addEdge(tr, netNode).setType(EdgeType.GATE);
        }
        if (c1 != null) {
            CircuitNode netNode = addNet(c1);
            graph.addEdge(tr, netNode).setType(EdgeType.CHANNEL);
        }
        if (c2 != null) {
            CircuitNode netNode = addNet(c2);
            graph.addEdge(tr, netNode).setType(EdgeType.CHANNEL);
        }
        tr.setFunction("" + gate);

        return tr;
    }

    public Graph<CircuitNode, CircuitEdge> readNetlist(File transdefs, File segdefs, File nodenames) throws IOException {
        String line;

        Map<String, String> nameMap = new HashMap<String, String>();

        BufferedReader namesReader = new BufferedReader(new FileReader(nodenames));
        while ((line = namesReader.readLine()) != null) {
            // vss: 1,
            String parts[] = line.split("[,:]");
            if (parts.length == 2) {
                String netName = parts[0].trim();
                String netNum = parts[1].trim();
                if (!nameMap.containsKey(netNum)) {
                    nameMap.put(netNum, netName);
                    System.out.println(netNum + " => " + netName);
                } else {
                    System.out.println("Ignoring duplicate mapping of " + netNum + " as " + netName);
                }
            }
        }
        namesReader.close();

        // Each line in transdefs represents a transistor

        BufferedReader transReader = new BufferedReader(new FileReader(transdefs));
        while ((line = transReader.readLine()) != null) {
            // ['t251',1,1,1,[4216,4221,4058,4085],[1,1,1,1,135],false,],
            if (line.startsWith("[")) {
                String parts[] = line.split(",");

                String tr = parts[0].replace("'", "").replace("[", "");
                String gateNet = parts[1].trim();
                String channel1Net = parts[2].trim();
                String channel2Net = parts[3].trim();

                if (nameMap.containsKey(gateNet)) {
                    gateNet = nameMap.get(gateNet);
                }
                if (nameMap.containsKey(channel1Net)) {
                    channel1Net = nameMap.get(channel1Net);
                }
                if (nameMap.containsKey(channel2Net)) {
                    channel2Net = nameMap.get(channel2Net);
                }

                addTransistor(tr, gateNet, channel1Net, channel2Net);

            }
        }
        transReader.close();

        // Each line in segdefs represents a possible pullup

        Set<String> pullupList = new TreeSet<String>();
        BufferedReader segReader = new BufferedReader(new FileReader(segdefs));
        while ((line = segReader.readLine()) != null) {
            // [52,'+',0,3872,4814,3925,4814,3925,4737,3941,4737,3941,4727,3917,4727,3917,4805,3872,4805],
            if (line.startsWith("[")) {
                String parts[] = line.split(",");
                if (parts[1].contains("+")) {
                    String net = parts[0].replace("[", "").trim();
                    if (nameMap.containsKey(net)) {
                        net = nameMap.get(net);
                    }
                    pullupList.add(net);
                }
            }
        }
        segReader.close();
        for (String net : pullupList) {
            if (!netMap.containsKey(net)) {
                System.out.println("Warning: pullup on unused net " + net);
            }
            addPullup("pullup" + net, net);
        }

        return graph;
    }

    public Graph<CircuitNode, CircuitEdge> getGraph() {
        return graph;
    }

    public void dumpStats() {
        int[] stats = new int[NodeType.VT_NUM_TYPES.ordinal()];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = 0;
        }
        for (CircuitNode cn : graph.vertexSet()) {
            stats[cn.getType().ordinal()]++;
        }
        for (int i = 0; i < stats.length; i++) {
            System.out.println(NodeType.values()[i].name() + "  = " + stats[i]);
        }
    }

    public void dumpGraph(File file) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            for (NodeType type : NodeType.values()) {
                if (type == NodeType.VT_NET) {
                    continue;
                }
                for (CircuitNode node : graph.vertexSet()) {
                    if (node.getType() == type) {
                        dumpNode(ps, node);
                    }
                }
            }
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void validateGraph() {
        int count = 0;
        Set<String> nets = new TreeSet<String>();
        for (CircuitNode node : graph.vertexSet()) {
            if (node.getType() == NodeType.VT_NET) {
                String net = node.getId();
                if (!ignoreWarnings.contains(net)) {
                    nets.add(net);
                }
            }
        }
        for (String net : nets) {
            CircuitNode node = netMap.get(net);
            Set<CircuitEdge> edges = graph.incomingEdgesOf(node);
            int n_channel = 0;
            int n_output = 0;
            int n_bidirectional = 0;
            int fanout = edges.size();
            for (CircuitEdge edge : edges) {
                switch (edge.getType()) {
                case CHANNEL:
                    n_channel++;
                    break;
                case OUTPUT:
                    n_output++;
                    break;
                case BIDIRECTIONAL:
                    n_bidirectional++;
                    break;
                default:
                }
            }
            if (edges.size() == 0) {
                System.out.println("Warning: net  " + node.getId() + " has no connections");
                count++;
            } else if (edges.size() == 1) {
                CircuitEdge edge = edges.iterator().next();
                CircuitNode target = graph.getEdgeSource(edge);
                System.out.println("Warning: net  " + node.getId() + " has only one connection [to " + target + " "
                        + edge.getType().name() + "]");
                count++;
            }
            if (n_channel + n_output + n_bidirectional == 0) {
                System.out.println("Warning: net  " + node.getId() + " (fanout = " + fanout + ") has no drivers");
                count++;
            } else if (n_output > 1) {
                System.out.println("Warning: net  " + node.getId() + " (fanout = " + fanout + ") has multiple output drivers ("
                        + n_output + ")");
                count++;
            }
        }
        System.out.println("Validation: " + count + " warnings");
    }

    public void buildPullupSet() {
        System.out.println("Building pullup set");
        for (CircuitNode node : graph.vertexSet()) {
            if (node.getType() == NodeType.VT_NET) {
                for (CircuitEdge edge : graph.incomingEdgesOf(node)) {
                    CircuitNode source = graph.getEdgeSource(edge);
                    if (source.getType() == NodeType.VT_DPULLUP || source.getType() == NodeType.VT_EPULLUP
                            || source.getType() == NodeType.VT_EFET_VCC) {
                        pullupSet.add(node.getId());
                    }
                }
            }
        }
        // for (String id : pullupSet) {
        // System.out.println(id);
        // }
        System.out.println("Building pullup set done; size = " + pullupSet.size());
    }

    // Returns the set of components connected to the net
    private Set<CircuitNode> getNeighbours(NetNode net) {
        HashSet<CircuitNode> result = new HashSet<CircuitNode>();
        for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
            CircuitNode node = graph.getEdgeSource(edge);
            if (node.getType() == NodeType.VT_NET || node.getType() == NodeType.VT_NET_EXT) {
                throw new RuntimeException("Currupt graph");
            }
            result.add(node);
        }
        return result;
    }

    private static boolean sameNet(String n1, String n2) {
        if (n1 != null && n2 != null) {
            return n1.equals(n2);
        }
        return n1 == null && n2 == null;
    }

    private Set<String> getConnections(CircuitNode tn, EdgeType type) {
        Set<String> nets = new HashSet<String>();
        for (CircuitEdge edge : graph.outgoingEdgesOf(tn)) {
            if (edge.getType() == type) {
                CircuitNode target = graph.getEdgeTarget(edge);
                if (target.getType() != NodeType.VT_NET) {
                    throw new RuntimeException("Corrupt graph");
                }
                nets.add(target.getId());
            }
        }
        return nets;
    }

    private String getC1(TransistorNode tn) {
        Set<String> nets = getConnections(tn, EdgeType.CHANNEL);
        if (tn.getType() == NodeType.VT_EFET) {
            if (nets.size() == 2) {
                Iterator<String> it = nets.iterator();
                String c1 = it.next();
                String c2 = it.next();
                return c1.compareTo(c2) < 0 ? c1 : c2;
            }
        } else if (tn.getType() == NodeType.VT_EFET_VSS) {
            if (nets.size() == 1) {
                return nets.iterator().next();
            }
        }
        throw new RuntimeException("Corrupt graph:" + tn + "; nets = " + nets);
    }

    private String getC2(TransistorNode tn) {
        Set<String> nets = getConnections(tn, EdgeType.CHANNEL);
        if (tn.getType() == NodeType.VT_EFET) {
            if (nets.size() == 2) {
                Iterator<String> it = nets.iterator();
                String c1 = it.next();
                String c2 = it.next();
                return c1.compareTo(c2) < 0 ? c2 : c1;
            }
        } else if (tn.getType() == NodeType.VT_EFET_VSS) {
            if (nets.size() == 1) {
                return null; // TODO: Would really like to return NET_VSS here
            }
        }
        throw new RuntimeException("Corrupt graph:" + tn + "; nets =" + nets);
    }

    private Set<TransistorNode> findParallelTransistors(String n1, String n2) {
        HashSet<TransistorNode> result = new HashSet<TransistorNode>();
        String n = n1 != null ? n1 : n2;
        for (CircuitNode cn : getNeighbours(getNet(n))) {
            if (cn.isCombinable()) {
                TransistorNode tn = (TransistorNode) cn;
                if (sameNet(getC1(tn), n1) && sameNet(getC2(tn), n2)) {
                    result.add(tn);
                } else if (sameNet(getC1(tn), n2) && sameNet(getC2(tn), n1)) {
                    result.add(tn);
                }
            }
        }
        return result;
    }

    private TransistorNode findSeriesTransistor(TransistorNode tn, String n1) {
        Set<CircuitNode> ts = getNeighbours(getNet(n1));
        if (ts.size() == 2) {
            if (!ts.remove(tn)) {
                throw new RuntimeException("findSeriesTransistors: set did not contain " + tn.getId());
            }
            CircuitNode cn = ts.iterator().next();
            if (cn.isCombinable()) {
                TransistorNode z = (TransistorNode) cn;
                if (n1.equals(getC1(z)) || n1.equals(getC2(z))) {
                    return z;
                }
            }
        }
        return null;
    }

    private void dumpConnections(PrintStream ps, String message, Set<String> nets) {
        if (nets.size() == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(message);
        sb.append(": [");
        boolean first = true;
        for (String net : nets) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("" + net);
            if (!pullupSet.contains("" + net)) {
                sb.append('#');
            }
            first = false;
        }
        sb.append(']');
        ps.println(sb.toString());
    }

    private void dumpNode(PrintStream ps, CircuitNode tn) {
        ps.println(tn);
        dumpConnections(ps, "             gate", getConnections(tn, EdgeType.GATE));
        dumpConnections(ps, "          channel", getConnections(tn, EdgeType.CHANNEL));
        dumpConnections(ps, "            input", getConnections(tn, EdgeType.INPUT));
        dumpConnections(ps, "           output", getConnections(tn, EdgeType.OUTPUT));
        dumpConnections(ps, "    bidirectional", getConnections(tn, EdgeType.BIDIRECTIONAL));
        dumpConnections(ps, "      unspecified", getConnections(tn, EdgeType.UNSPECIFIED));
        if (tn.getType() == NodeType.VT_EFET || tn.getType() == NodeType.VT_EFET_VSS) {
            ps.println("               fn: " + ((TransistorNode) tn).getFunction());
        }
    }

    public void copyGateEdges(CircuitNode t1, CircuitNode t2) {
        for (String gate : getConnections(t2, EdgeType.GATE)) {
            CircuitEdge edge = graph.addEdge(t1, getNet(gate));
            if (edge != null) {
                edge.setType(EdgeType.GATE);
            }
        }
    }

    public void detectGates() {
        boolean done;
        do {
            done = true;
            // Merge Parallel Transistors
            for (CircuitNode node : graph.vertexSet()) {
                if (node.isCombinable()) {
                    TransistorNode t1 = (TransistorNode) node;
                    Set<TransistorNode> parallel = findParallelTransistors(getC1(t1), getC2(t1));
                    if (!parallel.remove(t1)) {
                        throw new RuntimeException("findParallelTransistors: set did not contain " + t1.getId());
                    }
                    if (!parallel.isEmpty()) {
                        StringBuffer f = new StringBuffer();
                        f.append("(");
                        f.append(t1.getFunction());
                        for (TransistorNode t2 : parallel) {
                            // System.out.println("P Merging " + t2.getId() + "
                            // into " + t1.getId());
                            f.append(" OR ");
                            f.append(t2.getFunction());
                            // Move T2 gate connections to T1
                            copyGateEdges(t1, t2);
                            graph.removeVertex(t2);
                        }
                        f.append(")");
                        t1.setFunction(f.toString());
                        done = false;
                        break; // Avoids a concurrent modification exception at
                               // the expense of some efficiency
                    }
                }
            }
            // Merge Series Transistors
            for (CircuitNode node : graph.vertexSet()) {
                if (node.isCombinable()) {
                    String inner = null;
                    TransistorNode t1 = (TransistorNode) node;
                    TransistorNode t2 = null;
                    String c1 = getC1(t1);
                    String c2 = getC2(t1);
                    if (t2 == null && c1 != null) {
                        inner = c1;
                        t2 = findSeriesTransistor(t1, inner);
                    }
                    if (t2 == null && c2 != null) {
                        inner = c2;
                        t2 = findSeriesTransistor(t1, inner);
                    }
                    if (t2 == null) {
                        continue;
                    }
                    // System.out.println("S Merging " + t2.getId() + " into " +
                    // t1.getId());
                    StringBuffer f = new StringBuffer();
                    f.append("(");
                    f.append(t1.getFunction());
                    f.append(" AND ");
                    f.append(t2.getFunction());
                    f.append(")");
                    t1.setFunction(f.toString());
                    // Update connections
                    String other = null;
                    String t2c1 = getC1(t2);
                    String t2c2 = getC2(t2);
                    if (inner.equals(t2c1) && !inner.equals(t2c2)) {
                        other = t2c2;
                    } else if (inner.equals(t2c2) && !inner.equals(t2c1)) {
                        other = t2c1;
                    } else {
                        throw new RuntimeException("t2 is not connected to inner");
                    }
                    if (other != null) {
                        CircuitNode otherNet = getNet(other);
                        graph.addEdge(t1, otherNet).setType(EdgeType.CHANNEL);
                    }
                    // Move T2 gate connections to T1
                    copyGateEdges(t1, t2);
                    if (t1.getType() == NodeType.VT_EFET_VSS || t2.getType() == NodeType.VT_EFET_VSS) {
                        t1.setType(NodeType.VT_EFET_VSS);
                    }
                    NetNode innerNet = getNet(inner);
                    graph.removeVertex(t2);
                    graph.removeVertex(innerNet);
                    done = false;
                    break; // Avoids a concurrent modification exception at
                           // the expense of some efficiency
                }
            }
        } while (!done);
    }

    public void replaceModule(Module mod) {
        // Look for instances of the subgraph in the main graph
        IsomorphismInspector<CircuitNode, CircuitEdge> inspector = new VF2SubgraphIsomorphismInspector<CircuitNode, CircuitEdge>(
                graph, mod.getGraph(), new CircuitNodeComparator(), new CircuitEdgeCompator());
        Iterator<GraphMapping<CircuitNode, CircuitEdge>> it = inspector.getMappings();
        int count = 0;
        Map<ModuleNode, List<ModulePort>> toAdd = new HashMap<ModuleNode, List<ModulePort>>();
        while (it.hasNext()) {
            GraphMapping<CircuitNode, CircuitEdge> mapping = it.next();

            // Remove the transistors etc
            for (CircuitNode subcn : mod.getGraph().vertexSet()) {
                if (subcn.getType() != NodeType.VT_NET_EXT) {
                    CircuitNode cn = mapping.getVertexCorrespondence(subcn, false);
                    graph.removeVertex(cn);
                }
            }

            // Add a module
            count++;
            ModuleNode modNode = new ModuleNode(mod.getName() + count);
            // System.out.println(modNode.getId());
            List<ModulePort> ports = new LinkedList<ModulePort>();
            for (ModulePort subp : mod.getPorts()) {
                CircuitNode netNode = mapping.getVertexCorrespondence(subp.getNet(), false);
                ports.add(new ModulePort(subp.getType(), (NetNode) netNode));
            }
            toAdd.put(modNode, ports);

        }
        for (Entry<ModuleNode, List<ModulePort>> entry : toAdd.entrySet()) {
            ModuleNode modNode = entry.getKey();
            graph.addVertex(modNode);
            for (ModulePort modPort : entry.getValue()) {
                graph.addEdge(modNode, modPort.getNet()).setType(modPort.getType());
            }
        }
        System.out.println(mod.getName() + ": replaced " + count + " instances");
    }
}

package com.hoglet.nmoscircuitextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
    protected int net_vss = 1;
    protected int net_vcc = 2;
    protected Map<Integer, NetNode> netMap = new HashMap<Integer, NetNode>();
    protected Set<String> pullupSet = new TreeSet<String>();
    protected Graph<CircuitNode, CircuitEdge> graph;

    
    protected Set<String> duplicate_map = new HashSet<String>();
    
    public CircuitGraphBuilder() {
        clearNetMap();
        graph = new DefaultDirectedGraph<CircuitNode, CircuitEdge>(CircuitEdge.class);
    }

    private void clearNetMap() {
        netMap.clear();
    }

    private NetNode addNet(Integer net) {
        if (net == null) {
            throw new RuntimeException("Oops, tried to add null net");
        }
        if (net == net_vss) {
            throw new RuntimeException("Oops, tried to add VSS net");
        }
        if (net == net_vcc) {
            throw new RuntimeException("Oops, tried to add VCC net");
        }
        NetNode netNode = netMap.get(net);
        if (netNode == null) {
            netNode = new NetNode("" + net);
            graph.addVertex(netNode);
            netMap.put(net, netNode);
        }
        netNode.incDegree();
        return netNode;
    }

    private String getHash(TransistorNode tr) {
        return tr.getType() + "_" + tr.getGate() + "_" + tr.getC1() + "_" + tr.getC2();
    }
    
    private void addTransistor(TransistorNode tr) {
        String hash = getHash(tr);        
        if (duplicate_map.contains(hash)) {
            System.out.println("Skipping duplicate transistor: " + tr.getId());
            return;
        }
        duplicate_map.add(hash);
        graph.addVertex(tr);
        if (tr.getGate() != null) {
            CircuitNode netNode = addNet(tr.getGate());
            graph.addEdge(tr, netNode).setGate();
        }
        if (tr.getC1() != null) {
            CircuitNode netNode = addNet(tr.getC1());
            graph.addEdge(tr, netNode).setChannel();
        }
        if (tr.getC2() != null) {
            CircuitNode netNode = addNet(tr.getC2());
            graph.addEdge(tr, netNode).setChannel();
        }
    }

    public NetNode addExternal(int ext) {
        NetNode net = addNet(ext);
        net.setAsExternal();
        return net;
    }

    public TransistorNode addPullup(String id, int net) {
        TransistorNode tr = new TransistorNode(NodeType.VT_DPULLUP, id, null, net, null);
        addTransistor(tr);
        return tr;
    }

    public TransistorNode addTransistor(String id, int gateNet, int channel1Net, int channel2Net) {
        TransistorNode tr;

        // Known trap transistor (removed from netlist)
        if (gateNet == net_vss && channel1Net == net_vss && channel2Net == net_vss) {
            System.out.println("Skipping transistor (g=vss, c1=vss, c2=vss): " + id);
            return null;
        }

        if (gateNet == net_vcc && channel1Net == net_vcc && channel2Net == net_vcc) {
            System.out.println("Skipping transistor (g=vcc, c1=vcc, c2=vcc): " + id);
            return null;
        }

        if (channel1Net == net_vss && channel2Net == net_vss) {
            System.out.println("Skipping transistor (c1=vss, c2=vss): " + id);
            return null;
        }

        if (channel1Net == net_vcc && channel2Net == net_vcc) {
            System.out.println("Skipping transistor (c1=vcc, c2=vcc): " + id);
            return null;
        }

        // Enhancement Pullup
        if (gateNet == net_vcc && channel1Net == net_vcc) {

            tr = new TransistorNode(NodeType.VT_EPULLUP, id, null, channel2Net, null);

        } else if (gateNet == net_vcc && channel2Net == net_vcc) {

            tr = new TransistorNode(NodeType.VT_EPULLUP, id, null, channel1Net, null);

        } else {

            if (gateNet == net_vss) {
                System.out.println("Skipping transistor (g=vss): " + id);
                return null;
            }

            if (gateNet == net_vcc) {
                System.out.println("Skipping transistor (g=vcc): " + id);
                return null;
            }

            if (gateNet == channel1Net) {
                System.out.println("Skipping transistor (g=c1): " + id);
                return null;
            }

            if (gateNet == channel2Net) {
                System.out.println("Skipping transistor (g=c2): " + id);
                return null;
            }
            if (channel1Net == channel2Net) {
                System.out.println("Skipping transistor (c1=c2): " + id);
                return null;
            }

            if (channel1Net == net_vss) {
                tr = new TransistorNode(NodeType.VT_EFET_VSS, id, gateNet, channel2Net, null);
            } else if (channel2Net == net_vss) {
                tr = new TransistorNode(NodeType.VT_EFET_VSS, id, gateNet, channel1Net, null);
            } else if (channel1Net == net_vcc) {
                tr = new TransistorNode(NodeType.VT_EFET_VCC, id, gateNet, channel2Net, null);
            } else if (channel2Net == net_vcc) {
                tr = new TransistorNode(NodeType.VT_EFET_VCC, id, gateNet, channel1Net, null);
            } else {
                tr = new TransistorNode(NodeType.VT_EFET, id, gateNet, channel1Net, channel2Net);
            }
        }
        addTransistor(tr);
        return tr;
    }

    public Graph<CircuitNode, CircuitEdge> readNetlist(File transdefs, File segdefs) throws IOException {
        String line;

        // Each line in transdefs represents a transistor

        BufferedReader transReader = new BufferedReader(new FileReader(transdefs));
        while ((line = transReader.readLine()) != null) {
            // ['t251',1,1,1,[4216,4221,4058,4085],[1,1,1,1,135],false,],
            if (line.startsWith("[")) {
                String parts[] = line.split(",");

                String tr = parts[0].replace("'", "").replace("[", "");
                int gateNet = Integer.parseInt(parts[1]);
                int channel1Net = Integer.parseInt(parts[2]);
                int channel2Net = Integer.parseInt(parts[3]);

                addTransistor(tr, gateNet, channel1Net, channel2Net);

            }
        }
        transReader.close();

        // Each line in segdefs represents a possible pullup

        Set<Integer> pullupList = new TreeSet<Integer>();
        BufferedReader segReader = new BufferedReader(new FileReader(segdefs));
        while ((line = segReader.readLine()) != null) {
            // [52,'+',0,3872,4814,3925,4814,3925,4737,3941,4737,3941,4727,3917,4727,3917,4805,3872,4805],
            if (line.startsWith("[")) {
                String parts[] = line.split(",");
                if (parts[1].contains("+")) {
                    Integer net = Integer.parseInt(parts[0].replace("[", "").trim());
                    pullupList.add(net);
                }
            }
        }
        segReader.close();
        for (Integer net : pullupList) {
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

    public CircuitNode followOutgoingEdge(CircuitNode node, EdgeType type, CircuitEdge skipping) {
        // System.out.println("skipping = " + skipping);
        for (CircuitEdge edge : graph.outgoingEdgesOf(node)) {
            // System.out.println("edge = " + edge);

            if (skipping != null && edge.equals(skipping)) {
                // System.out.println("Skipped!!");
                continue;
            }
            if (edge.getType() == type) {
                CircuitNode target = graph.getEdgeTarget(edge);
                // System.out.println("Target = " + target.getId());
                return target;
            }
        }
        throw new RuntimeException("Edge of type " + type.name() + " not found from node " + node.getId());
    }

    public void traverseOrNet(StringBuffer sb, CircuitNode net, boolean root, CircuitNode skipping, Set<String> visited,
            boolean debug) {
        if (net.getType() != NodeType.VT_NET) {
            throw new RuntimeException("Node " + net.getId() + " is not a net");
        }
        if (debug) {
            System.out.println("net = " + net.getId());
        }
        if (visited.contains(net.getId())) {
            return;
        }
        visited.add(net.getId());
        CircuitNode gateNet;
        CircuitNode sourceNet;
        sb.append("(");
        boolean first = true;
        for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
            if (!root && edge.getType() == EdgeType.GATE) {
                // System.out.println("Unexpected gate connected to net " +
                // net.getId());
                sb.append("#gate#" + net.getId() + "#");
            } else if (edge.getType() == EdgeType.CHANNEL) {
                CircuitNode tr = graph.getEdgeSource(edge);
                if (skipping != null & tr.equals(skipping)) {
                    continue;
                }
                if (debug) {
                    System.out.println("TR: " + tr.getId() + " type " + tr.getType().name());
                }
                switch (tr.getType()) {
                case VT_EFET:
                    gateNet = followOutgoingEdge(tr, EdgeType.GATE, null);
                    sourceNet = followOutgoingEdge(tr, EdgeType.CHANNEL, edge);
                    if (visited.contains(sourceNet.getId())) {
                        continue;
                    }
                    if (!first) {
                        sb.append(" OR ");
                    }
                    first = false;
                    sb.append("[");
                    sb.append(gateNet.getId());
                    sb.append(" AND ");
                    if (pullupSet.contains(sourceNet.getId())) {
                        sb.append(sourceNet.getId());
                    } else {
                        traverseOrNet(sb, sourceNet, false, tr, visited, debug);
                    }
                    sb.append("]");
                    break;
                case VT_EFET_VSS:
                    if (!first) {
                        sb.append(" OR ");
                    }
                    first = false;
                    gateNet = followOutgoingEdge(tr, EdgeType.GATE, null);
                    sb.append(gateNet.getId());
                    break;
                case VT_DPULLUP:
                    if (root) {
                        continue;
                    }
                default:
                    // sb.append("?" + tr.getType() + "?" + net.getId() + "?");
                    throw new RuntimeException("Unexpected " + tr.getType() + " connected to net " + net.getId());
                }

            }
        }
        sb.append(")");
        visited.remove(net.getId());
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

    public void detectGatesOld() {
        for (CircuitNode node : graph.vertexSet()) {
            // Start search at a depletion pullup
            if (node.getType() == NodeType.VT_DPULLUP) {
                StringBuffer sb = new StringBuffer();
                CircuitNode net = followOutgoingEdge(node, EdgeType.CHANNEL, null);
                sb.append(net.getId());
                sb.append(" = ");
                traverseOrNet(sb, net, true, null, new HashSet<String>(), false);
                System.out.println(sb);
            }
        }
    }

    // // Returns the set of nets connected to the transistor channel
    // public Set<NetNode> getConnectedNets(TransistorNode node) {
    // HashSet<NetNode> result = new HashSet<NetNode>();
    // for (CircuitEdge edge : graph.outgoingEdgesOf(node)) {
    // if (edge.getType() == EdgeType.CHANNEL) {
    // CircuitNode net = graph.getEdgeTarget(edge);
    // if (net.getType() == NodeType.VT_NET) {
    // result.add((NetNode) net);
    // } else {
    // throw new RuntimeException("Corrupted graph");
    // }
    // }
    // }
    // return result;
    // }

    // Returns the set of transistors connected to the net
    public Set<TransistorNode> getConnectedTransistors(NetNode net) {
        HashSet<TransistorNode> result = new HashSet<TransistorNode>();
        for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
            CircuitNode node = graph.getEdgeSource(edge);
            if (node.isCombinable()) {
                result.add((TransistorNode) node);
            }
        }
        return result;
    }

    public static boolean sameNet(Integer n1, Integer n2) {
        if (n1 != null && n2 != null) {
            return n1.equals(n2);
        }
        return n1 == null && n2 == null;
    }

    public Set<TransistorNode> findParallelTransistors(Integer n1, Integer n2) {
        HashSet<TransistorNode> result = new HashSet<TransistorNode>();
        Integer n = n1 != null ? n1 : n2;
        NetNode net1 = netMap.get(n);
        if (net1 == null) {
            throw new RuntimeException("Failed to lookup net " + n1);
        }
        for (TransistorNode tn : getConnectedTransistors(net1)) {
            if (sameNet(tn.getC1(), n1) && sameNet(tn.getC2(), n2)) {
                result.add(tn);
            } else if (sameNet(tn.getC1(), n2) && sameNet(tn.getC2(), n1)) {
                result.add(tn);
            }
        }
        return result;
    }

    public TransistorNode findSeriesTransistor(TransistorNode tn, Integer n1) {
        NetNode net1 = netMap.get(n1);
        if (net1 == null) {
            throw new RuntimeException("Failed to lookup net " + n1);
        }
        Set<TransistorNode> ts = getConnectedTransistors(net1);
        if (ts.size() == 2) {
            if (!ts.remove(tn)) {
                throw new RuntimeException("findSeriesTransistors: set did not contain " + tn.getId());
            }
            TransistorNode z = ts.iterator().next();
            if (n1.equals(z.getC1()) || n1.equals(z.getC2())) {
                return z;
            }
        }
        return null;
    }

    public void detectGates() {
        boolean done;
        do {
            done = true;
            // Merge Parallel Transistors
            for (CircuitNode node : graph.vertexSet()) {
                if (node.isCombinable()) {
                    TransistorNode t1 = (TransistorNode) node;
                    System.out.println(t1.getId() + " " + t1.getC1() + " " + t1.getC2());
                    Set<TransistorNode> parallel = findParallelTransistors(t1.getC1(), t1.getC2());
                    if (!parallel.remove(t1)) {
                        throw new RuntimeException("findParallelTransistors: set did not contain " + t1.getId());
                    }
                    if (!parallel.isEmpty()) {
                        StringBuffer f = new StringBuffer();
                        f.append("(");
                        f.append(t1.getFunction());
                        for (TransistorNode t2 : parallel) {
                            // TODO - add edge from t1 to t2.gate net
                            System.out.println("P Merging " + t2.getId() + " into " + t1.getId());
                            f.append(" OR ");
                            f.append(t2.getFunction());
                            graph.removeVertex(t2);
                        }
                        f.append(")");
                        done = false;
                        t1.setFunction(f.toString());
                        System.out.println(f.toString());
                        break; // Avoids a concurrent modification exception at
                               // the expense of some efficiency
                    }
                }
            }
            // Merge Series Transistors
            for (CircuitNode node : graph.vertexSet()) {
                if (node.isCombinable()) {
                    Integer inner = null;
                    TransistorNode t1 = (TransistorNode) node;
                    TransistorNode t2 = null;
                    Integer c1 = t1.getC1();                    
                    Integer c2 = t1.getC2();
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
                    System.out.println("S Merging " + t2.getId() + " into " + t1.getId());
                    StringBuffer f = new StringBuffer();
                    f.append("(");
                    f.append(t1.getFunction());
                    f.append(" AND ");
                    f.append(t2.getFunction());
                    f.append(")");
                    done = false;
                    t1.setFunction(f.toString());
                    System.out.println(f.toString());
                    // Update connections
                    Integer other = null;
                    if (inner.equals(t2.getC1()) && !inner.equals(t2.getC2())) {
                        other = t2.getC2();
                    } else if (inner.equals(t2.getC2()) && !inner.equals(t2.getC1())) {
                        other = t2.getC1();
                    } else {
                        throw new RuntimeException("t2 is not connected to inner");
                    }
                    System.out.println("Inner = " + inner + "; other = " + other);
                    System.out.println(t1);
                    System.out.println(t2);
                    if (inner.equals(t1.getC1()) && !inner.equals(t1.getC2())) {
                        t1.setC1(other);
                    } else if (inner.equals(t1.getC2()) && !inner.equals(t1.getC1())) {
                        t1.setC2(other);
                    } else {
                        throw new RuntimeException("t1 is not connected to inner");
                    }
                    System.out.println(t1);
                    System.out.println(t2);
                    if (other != null) {
                        CircuitNode otherNet = netMap.get(other);
                        if (otherNet == null) {
                            throw new RuntimeException("Failed to lookup other net " + other);
                        }
                        graph.addEdge(t1, otherNet).setChannel();
                        t1.setType(NodeType.VT_EFET);
                    } else {
                        t1.setType(NodeType.VT_EFET_VSS);
                    }                    
                    CircuitNode innerNet = netMap.get(inner);
                    if (innerNet == null) {
                        throw new RuntimeException("Failed to lookup inner net " + inner);
                    }
                    graph.removeAllEdges(t1, innerNet);
                    graph.removeAllEdges(t2, innerNet);
                    graph.removeVertex(t2);
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
        while (it.hasNext()) {
            GraphMapping<CircuitNode, CircuitEdge> mapping = it.next();

            // Log the mapping
            // for (NetNode subp : mod.getPorts()) {
            // System.out.println("Port " + subp.id + " =>" +
            // mapping.getVertexCorrespondence(subp, false).getId());
            // }

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
    }
}

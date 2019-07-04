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

    private void addTransistor(TransistorNode tr) {
        if (graph.containsVertex(tr)) {
            System.out.println("Skipping duplicate transistor: " + tr.getId());
            return;
        }
        graph.addVertex(tr);
        if (tr.gate != null) {
            CircuitNode netNode = addNet(tr.gate);
            graph.addEdge(tr, netNode).setGate();
        }
        if (tr.c1 != null) {
            CircuitNode netNode = addNet(tr.c1);
            graph.addEdge(tr, netNode).setChannel();
        }
        if (tr.c2 != null) {
            CircuitNode netNode = addNet(tr.c2);
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
        if (net.type != NodeType.VT_NET) {
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

    public void detectGates() {
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

package com.hoglet.nmoscircuitextractor;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.IsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;
import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class CircuitGraphReducer {

    protected Graph<CircuitNode, CircuitEdge> graph;
    protected String net_vss;
    protected String net_vcc;
    protected Set<NetNode> ignoreWarnings = new HashSet<NetNode>();
    protected Set<CircuitNode> pullupNetSet = new TreeSet<CircuitNode>();

    public CircuitGraphReducer(Graph<CircuitNode, CircuitEdge> graph, String net_vss, String net_vcc, Set<NetNode> ignoreWarnings) {
        this.graph = graph;
        this.net_vss = net_vss;
        this.net_vcc = net_vcc;
        this.ignoreWarnings.addAll(ignoreWarnings);
        buildPullupSet();
        buildNodeTypes();
    }

    private void buildPullupSet() {
        System.out.println("Building net pullup set");
        for (CircuitNode node : graph.vertexSet()) {
            if (node.getType() == NodeType.VT_NET) {
                for (CircuitEdge edge : graph.incomingEdgesOf(node)) {
                    CircuitNode source = graph.getEdgeSource(edge);
                    if (source.isPullup()) {
                        pullupNetSet.add(node);
                        node.setTree(true);
                    }
                }
            }
        }
        System.out.println("Building pullup net set done; size = " + pullupNetSet.size());
    }

    // ============================================================
    // Device marking
    // ============================================================

    private void buildNodeTypes() {
        System.out.println("Building node types");
        // Find all pullups then mark the FETs in the corresponding pulldown
        // tree
        for (CircuitNode seed : graph.vertexSet()) {
            if (seed.isPullup()) {
                boolean debug = false;
                debug |= seed.getId().equals("pullup737");
                for (CircuitEdge edge : graph.outgoingEdgesOf(seed)) {
                    if (edge.getType() == EdgeType.GATE) {
                        continue;
                    }
                    CircuitNode net = graph.getEdgeTarget(edge);
                    seed.setTree(followIncomingConnections(net, seed, new HashSet<CircuitNode>(), 0, debug));
                }
            }
        }

        // A normal FET with both channels connected to nets with pullups is
        // likely a bidirectional pass transistor
        for (CircuitNode node1 : graph.vertexSet()) {
            if (node1.getType() == NodeType.VT_EFET) {
                node1.setPass(true);
                for (CircuitEdge edge : graph.outgoingEdgesOf(node1)) {
                    if (edge.getType() == EdgeType.CHANNEL) {
                        CircuitNode net = graph.getEdgeTarget(edge);
                        if (!pullupNetSet.contains(net)) {
                            node1.setPass(false);
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("Building device types done");
    }

    private String pad(int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    private boolean followIncomingConnections(CircuitNode net1, CircuitNode seed, Set<CircuitNode> visited, int depth,
            boolean debug) {
        if (net1.getType() != NodeType.VT_NET) {
            throw new RuntimeException("corrupt graph: " + net1.getType().name());
        }
        visited.add(net1);
        if (debug) {
            System.out.println(pad(depth) + "Net " + net1 + ":");
        }
        boolean ret = false;
        // Stop if you hit a pullup net other than the original
        for (CircuitEdge edge1 : graph.incomingEdgesOf((net1))) {
            if (edge1.getType() == EdgeType.CHANNEL || edge1.getType() == EdgeType.PULLUP) {
                CircuitNode device = graph.getEdgeSource(edge1);
                if (debug) {
                    System.out.println(pad(depth) + "Checking for pullup: " + device);
                }
                if (device.isPullup() && !device.equals(seed)) {
                    if (depth > 0) {
                        if (debug) {
                            System.out.println(pad(depth) + "Stopping because pullup encountered: " + device);
                        }
                        return ret;
                    } else {
                        System.out.println(pad(depth) + "Note: Multiple pullups on net: " + net1);
                    }
                }
            }
        }
        for (CircuitEdge edge1 : graph.incomingEdgesOf((net1))) {
            if (edge1.getType() == EdgeType.CHANNEL) {
                boolean found = false;
                CircuitNode device = graph.getEdgeSource(edge1);
                if (device.getType() == NodeType.VT_EFET_VSS) {
                    found = true;
                } else {
                    for (CircuitEdge edge2 : graph.outgoingEdgesOf(device)) {
                        if (edge2.getType() == EdgeType.CHANNEL) {
                            CircuitNode net2 = graph.getEdgeTarget(edge2);
                            if (!visited.contains(net2)) {
                                found = followIncomingConnections(net2, seed, visited, depth + 1, debug);
                            }
                        }
                    }
                }
                if (found) {
                    device.setTree(true);
                    if (debug) {
                        System.out.println(pad(depth) + device + " is part of a pulldown network");
                    }
                    ret = true;
                }
            }
        }
        if (debug) {
            System.out.println(pad(depth) + "Net " + net1 + " = " + ret);
        }
        return ret;
    }

    // ============================================================
    // Gate detection
    // ============================================================

    // Returns the set of components connected to the net
    private Set<CircuitNode> getNeighbours(NetNode net) {
        HashSet<CircuitNode> result = new HashSet<CircuitNode>();
        for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
            CircuitNode node = graph.getEdgeSource(edge);
            if (node.getType() == NodeType.VT_NET) {
                throw new RuntimeException("Currupt graph");
            }
            result.add(node);
        }
        return result;
    }

    private Set<NetNode> getConnections(CircuitNode tn, EdgeType type) {
        Set<NetNode> nets = new HashSet<NetNode>();
        for (CircuitEdge edge : graph.outgoingEdgesOf(tn)) {
            if (edge.getType() == type) {
                CircuitNode target = graph.getEdgeTarget(edge);
                if (target.getType() != NodeType.VT_NET) {
                    throw new RuntimeException("Corrupt graph");
                }
                nets.add((NetNode) target);
            }
        }
        return nets;
    }

    private Set<NetNode> getGateNets(TransistorNode tn) {
        Set<NetNode> nets = getConnections(tn, EdgeType.GATE);
        if (nets.size() == 0) {
            throw new RuntimeException(tn + " has no gate connections");
        }
        return nets;
    }

    private Set<NetNode> getChannelNets(TransistorNode tn) {
        Set<NetNode> nets = getConnections(tn, EdgeType.CHANNEL);
        // Sanity check transistor has the expected number of channel
        // connections
        int expected = tn.getType() == NodeType.VT_EFET ? 2 : 1;
        int actual = nets.size();
        if (actual != expected) {
            throw new RuntimeException(
                    tn + " has wrong number of channel connections (expected = " + expected + "; actual = " + actual + ")");
        }
        return nets;
    }

    private Set<TransistorNode> findParallelTransistors(Set<NetNode> nets) {
        HashSet<TransistorNode> result = new HashSet<TransistorNode>();
        NetNode n = nets.iterator().next();
        for (CircuitNode cn : getNeighbours(n)) {
            if (cn.isCombinable()) {
                TransistorNode tn = (TransistorNode) cn;
                if (getChannelNets(tn).equals(nets)) {
                    result.add(tn);
                }
            }
        }
        return result;
    }

    private TransistorNode findSeriesTransistor(TransistorNode tn, NetNode net) {
        Set<CircuitNode> ts = getNeighbours(net);
        if (ts.size() == 2) {
            if (!ts.remove(tn)) {
                throw new RuntimeException("findSeriesTransistors: set did not contain " + tn.getId());
            }
            CircuitNode cn = ts.iterator().next();
            if (cn.isCombinable()) {
                TransistorNode z = (TransistorNode) cn;
                if (getChannelNets(z).contains(net)) {
                    return z;
                }
            }
        }
        return null;
    }

    private void copyEdges(TransistorNode t1, TransistorNode t2, EdgeType type) {
        for (NetNode net : getConnections(t2, type)) {
            CircuitEdge edge = graph.addEdge(t1, net);
            if (edge != null) {
                edge.setType(type);
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
                    Set<NetNode> channelNets = getChannelNets(t1);
                    Set<TransistorNode> parallel = findParallelTransistors(channelNets);
                    if (!parallel.remove(t1)) {
                        throw new RuntimeException("findParallelTransistors: set did not contain " + t1.getId());
                    }
                    if (!parallel.isEmpty()) {
                        StringBuffer f = new StringBuffer();
                        f.append("(");
                        f.append(t1.getFunction());
                        for (TransistorNode t2 : parallel) {
                            // System.out.println("P Merging " + t2 + " into " +
                            // t1);
                            f.append(" | ");
                            f.append(t2.getFunction());
                            // Move T2 gate connections to T1
                            copyEdges(t1, t2, EdgeType.GATE);
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
                    TransistorNode t1 = (TransistorNode) node;
                    TransistorNode t2 = null;
                    NetNode inner = null;
                    for (NetNode channel : getChannelNets(t1)) {
                        t2 = findSeriesTransistor(t1, channel);
                        if (t2 != null) {
                            inner = channel;
                            break;
                        }
                    }
                    if (t2 == null) {
                        continue;
                    }
                    if (t2.getType() == NodeType.VT_EFET_VSS) {
                        if (t1.getType() == NodeType.VT_EFET_VSS) {
                            throw new RuntimeException("Cannot series merge " + t1 + " and " + t2);
                        }
                        // Swap t2 and t1, so that we merge into the VT_EFET_VSS
                        TransistorNode tmp = t1;
                        t1 = t2;
                        t2 = tmp;
                    }
                    // System.out.println("S Merging " + t2 + " into " + t1);
                    StringBuffer f = new StringBuffer();
                    f.append("(");
                    f.append(t1.getFunction());
                    f.append(" & ");
                    f.append(t2.getFunction());
                    f.append(")");
                    t1.setFunction(f.toString());
                    // Update connections
                    NetNode other = null;
                    for (NetNode channel : getChannelNets(t2)) {
                        if (!channel.equals(inner)) {
                            other = channel;
                            break;
                        }
                    }
                    if (other == null) {
                        throw new RuntimeException("t2 is not connected to inner");
                    }
                    if (other != null) {
                        graph.addEdge(t1, other).setType(EdgeType.CHANNEL);
                    }
                    // Move T2 gate connections to T1
                    copyEdges(t1, t2, EdgeType.GATE);
                    graph.removeVertex(t2);
                    graph.removeVertex(inner);
                    done = false;
                    break; // Avoids a concurrent modification exception at
                           // the expense of some efficiency
                }
            }
        } while (!done);
    }

    // ============================================================
    // Match and replace instances of a module
    // ============================================================

    private static Map<String, Integer> numMap = new HashMap<String, Integer>();

    public void replaceModule(Module mod) {
        // Look for instances of the subgraph in the main graph
        IsomorphismInspector<CircuitNode, CircuitEdge> inspector = new VF2SubgraphIsomorphismInspector<CircuitNode, CircuitEdge>(
                graph, mod.getGraph(), new CircuitNodeComparator(graph), new CircuitEdgeCompator());
        Iterator<GraphMapping<CircuitNode, CircuitEdge>> it = inspector.getMappings();
        int count = 0;
        Map<ModuleNode, List<ModulePort>> toAdd = new HashMap<ModuleNode, List<ModulePort>>();
        Set<CircuitNode> toDelete = new HashSet<CircuitNode>();
        while (it.hasNext()) {
            GraphMapping<CircuitNode, CircuitEdge> mapping = it.next();

            // Has this mapping already be handled (from another perspective)
            // which can happen if the sub graph has internal symmetry
            boolean visited = false;
            for (CircuitNode subcn : mod.getGraph().vertexSet()) {
                if (!subcn.isExternal()) {
                    CircuitNode cn = mapping.getVertexCorrespondence(subcn, false);
                    if (toDelete.contains(cn)) {
                        visited = true;
                        break;
                    }
                }
            }
            if (visited) {
                continue;
            }

//            boolean skip = false;
//            for (CircuitNode subcn : mod.getGraph().vertexSet()) {
//                CircuitNode cn = mapping.getVertexCorrespondence(subcn, false);
//                if (cn.getId().equals("t1") || cn.getId().equals("t3")) {
//                    skip = true;
//                    break;
//                }
//            }
//            if (skip) {
//                System.out.println("Skipping");
//                continue;
//            }

            // Record the components (transistors, internal nets) for later
            // deletion (outside the iterator) in toDelete
            for (CircuitNode subcn : mod.getGraph().vertexSet()) {
                if (!subcn.isExternal()) {
                    CircuitNode cn = mapping.getVertexCorrespondence(subcn, false);
                    toDelete.add(cn);
                }
            }

            // Record the new module to add in toAdd
            Integer num = numMap.get(mod.getName());
            if (num == null) {
                num = 0;
            }
            num++;
            numMap.put(mod.getName(), num);
            ModuleNode modNode = new ModuleNode(mod.getName(), mod.getName() + num);
            List<ModulePort> ports = new LinkedList<ModulePort>();
            for (ModulePort subp : mod.getPorts()) {
                CircuitNode netNode = mapping.getVertexCorrespondence(subp.getNet(), false);
                ports.add(new ModulePort(subp.getName(), subp.getType(), (NetNode) netNode));
            }
            toAdd.put(modNode, ports);
            count++;
        }
        // Delete the components from the graph that have been replaced by new
        // modules
        for (CircuitNode cn : toDelete) {
            graph.removeVertex(cn);
        }
        // Add the new modules to the graph
        for (Entry<ModuleNode, List<ModulePort>> entry : toAdd.entrySet()) {
            ModuleNode modNode = entry.getKey();
            graph.addVertex(modNode);
            for (ModulePort modPort : entry.getValue()) {
                CircuitEdge edge = graph.addEdge(modNode, modPort.getNet());
                edge.setType(modPort.getType());
                edge.setName(modPort.getName());
            }
        }
        System.out.println(mod.getName() + ": replaced " + count + " instances");
    }

    public void markModules(Module mod, String id) {
        // Look for instances of the subgraph in the main graph
        IsomorphismInspector<CircuitNode, CircuitEdge> inspector = new VF2SubgraphIsomorphismInspector<CircuitNode, CircuitEdge>(
                graph, mod.getGraph(), new CircuitNodeComparator(graph), new CircuitEdgeCompator());
        Iterator<GraphMapping<CircuitNode, CircuitEdge>> it = inspector.getMappings();
        int count = 0;
        int marked = 0;
        Set<CircuitNode> seenBefore = new HashSet<CircuitNode>();
        while (it.hasNext()) {
            GraphMapping<CircuitNode, CircuitEdge> mapping = it.next();

            // Has this mapping already be handled (from another perspective)
            // which can happen if the sub graph has internal symmetry
            boolean visited = false;
            for (CircuitNode subcn : mod.getGraph().vertexSet()) {
                if (!subcn.isExternal()) {
                    CircuitNode cn = mapping.getVertexCorrespondence(subcn, false);
                    if (seenBefore.contains(cn)) {
                        visited = true;
                        break;
                    }
                }
            }
            if (visited) {
                continue;
            }

            // Record the components (transistors, internal nets) for later
            // deletion (outside the iterator) in toDelete
            for (CircuitNode subcn : mod.getGraph().vertexSet()) {
                CircuitNode cn = mapping.getVertexCorrespondence(subcn, false);
                if (subcn.getId().equals(id)) {
                    cn.setMark(true);
                    marked++;
                }
                if (!subcn.isExternal()) {
                    seenBefore.add(cn);
                }
            }
            count++;
        }

        System.out.println(mod.getName() + " seen " + count + " instances; marked " + marked);
    }

    public void removeEnhancementPullups() {
        Set<CircuitNode> toDelete = new HashSet<CircuitNode>();
        for (CircuitNode node : graph.vertexSet()) {
            if (node.getType() == NodeType.VT_EPULLUP) {
                CircuitEdge edge = graph.outgoingEdgesOf(node).iterator().next();
                CircuitNode net = graph.getEdgeTarget(edge);
                if (net.getId().contains("pcbit")) {
                    System.out.println("Removing pullup " + node.getId() + " on " + net.getId());
                    toDelete.add(node);
                }
            }
        }
        for (CircuitNode node : toDelete) {
            graph.removeVertex(node);
        }
    }

    // Follow the channel connections, tracking the following end points:
    //
    // Starting at a pullup / efet_vss, count:
    // - gate connections
    // - follow channels
    // - terminate at any of: pullup, efet_vss, efet_vcc
    //
    // Really trying to determine if set of channel connected nodes are
    // gate-only
    public boolean isDigitalNode(Set<CircuitNode> visited, NetNode net) {
        for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
            CircuitNode node = graph.getEdgeSource(edge);
            // Ignore edges to components that have already been visited
            if (visited.contains(node)) {
                continue;
            }
            switch (edge.getType()) {
            case INPUT:
            case GATE:
                continue;
            case OUTPUT:
            case BIDIRECTIONAL:
            case UNSPECIFIED:
            case PULLUP:
                return false;
            case CHANNEL:
                if (node.getType() == NodeType.VT_EFET) {
                    Set<NetNode> channels = getChannelNets((TransistorNode) node);
                    channels.remove(net);
                    NetNode other = channels.iterator().next();
                    visited.add(node);
                    if (!isDigitalNode(visited, other)) {
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            }
        }
        return true;
    }

    public void replaceDigitalNodes() {
        int count = 0;
        System.out.println("Replacing digital nodes");
        Set<CircuitNode> toDelete = new HashSet<CircuitNode>();
        for (CircuitNode cn : graph.vertexSet()) {
            if (cn.getType() == NodeType.VT_NET) {
                NetNode net = (NetNode) cn;
                Set<CircuitNode> visited = new HashSet<CircuitNode>();
                CircuitNode pullup = null;
                CircuitNode efet_vss = null;
                for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
                    CircuitNode node = graph.getEdgeSource(edge);
                    if (pullup == null && node.getType() == NodeType.VT_DPULLUP) {
                        visited.add(node);
                        pullup = node;
                    }
                    if (efet_vss == null && node.getType() == NodeType.VT_EFET_VSS && edge.getType() == EdgeType.CHANNEL) {
                        visited.add(node);
                        efet_vss = node;
                    }
                    if (pullup != null && efet_vss != null) {
                        if (isDigitalNode(visited, net)) {
                            toDelete.add(pullup);
                            toDelete.add(efet_vss);
                        }
                        break;
                    }
                }
            }
        }
        for (CircuitNode cn : toDelete) {
            if (cn.getType() == NodeType.VT_DPULLUP) {
                graph.removeVertex(cn);
            }
            if (cn.getType() == NodeType.VT_EFET_VSS) {
                TransistorNode tn = (TransistorNode) cn;
                NetNode output = getChannelNets(tn).iterator().next();
                FunctionNode mod = new FunctionNode(output.getId());
                mod.setFunction(tn.getFunction());
                mod.setInit(output.isMark());
                graph.addVertex(mod);
                graph.addEdge(mod, output).setType(EdgeType.OUTPUT);
                for (NetNode net : getGateNets(tn)) {
                    graph.addEdge(mod, net).setType(EdgeType.INPUT);
                }
                graph.removeVertex(tn);
                count++;
            }
        }
        System.out.println("    replaced " + count + " transistor/pullup pairs");
        System.out.println("Done replacing digital nodes");
    }

    public void markDigitalNets() {
        System.out.println("Marking digital nets");
        Set<NetNode> digital = new TreeSet<NetNode>();
        Set<NetNode> analog = new TreeSet<NetNode>();
        for (CircuitNode cn : graph.vertexSet()) {
            if (cn.getType() == NodeType.VT_NET) {
                boolean isDigital = false;
                NetNode net = (NetNode) cn;
                Set<CircuitNode> visited = new HashSet<CircuitNode>();
                for (CircuitEdge edge : graph.incomingEdgesOf(net)) {
                    CircuitNode node = graph.getEdgeSource(edge);
                    if ((node.getType() == NodeType.VT_FUNCTION || node.getType() == NodeType.VT_MODULE)
                            && edge.getType() == EdgeType.OUTPUT) {
                        visited.add(node);
                        isDigital = isDigitalNode(visited, net);
                        if (isDigital) {
                            digital.add(net);
                            net.setDigital(true);
                        } else {
                            System.out.println("Warning: Module output drives " + net + " that is not marked as digital");
                        }
                        break;
                    }
                }
                if (!isDigital) {
                    analog.add(net);
                }
            }
        }
        System.out.println("Nets driven that are digital:");
        for (NetNode net : digital) {
            System.out.println("    " + net);
        }
        System.out.println("Nets driven that are analog:");
        for (NetNode net : analog) {
            System.out.println("    " + net);
        }
        System.out.println("Summary:");
        System.out.println(" num_digital  = " + digital.size());
        System.out.println(" num_analog   = " + analog.size());
        System.out.println("Marking digital nets done");

    }
    // ============================================================
    // Graph Output
    // ============================================================

    public void dumpStats() {
        // Calc distribution of nodes by node types
        int[] nodeStatsNone = new int[NodeType.values().length];
        int[] nodeStatsPass = new int[NodeType.values().length];
        int[] nodeStatsTree = new int[NodeType.values().length];
        int[] nodeStatsBoth = new int[NodeType.values().length];
        int[] nodeStatsTotal = new int[NodeType.values().length];
        for (CircuitNode cn : graph.vertexSet()) {
            if (cn.isPass()) {
                if (cn.isTree()) {
                    nodeStatsBoth[cn.getType().ordinal()]++;
                } else {
                    nodeStatsPass[cn.getType().ordinal()]++;
                }
            } else {
                if (cn.isTree()) {
                    nodeStatsTree[cn.getType().ordinal()]++;
                } else {
                    nodeStatsNone[cn.getType().ordinal()]++;
                }
            }
            nodeStatsTotal[cn.getType().ordinal()]++;
        }
        System.out.println("Node type distribution:");
        System.out.println("        type :  none  pass  tree  both : total");
        for (int i = 0; i < nodeStatsTotal.length; i++) {
            System.out.print(String.format("%12s", NodeType.values()[i].name()));
            System.out.print(" :");
            System.out.print(String.format("%6d", nodeStatsNone[i]));
            System.out.print(String.format("%6d", nodeStatsPass[i]));
            System.out.print(String.format("%6d", nodeStatsTree[i]));
            System.out.print(String.format("%6d", nodeStatsBoth[i]));
            System.out.print(" :");
            System.out.print(String.format("%6d", nodeStatsTotal[i]));
            System.out.println();
        }
        // Distribution of nets by number of connections
        Map<Integer, Set<NetNode>> connectionMap = new TreeMap<Integer, Set<NetNode>>();
        // Distribution of nets by connectivity
        Map<String, Set<NetNode>> connectionTypeMap = new TreeMap<String, Set<NetNode>>();
        for (CircuitNode cn : graph.vertexSet()) {
            if (cn.getType() == NodeType.VT_NET) {
                NetNode net = (NetNode) cn;
                int connectionTypes[] = new int[EdgeType.values().length];
                Set<CircuitEdge> edges = graph.incomingEdgesOf(net);
                Integer key1 = edges.size();
                Set<NetNode> value1 = connectionMap.get(key1);
                if (value1 == null) {
                    value1 = new TreeSet<NetNode>();
                    connectionMap.put(key1, value1);
                }
                value1.add(net);
                for (CircuitEdge edge : edges) {
                    connectionTypes[edge.getType().ordinal()]++;
                }
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < connectionTypes.length; i++) {
                    if (connectionTypes[i] > 0) {
                        sb.append(EdgeType.values()[i].name());
                        sb.append('=');
                        sb.append(connectionTypes[i]);
                        sb.append(';');
                    }
                }
                String key2 = sb.toString();
                Set<NetNode> value2 = connectionTypeMap.get(key2);
                if (value2 == null) {
                    value2 = new TreeSet<NetNode>();
                    connectionTypeMap.put(key2, value2);
                }
                value2.add(net);
            }
        }
        // System.out.println("Net connectivity distribution:");
        // for (Map.Entry<Integer, Set<NetNode>> entry :
        // connectionMap.entrySet()) {
        // System.out.print(" " + entry.getValue().size() + "\t" +
        // entry.getKey());
        // if (entry.getValue().size() <= 100) {
        // System.out.print("\t" + entry.getValue());
        // }
        // System.out.println();
        // }
        // System.out.println("Net connectivity type distribution:");
        // for (Map.Entry<String, Set<NetNode>> entry :
        // connectionTypeMap.entrySet()) {
        // System.out.print(" " + entry.getValue().size() + "\t" +
        // entry.getKey());
        // if (entry.getValue().size() <= 100) {
        // System.out.print("\t" + entry.getValue());
        // }
        // System.out.println();
        // }
    }

    private String netLabel(NetNode net) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(net.toString());
        sb.append(']');
        if (!pullupNetSet.contains(net)) {
            sb.append('#');
        }
        return sb.toString();
    }

    private void dumpConnections(PrintStream ps, String message, Set<NetNode> nets) {
        if (nets.size() == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(message);
        sb.append(": ");
        boolean first = true;
        for (NetNode net : nets) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(netLabel(net));
            first = false;
        }
        ps.println(sb.toString());
    }

    private void dumpNode(PrintStream ps, CircuitNode tn) {
        ps.println(tn);
        if (tn.getType() == NodeType.VT_MODULE) {
            for (CircuitEdge edge : graph.outgoingEdgesOf(tn)) {
                ps.println(String.format("%17s: %s", edge.getName(), netLabel((NetNode) graph.getEdgeTarget(edge))));
            }
        } else {
            dumpConnections(ps, "             gate", getConnections(tn, EdgeType.GATE));
            dumpConnections(ps, "          channel", getConnections(tn, EdgeType.CHANNEL));
            dumpConnections(ps, "           pullup", getConnections(tn, EdgeType.PULLUP));
            dumpConnections(ps, "      unspecified", getConnections(tn, EdgeType.UNSPECIFIED));
            dumpConnections(ps, "            input", getConnections(tn, EdgeType.INPUT));
            dumpConnections(ps, "           output", getConnections(tn, EdgeType.OUTPUT));
            dumpConnections(ps, "    bidirectional", getConnections(tn, EdgeType.BIDIRECTIONAL));
        }
        if (tn  instanceof IFunction) {
            ps.println("               fn: NOT(" + ((IFunction) tn).getFunction() + ")");
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

    // ============================================================
    // Graph Validation
    // ============================================================

    public void validateGraph() {
        int count = 0;
        Set<NetNode> nets = new TreeSet<NetNode>();
        for (CircuitNode node : graph.vertexSet()) {
            if (node.getType() == NodeType.VT_NET) {
                if (!ignoreWarnings.contains(node)) {
                    nets.add((NetNode) node);
                }
            }
        }
        for (NetNode node : nets) {
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

}

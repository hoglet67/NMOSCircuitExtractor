package com.hoglet.nmoscircuitextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;
import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class CircuitGraphBuilder {
    protected String net_vss = "vss";
    protected String net_vcc = "vcc";

    protected Map<String, NetNode> netMap = new HashMap<String, NetNode>();
    protected Graph<CircuitNode, CircuitEdge> graph;

    protected Set<String> duplicate_map = new HashSet<String>();

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
        net.setExternal(true);
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
}

package com.hoglet.nmoscircuitextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.hoglet.nmoscircuitextractor.CircuitNode.NodeType;

public class CircuitGraphBuilder {
    protected int net_vss = 1;
    protected int net_vcc = 2;
    protected Map<Integer, NetNode> netMap = new HashMap<Integer, NetNode>();
    protected int[] stats = new int[NodeType.VT_NUM_TYPES.ordinal()];
    protected Graph<CircuitNode, CircuitEdge> graph;

    public CircuitGraphBuilder() {
        clearNetMap();
        clearStats();
        graph = new DefaultDirectedGraph<CircuitNode, CircuitEdge>(CircuitEdge.class);
    }

    private void clearNetMap() {
        netMap.clear();
    }

    private void clearStats() {
        for (int i = 0; i < stats.length; i++) {
            stats[i] = 0;
        }
    }

    private void dumpStats() {
        for (int i = 0; i < stats.length; i++) {
            System.out.println(NodeType.values()[i].name() + "  = " + stats[i]);
        }
    }

    private CircuitNode addNet(Integer net) {
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
            stats[netNode.getType().ordinal()]++;
            netMap.put(net, netNode);
        }
        netNode.incDegree();
        return netNode;
    }

    public void addTransistor(TransistorNode tr) {
        if (graph.containsVertex(tr)) {
            System.out.println("Skipping duplicate transistor: " + tr.getId());
            return;
        }
        graph.addVertex(tr);
        stats[tr.getType().ordinal()]++;
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

                // Known trap transistor (removed from netlist)
                if (gateNet == net_vss && channel1Net == net_vss && channel2Net == net_vss) {
                    System.out.println("Skipping transistor (g=vss, c1=vss, c2=vss): " + tr);
                    continue;
                }

                if (gateNet == net_vcc && channel1Net == net_vcc && channel2Net == net_vcc) {
                    System.out.println("Skipping transistor (g=vcc, c1=vcc, c2=vcc): " + tr);
                    continue;
                }

                if (channel1Net == net_vss && channel2Net == net_vss) {
                    System.out.println("Skipping transistor (c1=vss, c2=vss): " + tr);
                    continue;
                }

                if (channel1Net == net_vcc && channel2Net == net_vcc) {
                    System.out.println("Skipping transistor (c1=vcc, c2=vcc): " + tr);
                    continue;
                }

                // Enhancement Pullup
                if (gateNet == net_vcc && channel1Net == net_vcc) {
                    addTransistor(new TransistorNode(NodeType.VT_EPULLUP, tr, null, channel2Net, null));
                    continue;
                }
                if (gateNet == net_vcc && channel2Net == net_vcc) {
                    addTransistor(new TransistorNode(NodeType.VT_EPULLUP, tr, null, channel1Net, null));
                    continue;
                }

                if (gateNet == net_vss) {
                    System.out.println("Skipping transistor (g=vss): " + tr);
                    continue;
                }

                if (gateNet == net_vcc) {
                    System.out.println("Skipping transistor (g=vcc): " + tr);
                    continue;
                }

                if (gateNet == channel1Net) {
                    System.out.println("Skipping transistor (g=c1): " + tr);
                    continue;
                }

                if (gateNet == channel2Net) {
                    System.out.println("Skipping transistor (g=c2): " + tr);
                    continue;
                }
                if (channel1Net == channel2Net) {
                    System.out.println("Skipping transistor (c1=c2): " + tr);
                    continue;
                }

                if (channel1Net == net_vss) {
                    addTransistor(new TransistorNode(NodeType.VT_EFET_VSS, tr, gateNet, channel2Net, null));
                } else if (channel2Net == net_vss) {
                    addTransistor(new TransistorNode(NodeType.VT_EFET_VSS, tr, gateNet, channel1Net, null));
                } else if (channel1Net == net_vcc) {
                    addTransistor(new TransistorNode(NodeType.VT_EFET_VCC, tr, gateNet, channel2Net, null));
                } else if (channel2Net == net_vcc) {
                    addTransistor(new TransistorNode(NodeType.VT_EFET_VCC, tr, gateNet, channel1Net, null));
                } else {
                    addTransistor(new TransistorNode(NodeType.VT_EFET, tr, gateNet, channel1Net, channel2Net));
                }

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
            addTransistor(new TransistorNode(NodeType.VT_DPULLUP, "pullup" + net, null, net, null));
        }
        dumpStats();

        return graph;
    }

    public Graph<CircuitNode, CircuitEdge> getGraph() {
        return graph;
    }
}

package com.hoglet.nmoscircuitextractor;

import java.util.LinkedList;
import java.util.List;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class ModuleGen {

    public static String net_vss = "vss";
    public static String net_vcc = "vcc";

    // Build a small subgraph that looks like the register cell
    public static Module registerModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(100))); // in/out
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(101))); // in/out
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // select
        builder.addTransistor("200", 111, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 102, 110, 100);
        builder.addTransistor("203", 102, 111, 101);
        builder.addPullup("1", 110);
        builder.addPullup("2", 111);
        return new Module("register", builder.getGraph(), ports);
    }

    public static Module invertingSuperBufferModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        builder.addPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addTransistor("202", 110, 101, net_vcc);
        return new Module("invertingSuperbuffer", builder.getGraph(), ports);
    }

    public static Module noninvertingSuperBufferModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        builder.addPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 110, 101, net_vss);
        builder.addTransistor("202", 100, 101, net_vcc);
        return new Module("noninvertingSuperBuffer", builder.getGraph(), ports);
    }

    public static Module oldStorageModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // data
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // clock
        builder.addTransistor("200", 102, 101, 110);
        builder.addTransistor("201", 110, 100, net_vss);
        builder.addPullup("1", 100);
        return new Module("storage", builder.getGraph(), ports);
    }

    public static Module storageModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        NetNode net100 = builder.addExternal(100);
        net100.setGateOnly(true);
        ports.add(new ModulePort(EdgeType.OUTPUT, net100)); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // data
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // clock
        builder.addTransistor("200", 102, 101, 100);
        return new Module("storage", builder.getGraph(), ports);
    }

    public static Module xor2Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(102))); // output
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 101, 111, net_vss);
        builder.addTransistor("202", 110, 111, 112);
        builder.addTransistor("203", 111, 110, 112);
        builder.addTransistor("204", 112, 102, net_vss);
        builder.addPullup("1", 110);
        builder.addPullup("2", 111);
        builder.addPullup("3", 112);
        builder.addPullup("4", 102);
        return new Module("xor2", builder.getGraph(), ports);
    }

    public static Module xnor2Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(102))); // output
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 101, 111, net_vss);
        builder.addTransistor("202", 110, 111, 102);
        builder.addTransistor("203", 111, 110, 102);
        builder.addPullup("1", 110);
        builder.addPullup("2", 111);
        builder.addPullup("3", 102);
        return new Module("xnor2", builder.getGraph(), ports);
    }

    public static Module IRLatchModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // data
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(103))); // not
                                                                             // write
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(104))); // write
        builder.addTransistor("200", 112, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 103, 111, 112);
        builder.addPullup("1", 110);
        builder.addPullup("2", 111);
        builder.addTransistor("204", 110, 100, net_vcc);
        builder.addTransistor("205", 111, 100, net_vss);
        builder.addTransistor("206", 111, 101, net_vcc);
        builder.addTransistor("207", 110, 101, net_vss);
        builder.addTransistor("207", 104, 112, 102);
        return new Module("IRLatch", builder.getGraph(), ports);
    }

    public static Module latchModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // data
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(103))); // clock
        builder.addTransistor("200", 102, 100, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addTransistor("202", 103, 101, 102);
        builder.addPullup("1", 100);
        builder.addPullup("2", 101);
        return new Module("latch", builder.getGraph(), ports);
    }

    public static Module clockedRSLatchModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // set
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(103))); // reset
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(104))); // clock
        builder.addTransistor("200", 100, 101, net_vss);
        builder.addTransistor("201", 101, 100, net_vss);
        builder.addTransistor("202", 104, 102, 100);
        builder.addTransistor("203", 104, 103, 101);
        builder.addPullup("1", 100);
        builder.addPullup("2", 101);
        return new Module("clockedRSLatch", builder.getGraph(), ports);
    }

    public static Module clockedRSLatchPPModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // set
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // reset
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(103))); // clock
        builder.addTransistor("200", 111, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 103, 101, 110);
        builder.addTransistor("203", 103, 102, 111);
        builder.addTransistor("204", 110, 100, net_vss);
        builder.addTransistor("205", 111, 100, net_vcc);
        builder.addPullup("1", 110);
        builder.addPullup("2", 111);
        return new Module("clockedRSLatchPP", builder.getGraph(), ports);
    }

    public static Module RSLatchModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // set
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(103))); // reset
        builder.addTransistor("200", 100, 101, net_vss);
        builder.addTransistor("201", 101, 100, net_vss);
        builder.addTransistor("202", 102, 100, net_vss);
        builder.addTransistor("203", 103, 101, net_vss);
        builder.addPullup("1", 100);
        builder.addPullup("2", 101);
        return new Module("RSLatch", builder.getGraph(), ports);
    }

    public static Module RSLatchPPModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // set
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // reset
        builder.addTransistor("200", 111, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 101, 110, net_vss);
        builder.addTransistor("203", 102, 111, net_vss);
        builder.addTransistor("204", 110, 100, net_vss);
        builder.addTransistor("205", 111, 100, net_vcc);
        builder.addPullup("1", 110);
        builder.addPullup("2", 111);
        return new Module("RSLatchPP", builder.getGraph(), ports);
    }

    public static Module commonNANDModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // inputA
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(103))); // inputB
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(104))); // inputC(common)
        builder.addTransistor("200", 102, 100, 110);
        builder.addTransistor("201", 103, 101, 110);
        builder.addTransistor("202", 104, 110, net_vss);
        builder.addPullup("1", 100);
        builder.addPullup("2", 101);
        return new Module("commonNAND", builder.getGraph(), ports);
    }

    public static Module crossCoupledTransistors1Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // inputA
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // inputB
        builder.addTransistor("200", 101, 102, 100);
        builder.addTransistor("201", 102, 101, 100);
        builder.addPullup("1", 100);
        return new Module("crossCoupledTransistors1", builder.getGraph(), ports);
    }

    public static Module crossCoupledTransistors2Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(101))); // output
        builder.addTransistor("200", 101, 100, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addPullup("1", 100);
        builder.addPullup("2", 101);
        return new Module("crossCoupledTransistors2", builder.getGraph(), ports);
    }

    public static Module pushPullModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(102))); // input
        builder.addTransistor("200", 101, 100, net_vss);
        builder.addTransistor("201", 102, 100, net_vcc);
        return new Module("pushPull", builder.getGraph(), ports);
    }

    public static Module abPinDriverModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(1018))); // +input
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(1019))); // -input
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(627))); // oe
        // Push/Pull output stage
        builder.addTransistor("200", 2759, 100, net_vcc);
        builder.addTransistor("201", 2754, 100, net_vss);
        // High-side driver
        builder.addTransistor("202", 2753, 2754, net_vcc);
        builder.addTransistor("203", 1018, 2754, net_vss);
        builder.addTransistor("204", 1018, 2753, net_vss);
        builder.addTransistor("205", 627, 2753, net_vss);
        builder.addTransistor("206", 627, 2754, net_vss);
        builder.addPullup("1", 2753);
        // Low-side driver
        builder.addTransistor("212", 2718, 2759, net_vcc);
        builder.addTransistor("213", 1019, 2759, net_vss);
        builder.addTransistor("214", 1019, 2718, net_vss);
        builder.addTransistor("215", 627, 2718, net_vss);
        builder.addTransistor("216", 627, 2759, net_vss);
        builder.addPullup("2", 2718);
        return new Module("abPinDriver", builder.getGraph(), ports);
    }

    public static Module pass8Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(100))); // A0
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(101))); // A1
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(102))); // A2
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(103))); // A3
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(104))); // A4
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(105))); // A5
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(106))); // A6
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(107))); // A7
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(110))); // B0
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(111))); // B1
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(112))); // B2
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(113))); // B3
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(114))); // B4
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(115))); // B5
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(116))); // B6
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(117))); // B7
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(120))); // input
        builder.addTransistor("200", 120, 100, 110);
        builder.addTransistor("201", 120, 101, 111);
        builder.addTransistor("202", 120, 102, 112);
        builder.addTransistor("203", 120, 103, 113);
        builder.addTransistor("204", 120, 104, 114);
        builder.addTransistor("205", 120, 105, 115);
        builder.addTransistor("206", 120, 106, 116);
        builder.addTransistor("207", 120, 107, 117);
        return new Module("pass8", builder.getGraph(), ports);
    }

    public static List<Module> getModules() {
        List<Module> list = new LinkedList<Module>();
        // list.add(commonNANDModule()); // No instances
        list.add(xor2Module());
        list.add(xnor2Module());
        list.add(abPinDriverModule());
        list.add(invertingSuperBufferModule());
        list.add(noninvertingSuperBufferModule());
        list.add(registerModule());
        list.add(IRLatchModule());
        list.add(latchModule());
        list.add(clockedRSLatchPPModule());
        list.add(clockedRSLatchModule());
        list.add(RSLatchPPModule());
        list.add(RSLatchModule());
        list.add(storageModule());
        list.add(crossCoupledTransistors1Module());
        list.add(crossCoupledTransistors2Module());
        // list.add(pushPullModule());
        // list.add(pass8Module()); // Search blows up
        return list;
    }

    // public static Module Module() {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder();
    // return new Module("", builder.getGraph(), ports);
    // }

}
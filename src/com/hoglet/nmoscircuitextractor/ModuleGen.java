package com.hoglet.nmoscircuitextractor;

import java.util.LinkedList;
import java.util.List;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class ModuleGen {

    public static int net_vss = 1;
    public static int net_vcc = 2;

    // Build a small subgraph that looks like the register cell
    public static Module registerModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(200))); // in/out
        ports.add(new ModulePort(EdgeType.BIDIRECTIONAL, builder.addExternal(201))); // in/out
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(202))); // select
        builder.addTransistor("1", 100, 101, net_vss);
        builder.addTransistor("2", 101, 100, net_vss);
        builder.addTransistor("3", 202, 100, 200);
        builder.addTransistor("4", 202, 101, 201);
        builder.addPullup("5", 100);
        builder.addPullup("6", 101);
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

    public static Module storageModule() {
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

    public static Module rsLatchModule() {
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
        return new Module("rsLatch", builder.getGraph(), ports);
    }

    public static Module rslatchPPModule() {
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
        return new Module("rslatchPP", builder.getGraph(), ports);
    }

    public static List<Module> getModules() {
        List<Module> list = new LinkedList<Module>();
        list.add(rslatchPPModule());
        list.add(xor2Module());
        list.add(xnor2Module());
        list.add(invertingSuperBufferModule());
        list.add(noninvertingSuperBufferModule());
        list.add(registerModule());
        list.add(latchModule());
        list.add(rsLatchModule());
        list.add(storageModule());
        return list;
    }

    // public static Module Module() {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder();
    // return new Module("", builder.getGraph(), ports);
    // }

    public static Module nand2Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(200))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(201))); // input
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(202))); // input
        builder.addTransistor("1", 201, 100, net_vss);
        builder.addTransistor("2", 202, 200, 100);
        builder.addPullup("3", 200);
        return new Module("nand2", builder.getGraph(), ports);
    }

}
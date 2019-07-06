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

    public static Module passInverterModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal(200))); // output
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(201))); // input
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal(202))); // select
        builder.addPullup("1", 200);
        builder.addTransistor("200", 202, 201, 100);
        builder.addTransistor("201", 100, 200, net_vss);
        return new Module("passInverter", builder.getGraph(), ports);
    }

}
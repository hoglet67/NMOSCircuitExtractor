package com.hoglet.nmoscircuitextractor;

import java.util.LinkedList;
import java.util.List;

public class ModuleGen {

    public static int net_vss = 1;
    public static int net_vcc = 2;

    // Build a small subgraph that looks like the register cell
    public static Module registerModule() {
        List<NetNode> ports = new LinkedList<NetNode>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(builder.addExternal(200)); // in/out
        ports.add(builder.addExternal(201)); // in/out
        ports.add(builder.addExternal(202)); // select
        builder.addTransistor("1", 100, 101, net_vss);
        builder.addTransistor("2", 101, 100, net_vss);
        builder.addTransistor("3", 202, 100, 200);
        builder.addTransistor("4", 202, 101, 201);
        builder.addPullup("5", 100);
        builder.addPullup("6", 101);
        return new Module(builder.getGraph(), ports);
    }

    public static Module nand2Module() {
        List<NetNode> ports = new LinkedList<NetNode>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(builder.addExternal(200)); // output
        ports.add(builder.addExternal(201)); // input
        ports.add(builder.addExternal(202)); // input
        builder.addTransistor("1", 201, 100, net_vss);
        builder.addTransistor("2", 202, 200, 100);
        builder.addPullup("3", 200);
        return new Module(builder.getGraph(), ports);
    }

    public static Module invertingSuperBufferModule() {
        List<NetNode> ports = new LinkedList<NetNode>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(builder.addExternal(100)); // input
        ports.add(builder.addExternal(101)); // output
        builder.addPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addTransistor("202", 110, 101, net_vcc);
        return new Module(builder.getGraph(), ports);
    }

    public static Module noninvertingSuperBufferModule() {
        List<NetNode> ports = new LinkedList<NetNode>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(builder.addExternal(100)); // input
        ports.add(builder.addExternal(101)); // output
        builder.addPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 110, 101, net_vss);
        builder.addTransistor("202", 100, 101, net_vcc);
        return new Module(builder.getGraph(), ports);
    }

    public static Module passInverterModule() {
        List<NetNode> ports = new LinkedList<NetNode>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder();
        ports.add(builder.addExternal(200)); // output
        ports.add(builder.addExternal(201)); // input
        ports.add(builder.addExternal(202)); // select
        builder.addPullup("1", 200);
        builder.addTransistor("200", 202, 201, 100);
        builder.addTransistor("201", 100, 200, net_vss);
        return new Module(builder.getGraph(), ports);
    }

}
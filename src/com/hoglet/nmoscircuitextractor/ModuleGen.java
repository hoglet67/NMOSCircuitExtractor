package com.hoglet.nmoscircuitextractor;

import java.util.LinkedList;
import java.util.List;

import com.hoglet.nmoscircuitextractor.CircuitEdge.EdgeType;

public class ModuleGen {

    private String net_vss;
    private String net_vcc;

    private int nextNet = 100;

    public ModuleGen(String net_vss, String net_vcc) {
        this.net_vss = net_vss;
        this.net_vcc = net_vcc;
    }

    private String nextNet() {
        return "" + (nextNet++);
    }

    private void addRegister(CircuitGraphBuilder builder, String name, String busP, String busN, String sel) {
        String net0 = nextNet();
        String net1 = nextNet();
        builder.addTransistor(name + "0", net1, net0, net_vss);
        builder.addTransistor(name + "1", net0, net1, net_vss);
        builder.addTransistor(name + "2", sel, net0, busP);
        builder.addTransistor(name + "3", sel, net1, busN);
        builder.addDPullup(name + "1", net0);
        builder.addDPullup(name + "2", net1);
    }

    private void addSuperComplementary(CircuitGraphBuilder builder, String name, String in, String OP, String ON) {
        String net0 = nextNet();
        builder.addDPullup(net0 + "_pullup", net0);
        builder.addTransistor(name + "0", in, net0, net_vss);
        builder.addTransistor(name + "1", in, ON, net_vss);
        builder.addTransistor(name + "2", net0, ON, net_vcc);
        builder.addTransistor(name + "3", net0, OP, net_vss);
        builder.addTransistor(name + "4", in, OP, net_vcc);
    }

    private Module regfileSliceModule() {
        String[] names = new String[] { "pc", "ir", "wz", "sp", "iy", "ix", "hl1", "hl0", "de1", "de0", "bc1", "bc0", "af1",
                "af0" };
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);

        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal("pc_din")));
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal("pc_wr")));
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal("r_p")));
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal("r_x1")));
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal("clk")));
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal("reg_wr")));
        ports.add(new ModulePort(EdgeType.INPUT, builder.addExternal("reg_din")));
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal("reg_dout")));
        ports.add(new ModulePort(EdgeType.OUTPUT, builder.addExternal("pc_dout")));

        for (int i = 0; i < names.length; i++) {
            String reg = "reg_" + names[i];
            String sel = "regsel" + names[i];
            ports.add(new ModulePort(EdgeType.INPUT, builder.addGlobal(sel)));
            String busP = i < 2 ? "pcbit" : "regbit";
            String busN = i < 2 ? "_pcbit" : "_regbit";
            addRegister(builder, reg, busP, busN, sel);
        }

        builder.addTransistor("pcout", "pcbit", "pc_dout", net_vss);
        builder.addDPullup("pullpcout", "pc_dout");

        addSuperComplementary(builder, "sc1", "pc_din", "pcP", "pcN");
        builder.addTransistor("pass1", "pc_wr", "pcP", "pcbit");
        builder.addTransistor("pass2", "pc_wr", "pcN", "_pcbit");
        builder.addTransistor("pass3", "r_p", "pcbit", "regbit");
        builder.addTransistor("pass4", "r_p", "_pcbit", "_regbit");
        builder.addTransistor("pass5", "reg_wr", "regbit", "regP");
        builder.addTransistor("pass6", "reg_wr", "_regbit", "regN");

        builder.addTransistor("pull1a", "clk", "regbit", "p1");
        builder.addTransistor("pull1b", "r_x1", "p1", net_vcc);
        builder.addTransistor("pull2a", "clk", "_regbit", "p2");
        builder.addTransistor("pull2b", "r_x1", "p2", net_vcc);

        builder.addEPullup("pull3", "pcbit");
        builder.addEPullup("pull4", "_pcbit");

        // builder.addTransistor("uinv", "reg_din", "_reg_din", net_vss);
        // builder.addDPullup("pull5", "_reg_din");
        addSuperComplementary(builder, "sc2", "reg_din", "regP", "regN");

        builder.addTransistor("regout", "_regbit", "reg_dout", net_vss);
        builder.addDPullup("pullregout", "reg_dout");

        return new Module("regfileSlice", builder.getGraph(), ports);
    }

    // Build a small subgraph that looks like the register cell
    private Module registerModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("Q", EdgeType.BIDIRECTIONAL, builder.addExternal(100))); // in/out
        ports.add(new ModulePort("NQ", EdgeType.BIDIRECTIONAL, builder.addExternal(101))); // in/out
        ports.add(new ModulePort("SEL", EdgeType.INPUT, builder.addExternal(102))); // select
        builder.addTransistor("200", 111, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 102, 110, 100);
        builder.addTransistor("203", 102, 111, 101);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        return new Module("register", builder.getGraph(), ports);
    }

    private Module superComplementaryModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("I", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("O1", EdgeType.OUTPUT, builder.addExternal(101))); // output
        ports.add(new ModulePort("O2", EdgeType.OUTPUT, builder.addExternal(102))); // output
        builder.addDPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addTransistor("202", 110, 101, net_vcc);
        builder.addTransistor("203", 110, 102, net_vss);
        builder.addTransistor("204", 100, 102, net_vcc);
        return new Module("superComplementary", builder.getGraph(), ports);
    }

    private Module superInvertorModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("I", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("O", EdgeType.OUTPUT, builder.addExternal(101))); // output
        builder.addDPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addTransistor("202", 110, 101, net_vcc);
        return new Module("superInverter", builder.getGraph(), ports);
    }

    private Module superPushPullModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("I", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("O", EdgeType.OUTPUT, builder.addExternal(101))); // output
        builder.addDPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 100, 111, net_vss);
        builder.addTransistor("202", 110, 111, net_vcc);
        builder.addDPullup("2", 112);
        builder.addTransistor("203", 111, 112, net_vss);
        builder.addTransistor("204", 111, 113, net_vss);
        builder.addTransistor("205", 112, 113, net_vcc);
        builder.addTransistor("206", 113, 101, net_vss);
        builder.addTransistor("207", 111, 101, net_vcc);
        return new Module("superPushPull", builder.getGraph(), ports);
    }

    private Module superBufferModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("I", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("O", EdgeType.OUTPUT, builder.addExternal(101))); // output
        builder.addDPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 110, 101, net_vss);
        builder.addTransistor("202", 100, 101, net_vcc);
        return new Module("superBuffer", builder.getGraph(), ports);
    }

    private Module superNORModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("I1", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("I2", EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort("O", EdgeType.OUTPUT, builder.addExternal(102))); // output
        builder.addDPullup("1", 110);
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 101, 110, net_vss);
        builder.addTransistor("202", 100, 102, net_vss);
        builder.addTransistor("203", 101, 102, net_vss);
        builder.addTransistor("204", 110, 102, net_vcc);
        return new Module("superNOR", builder.getGraph(), ports);
    }

    // Don't try to extract this module, it's actually a buffer with output
    // enable!
    //
    // private Module superNORAltModule() {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
    // ports.add(new ModulePort("I1", EdgeType.INPUT,
    // builder.addExternal(100))); // input
    // ports.add(new ModulePort("I2", EdgeType.INPUT,
    // builder.addExternal(101))); // input
    // ports.add(new ModulePort("O", EdgeType.OUTPUT,
    // builder.addExternal(102))); // output
    // builder.addDPullup("1", 110);
    // builder.addDPullup("2", 111);
    // builder.addTransistor("200", 100, 110, net_vss);
    // builder.addTransistor("201", 101, 110, net_vss);
    // builder.addTransistor("202", 110, 111, net_vss);
    // builder.addTransistor("203", 101, 111, net_vss);
    // builder.addTransistor("204", 111, 102, net_vcc);
    // builder.addTransistor("205", 110, 102, net_vss);
    // return new Module("superNORAlt", builder.getGraph(), ports);
    // }

    private Module superNANDModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("I1", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("I2", EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort("O", EdgeType.OUTPUT, builder.addExternal(102))); // output
        builder.addDPullup("1", 110);
        builder.addTransistor("200", 100, 110, 111);
        builder.addTransistor("201", 101, 111, net_vss);
        builder.addTransistor("202", 100, 102, 112);
        builder.addTransistor("203", 101, 112, net_vss);
        builder.addTransistor("204", 110, 102, net_vcc);
        return new Module("superNAND", builder.getGraph(), ports);
    }

    // private Module oldStorageModule() {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
    // ports.add(new ModulePort("Q", EdgeType.OUTPUT,
    // builder.addExternal(100))); // output
    // ports.add(new ModulePort("D", EdgeType.INPUT, builder.addExternal(101)));
    // // data
    // ports.add(new ModulePort("G", EdgeType.INPUT, builder.addExternal(102)));
    // // clock
    // builder.addTransistor("200", 102, 101, 110);
    // builder.addTransistor("201", 110, 100, net_vss);
    // builder.addDPullup("1", 100);
    // return new Module("storage", builder.getGraph(), ports);
    // }

    private Module storage1GModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        NetNode net100 = builder.addExternal(100);
        net100.setChannelConstraint(1);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, net100)); // output
        ports.add(new ModulePort("D", EdgeType.INPUT, builder.addExternal(101))); // data
        ports.add(new ModulePort("G", EdgeType.INPUT, builder.addExternal(102))); // gate
        builder.addTransistor("200", 102, 101, 100);
        return new Module("storage1G", builder.getGraph(), ports);
    }

    private Module storage2GaModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        NetNode net100 = builder.addExternal(100);
        net100.setChannelConstraint(1);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, net100)); // output
        ports.add(new ModulePort("D", EdgeType.INPUT, builder.addExternal(101))); // data
        ports.add(new ModulePort("G", EdgeType.INPUT, builder.addExternal(102))); // gate
        builder.addTransistor("200", 102, 110, 100);
        builder.addTransistor("201", 102, 101, 110);
        return new Module("storage2Ga", builder.getGraph(), ports);
    }

    private Module storage2GbModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        NetNode net100 = builder.addExternal(100);
        net100.setChannelConstraint(1);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, net100)); // output
        ports.add(new ModulePort("D", EdgeType.INPUT, builder.addExternal(101))); // data
        ports.add(new ModulePort("G1", EdgeType.INPUT, builder.addExternal(102))); // gate1
        ports.add(new ModulePort("G2", EdgeType.INPUT, builder.addExternal(103))); // gate2
        builder.addTransistor("200", 102, 110, 100);
        builder.addTransistor("201", 103, 101, 110);
        return new Module("storage2Gb", builder.getGraph(), ports);
    }

    // private Module muxModule(int n) {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
    // NetNode net100 = builder.addExternal(100);
    // net100.setChannelConstraint(n);
    // ports.add(new ModulePort("O", EdgeType.OUTPUT, net100)); // output
    // for (int i = 0; i < n; i++) {
    // ports.add(new ModulePort("D" + i, EdgeType.INPUT, builder.addExternal(101
    // + 50 * i))); // data0
    // ports.add(new ModulePort("S" + i, EdgeType.INPUT, builder.addExternal(102
    // + 50 * i))); // clock0
    // builder.addTransistor("" + (200 + i), 102 + 50 * i, 101 + 50 * i, 100);
    // }
    // return new Module("mux" + n, builder.getGraph(), ports);
    // }

    private Module xor2Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("A", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("B", EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort("O", EdgeType.OUTPUT, builder.addExternal(102))); // output
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 101, 111, net_vss);
        builder.addTransistor("202", 110, 111, 112);
        builder.addTransistor("203", 111, 110, 112);
        builder.addTransistor("204", 112, 102, net_vss);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        builder.addDPullup("3", 112);
        builder.addDPullup("4", 102);
        return new Module("xor2", builder.getGraph(), ports);
    }

    private Module xnor2Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("A", EdgeType.INPUT, builder.addExternal(100))); // input
        ports.add(new ModulePort("B", EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort("O", EdgeType.OUTPUT, builder.addExternal(102))); // output
        builder.addTransistor("200", 100, 110, net_vss);
        builder.addTransistor("201", 101, 111, net_vss);
        builder.addTransistor("202", 110, 111, 102);
        builder.addTransistor("203", 111, 110, 102);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        builder.addDPullup("3", 102);
        return new Module("xnor2", builder.getGraph(), ports);
    }

    private Module z80DBLatchModule() {
        // See http://baltazarstudios.com/anatomy-z80-gate/
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("DQ", EdgeType.OUTPUT, builder.addExternal(100))); // DQ
        ports.add(new ModulePort("DIN", EdgeType.INPUT, builder.addExternal(101))); // Din
        ports.add(new ModulePort("DB", EdgeType.BIDIRECTIONAL, builder.addExternal(102))); // DB
        ports.add(new ModulePort("RD_PIN_WR_LAT", EdgeType.INPUT, builder.addExternal(103))); // B
        ports.add(new ModulePort("RD_BUS_WR_LAT", EdgeType.INPUT, builder.addExternal(104))); // C
        ports.add(new ModulePort("RD_LAT_WR_BUS", EdgeType.INPUT, builder.addExternal(105))); // D
        builder.addTransistor("30", 105, 113, 102);
        builder.addTransistor("31", 112, 113, net_vss);
        builder.addTransistor("32", 111, 113, net_vcc);
        builder.addTransistor("33", 104, 102, 100);
        builder.addTransistor("34", 100, 112, net_vss);
        builder.addDPullup("35", 112);
        builder.addTransistor("36", 112, 111, net_vss);
        builder.addDPullup("37", 111);
        builder.addTransistor("38", 103, 101, 100);
        builder.addTransistor("39", 110, 111, 100);
        builder.addTransistor("40", 103, 110, net_vss);
        builder.addTransistor("41", 104, 110, net_vss);
        builder.addDPullup("42", 110);
        return new Module("DBLatch", builder.getGraph(), ports);
    }

    private Module z80IRLatchModule() {
        // See http://baltazarstudios.com/z80-instruction-register-deciphered/
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort("NQ", EdgeType.OUTPUT, builder.addExternal(101))); // output
        ports.add(new ModulePort("DIN", EdgeType.INPUT, builder.addExternal(102))); // data
        ports.add(new ModulePort("NWR", EdgeType.INPUT, builder.addExternal(103))); // not_write
        ports.add(new ModulePort("WR", EdgeType.INPUT, builder.addExternal(104))); // write
        builder.addTransistor("200", 112, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 103, 111, 112);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        builder.addTransistor("204", 110, 100, net_vcc);
        builder.addTransistor("205", 111, 100, net_vss);
        builder.addTransistor("206", 111, 101, net_vcc);
        builder.addTransistor("207", 110, 101, net_vss);
        builder.addTransistor("207", 104, 112, 102);
        return new Module("IRLatch", builder.getGraph(), ports);
    }

    private Module latchModule(boolean incQ, boolean incNQ) {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        if (incNQ) {
            ports.add(new ModulePort("NQ", EdgeType.OUTPUT, builder.addExternal(100))); // output
        }
        if (incQ) {
            ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(101))); // output
        }
        ports.add(new ModulePort("D", EdgeType.INPUT, builder.addExternal(102))); // data
        ports.add(new ModulePort("CLK", EdgeType.INPUT, builder.addExternal(103))); // clock
        builder.addTransistor("200", 102, 100, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addTransistor("202", 103, 101, 102);
        builder.addDPullup("1", 100);
        builder.addDPullup("2", 101);
        return new Module("latch", builder.getGraph(), ports);
    }

    private Module latchPassModule(boolean incQ, boolean incNQ) {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        if (incNQ) {
            ports.add(new ModulePort("NQ", EdgeType.OUTPUT, builder.addExternal(100))); // output
        }
        if (incQ) {
            ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(101))); // output
        }
        ports.add(new ModulePort("D", EdgeType.INPUT, builder.addExternal(102))); // data
        ports.add(new ModulePort("WR", EdgeType.INPUT, builder.addExternal(103))); // clock
        ports.add(new ModulePort("NWR", EdgeType.INPUT, builder.addExternal(104))); // clock
        builder.addTransistor("200", 110, 100, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addTransistor("202", 104, 101, 110);
        builder.addTransistor("203", 103, 102, 110);
        builder.addDPullup("1", 100);
        builder.addDPullup("2", 101);
        return new Module("latchPass", builder.getGraph(), ports);
    }

    private Module latchTogglePPModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("P", EdgeType.INPUT, builder.addExternal(100))); // precharge
        ports.add(new ModulePort("T", EdgeType.INPUT, builder.addExternal(101))); // toggle
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(102))); // out
        builder.addTransistor("200", 110, 111, net_vss);
        builder.addTransistor("201", 111, 110, net_vss);
        builder.addTransistor("202", 100, 110, 112);
        builder.addTransistor("203", 100, 112, 113);
        builder.addTransistor("204", 113, 114, net_vss);
        builder.addTransistor("205", 101, 114, 110);
        builder.addTransistor("206", 100, 111, 115);
        builder.addTransistor("207", 100, 115, 116);
        builder.addTransistor("208", 116, 117, net_vss);
        builder.addTransistor("209", 101, 117, 111);
        builder.addTransistor("210", 111, 102, net_vss);
        builder.addTransistor("211", 110, 102, net_vcc);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        return new Module("latchToggle", builder.getGraph(), ports);
    }

    private Module latchToggleModule(boolean incNQ) {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("P", EdgeType.INPUT, builder.addExternal(100))); // precharge
        ports.add(new ModulePort("T", EdgeType.INPUT, builder.addExternal(101))); // toggle
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(110))); // out
        if (incNQ) {
            ports.add(new ModulePort("NQ", EdgeType.OUTPUT, builder.addExternal(111))); // out
        }
        builder.addTransistor("200", 110, 111, net_vss);
        builder.addTransistor("201", 111, 110, net_vss);
        builder.addTransistor("202", 100, 110, 112);
        builder.addTransistor("203", 100, 112, 113);
        builder.addTransistor("204", 113, 114, net_vss);
        builder.addTransistor("205", 101, 114, 110);
        builder.addTransistor("206", 100, 111, 115);
        builder.addTransistor("207", 100, 115, 116);
        builder.addTransistor("208", 116, 117, net_vss);
        builder.addTransistor("209", 101, 117, 111);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        return new Module("latchToggle", builder.getGraph(), ports);
    }

    private Module clockedRSLatchModule(boolean incQ, boolean incNQ) {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        if (incNQ) {
            ports.add(new ModulePort("NQ", EdgeType.OUTPUT, builder.addExternal(100))); // output
        }
        if (incQ) {
            ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(101))); // output
        }
        ports.add(new ModulePort("NS", EdgeType.INPUT, builder.addExternal(102))); // set
        ports.add(new ModulePort("NR", EdgeType.INPUT, builder.addExternal(103))); // reset
        ports.add(new ModulePort("CLK", EdgeType.INPUT, builder.addExternal(104))); // clock
        builder.addTransistor("200", 100, 101, net_vss);
        builder.addTransistor("201", 101, 100, net_vss);
        builder.addTransistor("202", 104, 102, 100);
        builder.addTransistor("203", 104, 103, 101);
        builder.addDPullup("1", 100);
        builder.addDPullup("2", 101);
        return new Module("clockedRSLatch", builder.getGraph(), ports);
    }

    private Module clockedRSLatchPPModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort("NS", EdgeType.INPUT, builder.addExternal(101))); // set
        ports.add(new ModulePort("NR", EdgeType.INPUT, builder.addExternal(102))); // reset
        ports.add(new ModulePort("CLK", EdgeType.INPUT, builder.addExternal(103))); // clock
        builder.addTransistor("200", 111, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 103, 101, 110);
        builder.addTransistor("203", 103, 102, 111);
        builder.addTransistor("204", 110, 100, net_vss);
        builder.addTransistor("205", 111, 100, net_vcc);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        return new Module("clockedRSLatchPP", builder.getGraph(), ports);
    }

    private Module setResetLatchModule(boolean incQ, boolean incNQ) {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        if (incNQ) {
            ports.add(new ModulePort("NQ", EdgeType.OUTPUT, builder.addExternal(100))); // output
        }
        if (incQ) {
            ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(101))); // output
        }
        ports.add(new ModulePort("S", EdgeType.INPUT, builder.addExternal(102))); // set
        ports.add(new ModulePort("R", EdgeType.INPUT, builder.addExternal(103))); // reset
        builder.addTransistor("200", 100, 101, net_vss);
        builder.addTransistor("201", 101, 100, net_vss);
        builder.addTransistor("202", 102, 100, net_vss);
        builder.addTransistor("203", 103, 101, net_vss);
        builder.addDPullup("1", 100);
        builder.addDPullup("2", 101);
        return new Module("RSLatch", builder.getGraph(), ports);
    }

    private Module setResetLatchPPModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort("S", EdgeType.INPUT, builder.addExternal(101))); // set
        ports.add(new ModulePort("R", EdgeType.INPUT, builder.addExternal(102))); // reset
        builder.addTransistor("200", 111, 110, net_vss);
        builder.addTransistor("201", 110, 111, net_vss);
        builder.addTransistor("202", 101, 110, net_vss);
        builder.addTransistor("203", 102, 111, net_vss);
        builder.addTransistor("204", 110, 100, net_vss);
        builder.addTransistor("205", 111, 100, net_vcc);
        builder.addDPullup("1", 110);
        builder.addDPullup("2", 111);
        return new Module("RSLatchPP", builder.getGraph(), ports);
    }

    // private Module commonNANDModule() {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
    // ports.add(new ModulePort("O1", EdgeType.OUTPUT,
    // builder.addExternal(100))); // output
    // ports.add(new ModulePort("O2", EdgeType.OUTPUT,
    // builder.addExternal(101))); // output
    // ports.add(new ModulePort("A1", EdgeType.INPUT,
    // builder.addExternal(102))); // inputA
    // ports.add(new ModulePort("A2", EdgeType.INPUT,
    // builder.addExternal(103))); // inputB
    // ports.add(new ModulePort("B", EdgeType.INPUT, builder.addExternal(104)));
    // // inputC(common)
    // builder.addTransistor("200", 102, 100, 110);
    // builder.addTransistor("201", 103, 101, 110);
    // builder.addTransistor("202", 104, 110, net_vss);
    // builder.addDPullup("1", 100);
    // builder.addDPullup("2", 101);
    // return new Module("commonNAND", builder.getGraph(), ports);
    // }

    private Module crossCoupledTransistors1Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort("A", EdgeType.INPUT, builder.addExternal(101))); // inputA
        ports.add(new ModulePort("B", EdgeType.INPUT, builder.addExternal(102))); // inputB
        builder.addTransistor("200", 101, 102, 100);
        builder.addTransistor("201", 102, 101, 100);
        builder.addDPullup("1", 100);
        return new Module("crossCoupledTransistors1", builder.getGraph(), ports);
    }

    public Module crossCoupledTransistors2Module() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("Q", EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort("NQ", EdgeType.OUTPUT, builder.addExternal(101))); // output
        builder.addTransistor("200", 101, 100, net_vss);
        builder.addTransistor("201", 100, 101, net_vss);
        builder.addDPullup("1", 100);
        builder.addDPullup("2", 101);
        return new Module("crossCoupledTransistors2", builder.getGraph(), ports);
    }

    private Module pushPullModule() {
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("O", EdgeType.BIDIRECTIONAL, builder.addExternal(100))); // output
        ports.add(new ModulePort("IL", EdgeType.INPUT, builder.addExternal(101))); // input
        ports.add(new ModulePort("IH", EdgeType.INPUT, builder.addExternal(102))); // input
        builder.addTransistor("200", 101, 100, net_vss);
        builder.addTransistor("201", 102, 100, net_vcc);
        return new Module("pushPull", builder.getGraph(), ports);
    }

    private Module z80ABPinDriverModule() {
        // See http://baltazarstudios.com/anatomy-z80-gate/
        List<ModulePort> ports = new LinkedList<ModulePort>();
        CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
        ports.add(new ModulePort("AB", EdgeType.OUTPUT, builder.addExternal(100))); // output
        ports.add(new ModulePort("I", EdgeType.INPUT, builder.addExternal(1018))); // +input
        ports.add(new ModulePort("NI", EdgeType.INPUT, builder.addExternal(1019))); // -input
        ports.add(new ModulePort("OE", EdgeType.INPUT, builder.addExternal(627))); // oe
        // Push/Pull output stage
        builder.addTransistor("200", 2759, 100, net_vcc);
        builder.addTransistor("201", 2754, 100, net_vss);
        // High-side driver
        builder.addTransistor("202", 2753, 2754, net_vcc);
        builder.addTransistor("203", 1018, 2754, net_vss);
        builder.addTransistor("204", 1018, 2753, net_vss);
        builder.addTransistor("205", 627, 2753, net_vss);
        builder.addTransistor("206", 627, 2754, net_vss);
        builder.addDPullup("1", 2753);
        // Low-side driver
        builder.addTransistor("212", 2718, 2759, net_vcc);
        builder.addTransistor("213", 1019, 2759, net_vss);
        builder.addTransistor("214", 1019, 2718, net_vss);
        builder.addTransistor("215", 627, 2718, net_vss);
        builder.addTransistor("216", 627, 2759, net_vss);
        builder.addDPullup("2", 2718);
        return new Module("abPinDriver", builder.getGraph(), ports);
    }

    // private Module pass8Module() {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
    // ports.add(new ModulePort("A0", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(100))); // A0
    // ports.add(new ModulePort("A1", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(101))); // A1
    // ports.add(new ModulePort("A2", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(102))); // A2
    // ports.add(new ModulePort("A3", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(103))); // A3
    // ports.add(new ModulePort("A4", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(104))); // A4
    // ports.add(new ModulePort("A5", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(105))); // A5
    // ports.add(new ModulePort("A6", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(106))); // A6
    // ports.add(new ModulePort("A7", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(107))); // A7
    // ports.add(new ModulePort("B0", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(110))); // B0
    // ports.add(new ModulePort("B1", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(111))); // B1
    // ports.add(new ModulePort("B2", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(112))); // B2
    // ports.add(new ModulePort("B3", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(113))); // B3
    // ports.add(new ModulePort("B4", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(114))); // B4
    // ports.add(new ModulePort("B5", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(115))); // B5
    // ports.add(new ModulePort("B6", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(116))); // B6
    // ports.add(new ModulePort("B7", EdgeType.BIDIRECTIONAL,
    // builder.addExternal(117))); // B7
    // ports.add(new ModulePort("G", EdgeType.INPUT, builder.addExternal(120)));
    // // input
    // builder.addTransistor("200", 120, 100, 110);
    // builder.addTransistor("201", 120, 101, 111);
    // builder.addTransistor("202", 120, 102, 112);
    // builder.addTransistor("203", 120, 103, 113);
    // builder.addTransistor("204", 120, 104, 114);
    // builder.addTransistor("205", 120, 105, 115);
    // builder.addTransistor("206", 120, 106, 116);
    // builder.addTransistor("207", 120, 107, 117);
    // return new Module("pass8", builder.getGraph(), ports);
    // }

    public List<Module> getBasicModules() {
        List<Module> list = new LinkedList<Module>();
        // Register file
        list.add(regfileSliceModule());
        // Super buffers
        // list.add(superComplementaryModule()); // only used in reg file
        // list.add(superPushPullModule());
        list.add(superNORModule());
        list.add(superNANDModule());
        list.add(superInvertorModule());
        list.add(superBufferModule());
        // Complex gates
        list.add(xor2Module());
        list.add(xnor2Module());
        // Latches
        list.add(latchTogglePPModule());
        list.add(latchToggleModule(false));
        list.add(latchToggleModule(true));
//        list.add(latchPassModule(true, false));
//        list.add(latchPassModule(false, true));
//        list.add(latchPassModule(true, true));
//        list.add(latchModule(true, false));
//        list.add(latchModule(false, true));
//        list.add(latchModule(true, true));
        // Storage modules
        list.add(storage2GaModule());
        list.add(storage2GbModule());
        list.add(storage1GModule());
        // list.add(clockedRSLatchPPModule());
        // list.add(clockedRSLatchModule(true, false)); // breaks at this point
        // list.add(clockedRSLatchModule(false, true));
        // list.add(clockedRSLatchModule(true, true));
        // list.add(setResetLatchPPModule());
        // list.add(setResetLatchModule(true, false));
        // list.add(setResetLatchModule(false, true));
        // list.add(setResetLatchModule(true, true));
        return list;
    }

    public List<Module> getAllModules() {
        List<Module> list = new LinkedList<Module>();
        // Complex modules
        list.add(z80ABPinDriverModule());
        list.add(z80IRLatchModule());
        list.add(z80DBLatchModule());
        // Storage modules
        list.add(storage2GaModule());
        list.add(storage2GbModule());
        list.add(storage1GModule());
        // Combinatorial
        list.add(xor2Module());
        list.add(xnor2Module());
        list.add(superNORModule());
        list.add(superNANDModule());
        list.add(superComplementaryModule());
        list.add(superInvertorModule());
        list.add(superBufferModule());
        // Latches / Registers
        list.add(registerModule());
        list.add(latchPassModule(true, false));
        list.add(latchPassModule(false, true));
        list.add(latchPassModule(true, true));
        list.add(latchModule(true, false));
        list.add(latchModule(false, true));
        list.add(latchModule(true, true));
        list.add(clockedRSLatchPPModule());
        list.add(clockedRSLatchModule(true, false));
        list.add(clockedRSLatchModule(false, true));
        list.add(clockedRSLatchModule(true, true));
        list.add(setResetLatchPPModule());
        list.add(setResetLatchModule(true, false));
        list.add(setResetLatchModule(false, true));
        list.add(setResetLatchModule(true, true));
        // Mux modules
        // for (int n = 8; n >= 2; n--) {
        // list.add(muxModule(n));
        // }
        list.add(crossCoupledTransistors1Module());
        // list.add(crossCoupledTransistors2Module());
        list.add(pushPullModule());
        // list.add(pass8Module()); // Search blows up
        return list;
    }

    // private Module Module() {
    // List<ModulePort> ports = new LinkedList<ModulePort>();
    // CircuitGraphBuilder builder = new CircuitGraphBuilder(net_vss, net_vcc);
    // return new Module("", builder.getGraph(), ports);
    // }

}
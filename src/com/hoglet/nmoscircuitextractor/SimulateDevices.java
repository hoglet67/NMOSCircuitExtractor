package com.hoglet.nmoscircuitextractor;

public class SimulateDevices {

    public static final int W = 6;
    public static final int WMASK = (1 << W) - 1;

    public static final int HI = 3 << (W - 3);
    public static final int LO = -(3 << (W - 3));

    public static void rangeCheckSigned(String s, int i, int w) {
        int min = -(1 << (w - 1));
        int max = (1 << (w - 1)) - 1;
        if (i < min || i > max) {
            throw new RuntimeException(s + ": out of signed range: (" + min + " .. " + max + "):" + i);
        }
    }

    public static void rangeCheckUnsigned(String s, int i, int w) {
        int min = 0;
        int max = (1 << w) - 1;
        if (i < min || i > max) {
            throw new RuntimeException(s + ": out of unsigned range: (" + min + " .. " + max + "):" + i);
        }
    }

    public static int abs(int i) {
        return i >= 0 ? i : -i;
    }
    
    public static int sign(int i) {
        return i >= 0 ? 1 : -1;
    }

    // module transistor_pullup(input signed [`W-1:0] v, output signed [`W-1:0]
    // i);
    // wire signed [`W-1:0] hi = `HI;
    // wire signed [`W:0] dv = {hi[`W-1],hi} - {v[`W-1],v};
    // assign i = {{3{dv[`W]}},dv[`W:4]};
    // endmodule

    public static int transistor_pullup(String id, int v) {
        int hi = HI;
        int dv = hi - v;
        int i = (dv >> (W - 2));
        rangeCheckSigned("pullup " + id + ": i", i, W);
        return i;
    }

    // module transistor_nmos(input g, input signed [`W-1:0] vs,vd, output
    // signed [`W-1:0] is,id);
    // wire signed [`W:0] vsd = {vd[`W-1],vd} - {vs[`W-1],vs};
    // wire signed [`W-1:0] isd = {vsd[`W],vsd[`W:2]};
    // wire signed [`W-1:0] i = g ? isd : 0;
    // assign is = i;
    // assign id = -i;
    // endmodule

    public static int transistor_nmos(String id, boolean g, int vd, int vs) {
        int vsd = vd - vs;
        rangeCheckSigned("transistor " + id + ": vsd", vsd, W + 1);
        int isd = abs(vsd) >> 1;
        rangeCheckSigned("transistor " + id + ": isd", isd, W);
        int i = g ? isd : 0;
        rangeCheckSigned("transistor " + id + ": i", i, W);
        return sign(vsd) * i; // returns is
    }

    // module transistor_nmos_vss(input g, input signed [`W-1:0] vd, output
    // signed [`W-1:0] id);
    // wire signed [2:0] lo = 3'b110;
    // wire signed [2:0] vdtop = lo - {vd[`W-1],vd[`W-1:`W-2]};
    // wire signed [`W:0] vsd = {vdtop,~vd[`W-3:0]};
    // wire signed [`W-1:0] i = {vsd[`W],vsd[`W:2]};
    // assign id = g ? i : `W'd0;
    // endmodule

    public static int transistor_nmos_vss(String id, boolean g, int vd) {
        int low = -2;
        int vdtrunc = (vd >> (W - 2));
        rangeCheckSigned("transistor " + id + ": vdtrunc", vdtrunc, 2);
        int vdtop = low - vdtrunc;
        rangeCheckSigned("transistor " + id + ": vdtop", vdtop, 3);
        // Temporarily work with unsigned, as it's easier to mimic the verilog
        int vsd = ((vdtop & 7) << (W - 2)) | ((vd & ((1 << (W - 2)) - 1)) ^ ((1 << (W - 2)) - 1));
        rangeCheckUnsigned("transistor " + id + ": vsd", vsd, W + 1);
        // Convert back to W + 1 bit signed
        if (vsd >= (1 << W)) {
            vsd = vsd - (2 << W);
        }
        rangeCheckSigned("transistor " + id + ": vsd", vsd, W + 1);
        int i = vsd >> 2;
        rangeCheckSigned("transistor " + id + ": i", i, W);
        // System.out.print(String.format(": low = %3d; vdtop = %3d; vsd = %3d;
        // i = %3d", low, vdtop, vsd, i));
        return g ? i : 0;
    }

    // module net_node_2(input eclk, input erst, input signed [`W-1:0] i0, i1,
    // output reg signed [`W-1:0] v);
    // wire signed [`W-1:0] i = i0+i1;
    // always @(posedge eclk)
    // if (erst)
    // v <= `LO2;
    // else
    // v <= v + i;
    // endmodule

    public static int net_node_2(String id, int v, int i0, int i1) {
        int isum = i0 + i1;
        rangeCheckSigned("node " + id + ": isum", isum, W);
        int vsum = v + isum;
        rangeCheckSigned("node " + id + ": vsum", vsum, W);
        return vsum;
    }

    // Model A:
    // - Pullup
    // - Transistor
    // - Transistor
    // - Transistor connected to VCC
    public static void run_model_A(int v1, int v2, int v3, boolean reverse) {

        System.out.println("Running model A: reverse = " + reverse);

        int iteration = 1;

        while (true) {
            int i1, i2, i3, i4;
            int is2, id2, is3, id3;

            System.out.print(String.format("%3d : v1 = %3d; v2 = %3d; v3 = %3d", iteration, v1, v2, v3));

            i1 = transistor_pullup("t1", v1);

            if (reverse) {
                // connected backwards
                // label, g, d, s (vd < vs)
                i2 = transistor_nmos("t2", true, v2, v1);
                i3 = transistor_nmos("t3", true, v3, v2);
                id2 = i2;
                is2 = -i2;
                id3 = i3;
                is3 = -i3;
            } else {
                // connected forwards
                // label, g, d, s (vd > vs)
                i2 = transistor_nmos("t2", true, v1, v2);
                i3 = transistor_nmos("t3", true, v2, v3);
                id2 = -i2;
                is2 = i2;
                id3 = -i3;
                is3 = i3;
            }

            // i4 = transistor_nmos_vss("t4", true, v3);

            i4 = -transistor_nmos("t4", true, v3, LO);

            System.out.println(String.format(" : i1 = %3d; i2 = %3d; i3 = %3d; i4 = %3d", i1, i2, i3, i4));

            int v1_next = net_node_2("n1", v1, i1, id2);
            int v2_next = net_node_2("n2", v2, is2, id3);
            int v3_next = net_node_2("n3", v3, is3, i4);

            if ((v1 == v1_next) && (v2 == v2_next) && (v3 == v3_next)) {
                break;
            }
            v1 = v1_next;
            v2 = v2_next;
            v3 = v3_next;

            iteration++;
        }

        System.out.println("Model A converged after " + iteration + " iterations");
        System.out.println();
    }

    public static void run_model_A(int v, boolean reverse) {
        run_model_A(v, v, v, reverse);
    }

    public static void main(String args[]) {

        for (int i = 0; i < 2; i++) {
            // Test with node set as per an example captured from the simulation
            run_model_A(11, 11, -17, i == 0);
            // Test with all nodes initially set to Hi
            run_model_A(HI, i == 0);
            // Test with all nodes initially set to Lo
            run_model_A(LO, i == 0);
        }
    }
}

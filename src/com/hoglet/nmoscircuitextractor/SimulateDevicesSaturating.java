package com.hoglet.nmoscircuitextractor;

public class SimulateDevicesSaturating {

    public static final int W = 6;
    public static final int WMASK = (1 << W) - 1;

    public static final int W_MAX = (1 << (W - 1)) - 1;
    public static final int W_MIN = -(1 << (W - 1));

    public static final int HI = 3 << (W - 3);
    public static final int LO = -(3 << (W - 3));

    public static final int PULLUP_STRENGTH = 2;
    public static final int PULLDOWN_STRENGTH = 3;

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

    public static int transistor_pullup(String id, int v) {
        int i = (v < HI) ? PULLUP_STRENGTH : 0;
        rangeCheckSigned("pullup " + id + ": i", i, W);
        return i;
    }

    public static int transistor_nmos(String id, boolean g, int vd, int vs) {
        int isd = 0;
        if (vd > vs) {
            isd = PULLDOWN_STRENGTH;
        } else if (vd < vs) {
            isd = -PULLDOWN_STRENGTH;
        }
        rangeCheckSigned("transistor " + id + ": isd", isd, W - 1);
        int i = g ? isd : 0;
        rangeCheckSigned("transistor " + id + ": i", i, W);
        return i; // returns is
    }

    public static int transistor_nmos_vss(String id, boolean g, int vd) {
        int i = (vd > LO) ? -PULLDOWN_STRENGTH : 0;
        rangeCheckSigned("transistor " + id + ": i", i, W);
        return g ? i : 0;
    }

    public static int net_node_2(String id, int v, int i0, int i1) {
        int isum = i0 + i1;
        rangeCheckSigned("node " + id + ": isum", isum, W);
        int vsum = v + isum;
        if (vsum > W_MAX) {
            vsum = W_MAX;
        } else if (vsum < W_MIN) {
            vsum = W_MIN;
        }
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

            i4 = transistor_nmos_vss("t4", true, v3);

            System.out.println(String.format(" : i1 = %3d; i2 = %3d; i3 = %3d; i4 = %3d", i1, i2, i3, i4));

            int v1_next = net_node_2("n1", v1, i1, id2);
            int v2_next = net_node_2("n2", v2, is2, id3);
            int v3_next = net_node_2("n3", v3, is3, i4);

            if (((v1 == v1_next) && (v2 == v2_next) && (v3 == v3_next)) || iteration == 500) {
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

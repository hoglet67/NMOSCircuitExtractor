// Models for transistors and nodes
//
// Copyright (c) 2010 Peter Monta

`include "common_saturating.v"

`define PULLUP_STRENGTH 2
`define PULLDOWN_STRENGTH 3

module pad_input(input p, input signed [`W-1:0] v, output signed [`W-1:0] i);
   wire signed [`W-1:0] vhi = `HI;
   wire signed [`W-1:0] vlo = `LO;
   assign i = p ? ((v < vhi) ? `PULLUP_STRENGTH : 0) : ((v > vlo) ? -`PULLDOWN_STRENGTH : 0);
endmodule

module pad_output(output p, input signed [`W-1:0] v);
  assign p = ~v[`W-1];
endmodule

module pad_bidirectional(input p_i, output p_o, output p_t, input signed [`W-1:0] v, output signed [`W-1:0] i);
   wire signed [`W-1:0] vhi = `HI;
   wire signed [`W-1:0] vlo = `LO;
   assign p_o = ~v[`W-1];
   assign i = p_i ? ((v < vhi) ? `PULLUP_STRENGTH : 0) : ((v > vlo) ? -`PULLDOWN_STRENGTH : 0);
   assign p_t = 0; //fixme
endmodule

module transistor_nmos(input g, input signed [`W-1:0] vs,vd, output signed [`W-1:0] is,id);
   wire signed [`W-1:0] isd = (vd > vs) ? `PULLDOWN_STRENGTH : (vd < vs) ? -`PULLDOWN_STRENGTH : 0;
   wire signed [`W-1:0] i = g ? isd : 0;
   assign is = i;
   assign id = -i;
endmodule

module transistor_nmos_vcc(input g, input signed [`W-1:0] vs, output signed [`W-1:0] is);
   wire signed [`W-1:0] vhi = `HI;
   wire signed [`W-1:0] isd = (vs < vhi) ? `PULLUP_STRENGTH : 0;
   assign is = g ? isd : 0;
endmodule

module transistor_nmos_vss(input g, input signed [`W-1:0] vd, output signed [`W-1:0] id);
   wire signed [`W-1:0] vlo = `LO;
   wire signed [`W-1:0] i = (vd > vlo) ? -`PULLDOWN_STRENGTH : 0;
   assign id = g ? i : 0;
endmodule

module transistor_pullup(input signed [`W-1:0] v, output signed [`W-1:0] i);
   wire signed [`W-1:0] vhi = `HI;
   assign i = (v < vhi) ? `PULLUP_STRENGTH : 0;
endmodule

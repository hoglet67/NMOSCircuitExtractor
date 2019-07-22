// Models for transistors and nodes
//
// Copyright (c) 2010 Peter Monta

`include "common_monta.v"

module pad_input(input p, input signed [`W-1:0] v, output signed [`W-1:0] i);
  wire [`W-1:0] vp = p ? `HI : `LO;
  wire [`W:0] dv = {vp[`W-1],vp} - {v[`W-1],v};
  assign i = {{2{dv[`W]}},dv[`W:3]};
endmodule

module pad_output(output p, input signed [`W-1:0] v);
  assign p = ~v[`W-1];
endmodule

module pad_bidirectional(input p_i, output p_o, output p_t, input signed [`W-1:0] v, output signed [`W-1:0] i);
  assign p_o = ~v[`W-1];
  wire [`W-1:0] vp = p_i ? `HI : `LO;
  wire [`W:0] dv = {vp[`W-1],vp} - {v[`W-1],v};
  assign i = {{3{dv[`W]}},dv[`W:4]};
  assign p_t = 0; //fixme
endmodule

module transistor_nmos(input g, input signed [`W-1:0] vs,vd, output signed [`W-1:0] is,id);
  wire signed [`W:0] vsd = {vd[`W-1],vd} - {vs[`W-1],vs};
  wire signed [`W-1:0] isd = {vsd[`W],vsd[`W:2]};
  wire signed [`W-1:0] i = g ? isd : 0;
  assign is = i;
  assign id = -i;
endmodule

module transistor_nmos_vcc(input g, input signed [`W-1:0] vs, output signed [`W-1:0] is);
  wire signed [`W-1:0] vd = `HI;
  wire signed [`W:0] vsd = {vd[`W-1],vd} - {vs[`W-1],vs};
  wire signed [`W-1:0] isd = {vsd[`W],vsd[`W:2]};
  wire signed [`W-1:0] i = g ? isd : 0;
  assign is = i;
endmodule

module transistor_nmos_vss(input g, input signed [`W-1:0] vd, output signed [`W-1:0] id);
  wire signed [`W-1:0] vs = `LO;
  wire signed [`W:0] vsd = {vd[`W-1],vd} - {vs[`W-1],vs};
  wire signed [`W-1:0] isd = {vsd[`W], vsd[`W:2]};
  wire signed [`W-1:0] i = g ? isd : 0;
  assign id = -i;
endmodule

module transistor_pullup(input signed [`W-1:0] v, output signed [`W-1:0] i);
  wire signed [`W-1:0] hi = `HI;
  wire signed [`W:0] dv = {hi[`W-1],hi} - {v[`W-1],v};
  assign i = {{3{dv[`W]}},dv[`W:4]};
endmodule

module transistor_function_init0(input eclk, input erst, input i, output reg o);
   always @(posedge eclk)
      if (erst)
        o <= 1'b0;
      else
        o <= ~i;
endmodule

module transistor_function_init1(input eclk, input erst, input i, output reg o);
   always @(posedge eclk)
      if (erst)
        o <= 1'b1;
      else
        o <= ~i;
endmodule

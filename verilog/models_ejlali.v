`include "common_ejlali.v"

// Encoding:
//
// 00x - undriven
// 010 - floating 1
// 011 - floating 0
// 100 - weak 1
// 101 - weak 0
// 110 - strong 1
// 111 - strong 0
//
// These are carefully chosen so the largest value wins a conflict

module pad_input(input pad, input [`W-1:0] internal_in, output [`W-1:0] internal_out);
   assign internal_out = { `S_STRONG, (pad ? `L_HI : `L_LO) };
endmodule

module pad_output(output pad, input [`W-1:0] internal_in);
   assign pad = (|internal_in[`B_DRIVEN]) ? ((internal_in[`B_LEVEL] == `L_HI) ? 1'b1 : 1'b0) : 1'bz;
endmodule

module pad_bidirectional(input pad_i, output pad_o, output pad_t, input [`W-1:0] internal_in, output [`W-1:0] internal_out);
   pad_input pad_in (pad_i, internal_in, internal_out);
   pad_output pad_out (pad_o, internal_in);
   assign pad_t = (|internal_in[`B_DRIVEN]);
endmodule

module transistor_nmos(input g, input [`W-1:0] c1in, c2in, output reg [`W-1:0] c1out, c2out);

  always @(*)
    begin
       if (g) begin
          c1out = c2in;
          c2out = c1in;
       end else begin
          c1out = { `S_OFF, `L_LO };
          c2out = { `S_OFF, `L_LO };
       end
    end

endmodule // transistor_nmos

module transistor_nmos_vss(input g, input [`W-1:0] c1in, output reg [`W-1:0] c1out);

  always @(*)
    begin
       if (g) begin
          c1out = { `S_STRONG, `L_LO };
       end else begin
          c1out = { `S_OFF, `L_LO };
       end
    end

endmodule // transistor_nmos_vss

module transistor_nmos_vcc(input g, input [`W-1:0] c1in, output reg [`W-1:0] c1out);

  always @(*)
    begin
       if (g) begin
          c1out = { `S_STRONG, `L_HI };
       end else begin
          c1out = { `S_OFF, `L_LO };
       end
    end

endmodule // transistor_nmos_vcc


module transistor_pullup(input [`W-1:0] c1in, output [`W-1:0] c1out);
   assign c1out = { `S_WEAK, `L_HI };
endmodule // transistor_pullup

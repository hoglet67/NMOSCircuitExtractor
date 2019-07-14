// Input Encoding:
//
// 00 - not expected
// 01 - logic 0
// 10 - logic 1
// 11 - not expected
//
// Output Encoding:
//
// 00 - undriven
// 01 - logic 0
// 10 - logic 1
// 11 - not expected
//


module pad_input(input pad, output [`W-1:0] internal);
   assign internal = {pad, ~pad};
endmodule

module pad_output(output pad, input [`W-1:0] internal_in);
   assign pad = (internal_in == 2'b10) ? 1'b1 : (internal_in == 2'b01) ? 1'b0 : 1'bz;
endmodule

module pad_bidirectional(input pad_i, output pad_o, output pad_t, input [`W-1:0] internal_in, output [`W-1:0] internal_out);
   pad_input pad_in (pad_i, internal_out);
   pad_output pad_out (pad_o, internal_in);
   assign pad_t = 0; // fixme
endmodule



module transistor_nmos(input g, input [`W-1:0] c1in, c2in, output reg [`W-1:0] c1out, c2out);

  always @(*)
    begin
       if (g) begin
//          if (c1in == 2'b01 || c2in == 2'b01) begin
//             c1out = 2'b01;
//             c2out = 2'b01;
//          end else begin
//             c1out = 2'b10;
//             c2out = 2'b10;
//          end

          c1out = c1in + c2in;
          c2out = c1in - c2in;

       end else begin
          c1out = 2'b00;
          c2out = 2'b00;
       end
    end

endmodule // transistor_nmos

module transistor_nmos_vss(input g, input [`W-1:0] c1in, output reg [`W-1:0] c1out);

  always @(*)
    begin
       if (g) begin
          c1out = 2'b01;
       end else begin
          c1out = 2'b00;
       end
    end

endmodule // transistor_nmos_vss

module transistor_nmos_vcc(input g, input [`W-1:0] c1in, output reg [`W-1:0] c1out);

  always @(*)
    begin
       if (g) begin
          c1out = 2'b00;
       end else begin
          c1out = 2'b10;
       end
    end

endmodule // transistor_nmos_vcc


module transistor_pullup(input [`W-1:0] c1in, output [`W-1:0] c1out);
   assign c1out = 2'b10;
endmodule // transistor_pullup


//module net_node_2(input eclk, input erst, input [`W-1:0] i0, i1, output [`W-1:0] out);
//   wire sel = (|i0) | (|i1);
//   wire val = ~(i0[0] | i1[0]);
//   always @(posedge eclk) begin
//      if erst = 0 begin
//         out <= 2'b01;
//      else if sel begin
//         out <= { val, ~val};
//      end
//   end
//endmodule // net_node_2

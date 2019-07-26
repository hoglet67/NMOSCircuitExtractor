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

module pushPull(input eclk, input erst, input IH, input IL, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= (IH & ~IL);
endmodule

module superBuffer(input eclk, input erst, input I, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= I;
endmodule

module superInverter(input eclk, input erst, input I, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= ~I;
endmodule

module superComplementary(input eclk, input erst, input I, output reg O1, output reg O2);
   always @(posedge eclk)
     if (erst) begin
        O1 <= 1'b1;
        O2 <= 1'b0;
     end else begin
        O1 <= ~I;
        O2 <= I;
     end
endmodule

module superNAND(input eclk, input erst, input I1, input I2, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= ~(I1 & I2);
endmodule

module superNOR(input eclk, input erst, input I1, input I2, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= ~(I1 | I2);
endmodule

module superNORAlt(input eclk, input erst, input I1, input I2, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= ~(I1 | I2);
endmodule

module storage1G(input eclk, input erst, input D, input G, output reg Q);
   always @(posedge eclk)
     if (erst)
       Q <= 1'b0;
     else if (G)
       Q <= D;
endmodule

// Same as 1G
module storage2Ga(input eclk, input erst, input D, input G, output reg Q);
   always @(posedge eclk)
     if (erst)
       Q <= 1'b0;
     else if (G)
       Q <= D;
endmodule

module storage2Gb(input eclk, input erst, input D, input G1, input G2, output reg Q);
   always @(posedge eclk)
     if (erst)
       Q <= 1'b0;
     else if (G1 & G2)
       Q <= D;
endmodule


module regfileSlice
  (
   input      eclk,
   input      erst,
   input      pc_din,
   input      pc_wr,
   input      r_p,
   input      r_x1, // not used
   input      clk,  // not used
   input      reg_din,
   input      reg_wr,
   input      regselpc,
   input      regselir,
   input      regselwz,
   input      regselsp,
   input      regseliy,
   input      regselix,
   input      regselhl1,
   input      regselhl0,
   input      regselde1,
   input      regselde0,
   input      regselbc1,
   input      regselbc0,
   input      regselaf1,
   input      regselaf0,
   output reg reg_dout,
   output reg pc_dout
   );

   reg [13:0] regs;
   reg        ldata;
   reg        rdata;

   wire [13:0] sel = { regselaf0, regselaf1, regselbc0, regselbc1, regselde0, regselde1, regselhl0, regselhl1, regselix, regseliy, regselsp, regselwz, regselir, regselpc};

   integer     i1, i2, i3;

   // Work out the value on the left data bus

   always @(*) begin
      ldata = 1;
      if (r_p) begin
         // Left and Right busses are joined
         if (pc_wr & !pc_din)
           ldata = 0;
         if (reg_wr & !reg_din)
           ldata = 0;
         if (!pc_wr & !reg_wr)
           for (i1 = 0; i1 < 14; i1 = i1 + 1)
             if (sel[i1] & !regs[i1])
               ldata = 0;
      end else begin
         // Left and Right busses are split
         if (pc_wr & !pc_din)
           ldata = 0;
         if (!pc_wr)
           for (i1 = 0; i1 < 2; i1 = i1 + 1)
             if (sel[i1] & !regs[i1])
               ldata = 0;
      end
   end

   // Work out the value on the right data bus

   always @(*) begin
      rdata = 1;
      if (r_p) begin
         // Left and Right busses are joined
         if (pc_wr & !pc_din)
           rdata = 0;
         if (reg_wr & !reg_din)
           rdata = 0;
         if (!pc_wr & !reg_wr)
           for (i2 = 0; i2 < 14; i2 = i2 + 1)
             if (sel[i2] & !regs[i2])
               rdata = 0;
      end else begin
         // Left and Right busses are split
         if (reg_wr & !reg_din)
           rdata = 0;
         if (!reg_wr)
           for (i2 = 2; i2 < 14; i2 = i2 + 1)
             if (sel[i2] & !regs[i2])
               rdata = 0;
      end
   end


   always @(posedge eclk)
     if (erst) begin
        pc_dout <= 1;
        reg_dout <= 0;
        regs <= 0;
     end else begin
        pc_dout <= ~ldata;
        reg_dout <= rdata;
        if (pc_wr | (r_p & reg_wr))
          for (i3 = 0; i3 < 2; i3 = i3 + 1)
            if (sel[i3])
              regs[i3] <= ldata;
        if (reg_wr | (r_p & pc_wr))
          for (i3 = 2; i3 < 14; i3 = i3 + 1)
            if (sel[i3])
              regs[i3] <= rdata;
     end


  endmodule // storage2Gb

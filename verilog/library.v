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

module xor2(input eclk, input erst, input A, input B, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= A ^ B;
endmodule

module xnor2(input eclk, input erst, input A, input B, output reg O);
   always @(posedge eclk)
     if (erst)
       O <= 1'b0;
     else
       O <= A ~^ B;
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

module superPushPull(input eclk, input erst, input I, output reg O);
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

module abPinDriver(input eclk, input erst, input I, input NI, input OE, output reg AB);
   // TODO: implement tri state
   always @(posedge eclk)
     if (erst)
       AB <= 1'b0;
     else
       AB <= I ? 1'b1 : 1'b0;
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


module latch(input eclk, input erst, input CLK, input D,  output Q, output NQ);
   reg latch;
   always @(posedge eclk)
     if (erst) begin
       latch <= 1'b0;
     end else begin
        if (!CLK)
          latch <= D;
     end
   assign Q = latch;
   assign NQ = !latch;
endmodule

module latchPass(input eclk, input erst, input WR, input NWR, input D,  output Q, output NQ);
   reg latch;
   always @(posedge eclk)
     if (erst) begin
       latch <= 1'b0;
     end else begin
        if (WR & !NWR)
          latch <= D;
     end
   assign Q = latch;
   assign NQ = !latch;
endmodule

module latchToggle(input eclk, input erst, input P, input T,  output Q, output NQ);
   reg latch;
   reg [5:0] T_last;
   // The precharge input P is not required, as we use an edge detector
   always @(posedge eclk)
     if (erst) begin
        latch <= 1'b0;
     end else begin
        T_last <= { T_last[5:0], T};
        if (T_last == 6'b000111)
          latch <= !latch;
     end
   assign Q = latch;
   assign NQ = !latch;
endmodule

//module RSLatch(input eclk, input erst, input S, input R, output reg Q, output reg NQ);
//   always @(posedge eclk)
//     if (erst) begin
//       Q <= 1'b0;
//       NQ <= 1'b1;
//     end else begin
//        if (S & !R)
//          {Q, NQ} <= 2'b10;
//        else if (R & !S)
//          {Q, NQ} <= 2'b01;
//        else if (S & R)
//          {Q, NQ} <= 2'b00;
//     end
//endmodule

module RSLatch(input eclk, input erst, input S, input R, output Q, output NQ);
   reg latch;
   always @(posedge eclk)
     if (erst) begin
       latch <= 1'b0;
     end else begin
        if (S & !R)
          latch <= 1'b1;
        else if (R & !S)
          latch <= 1'b0;
     end
   assign Q = latch & !R;
   assign NQ = !latch & !S;
endmodule

module clockedRSLatch(input eclk, input erst, input CLK, input NS, input NR, output Q, output NQ);
   wire R = CLK & !NR;
   wire S = CLK & !NS;
   RSLatch latch(eclk, erst, S, R, Q, NQ);
endmodule

module clockedRSLatchPP(input eclk, input erst, input CLK, input NS, input NR, output Q, output NQ);
   clockedRSLatch latch (eclk, erst, CLK, NS, NR, Q, NQ);
endmodule

module regfileSlice
  (
   input      eclk,
   input      erst,
   input      pc_din,
   input      pc_wr,
   input      r_p,
   input      r_x1, // not used
   input      clk, // not used
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

   // Work out the value on the left data bus

   always @(*) begin
      ldata = 1'b1;
      if (r_p) begin
         // Left and Right busses are joined
         if (pc_wr & !pc_din)
           ldata = 1'b0;
         if (reg_wr & !reg_din)
           ldata = 1'b0;
         if (!pc_wr & !reg_wr)
           if (|(sel[13:0] & ~regs[13:0]))
             ldata = 1'b0;
      end else begin
         // Left and Right busses are split
         if (pc_wr & !pc_din)
           ldata = 1'b0;
         if (!pc_wr)
           if (|(sel[1:0] & ~regs[1:0]))
             ldata = 1'b0;
      end
   end

   // Work out the value on the right data bus

   always @(*) begin
      rdata = 1'b1;
      if (r_p) begin
         // Left and Right busses are joined
         if (pc_wr & !pc_din)
           rdata = 1'b0;
         if (reg_wr & !reg_din)
           rdata = 1'b0;
         if (!pc_wr & !reg_wr)
           if (|(sel[13:0] & ~regs[13:0]))
             rdata = 1'b0;
      end else begin
         // Left and Right busses are split
         if (reg_wr & !reg_din)
           rdata = 1'b0;
         if (!reg_wr)
           if (|(sel[13:2] & ~regs[13:2]))
             rdata = 1'b0;
      end
   end


   always @(posedge eclk)
     if (erst) begin
        pc_dout <= 1'b1;
        reg_dout <= 1'b0;
        regs <= 14'b0;
     end else begin
        pc_dout <= ~ldata;
        reg_dout <= rdata;
        if (pc_wr | (r_p & reg_wr)) begin
           if (sel[0])
             regs[0] <= ldata;
           if (sel[1])
             regs[1] <= ldata;
        end
        if (reg_wr | (r_p & pc_wr)) begin
           if (sel[2])
             regs[2] <= rdata;
           if (sel[3])
             regs[3] <= rdata;
           if (sel[4])
             regs[4] <= rdata;
           if (sel[5])
             regs[5] <= rdata;
           if (sel[6])
             regs[6] <= rdata;
           if (sel[7])
             regs[7] <= rdata;
           if (sel[8])
             regs[8] <= rdata;
           if (sel[9])
             regs[9] <= rdata;
           if (sel[10])
             regs[10] <= rdata;
           if (sel[11])
             regs[11] <= rdata;
           if (sel[12])
             regs[12] <= rdata;
           if (sel[13])
             regs[13] <= rdata;
        end

     end

endmodule

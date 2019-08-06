// Top-level test SoC for z80 core, RAM, peripherals, clocks
//
// Copyright (c) 2010 Peter Monta

module main();
   wire eclk /*verilator public*/;
   wire ereset /*verilator public*/;

`ifndef verilator
   clk_reset_gen _clk_reset_gen(eclk, ereset);
`endif

   wire [15:0] ab /*verilator public*/;
   wire [7:0]  db_i;
   wire [7:0]  db_o;
   wire [7:0]  db_t;
   wire        _reset, _wait, _int, _nmi, _busrq;
   wire        _m1, _rd, _wr, _mreq, _iorq, _rfsh, _halt, _busak;
   wire        clk;

   reg         _rd_last;
   reg         _wr_last;
   reg         _reset_last;
   reg         _halt_last;

   assign _wait = 1;
   assign _int = 1;
   assign _nmi = 1;
   assign _busrq = 1;

   integer     halt_count = 0;

   always @(posedge clk) begin
      if (_halt_last & !_halt) begin
         $display("halted");
      end
      if (!_halt) begin
         halt_count <= halt_count + 1;
         if (halt_count == 10)
           $finish;
      end
      _halt_last <= _halt;
   end

   wire [7:0] data = _wr ? db_i : db_o;
   wire [15:0] trace = {clk, _reset, _wait, _iorq, _mreq, _wr, _rd, _m1, data};

   // Log bus activity
   always @(posedge clk) begin
      $display("   trace: %02x %02x", trace[7:0], trace[15:8]);
      if (_reset_last & !_reset)
        $display("reset asserted");
      if (!_reset_last & _reset)
        $display("reset released");
      if (_rd_last & !_rd) begin
         if (!_mreq & !_m1)
           $display("fetch rd: [%04x] = %02x",ab,db_i);
         if (!_mreq & _m1)
           $display("  mem rd: [%04x] = %02x",ab,db_i);
         if (!_iorq)
           $display("   io rd: [%04x] = %02x",ab,db_i);
      end
      if (_wr_last & !_wr) begin
         if (!_mreq)
           $display("  mem wr: [%04x] = %02x",ab,db_o);
         if (!_iorq)
           $display("   io wr: [%04x] = %02x",ab,db_o);
      end
      _wr_last <= _wr;
      _rd_last <= _rd;
      _reset_last <= _reset;
   end


   clocks_6502 _clocks(eclk, ereset, _reset, clk);

   chip_z80 _chip_z80
     (
      eclk, ereset,
      _busak,
      _busrq,
      _halt,
      _int,
      _iorq,
      _m1,
      _mreq,
      _nmi,
      _rd,
      _reset,
      _rfsh,
      _wait,
      _wr,
      ab[0],
      ab[1],
      ab[10],
      ab[11],
      ab[12],
      ab[13],
      ab[14],
      ab[15],
      ab[2],
      ab[3],
      ab[4],
      ab[5],
      ab[6],
      ab[7],
      ab[8],
      ab[9],
      clk,
      db_i[0], db_o[0], db_t[0],
      db_i[1], db_o[1], db_t[1],
      db_i[2], db_o[2], db_t[2],
      db_i[3], db_o[3], db_t[3],
      db_i[4], db_o[4], db_t[4],
      db_i[5], db_o[5], db_t[5],
      db_i[6], db_o[6], db_t[6],
      db_i[7], db_o[7], db_t[7]
    );

   //assign db_i = 8'hf5;
   // f5 PUSH AF
   // 3e LD a,n
   // 3c inc a

   ram_6502 _ram_6502(eclk, ereset, clk, ab, db_i, db_o, !(!_wr & !_mreq));

`ifndef verilator
   initial begin
      $dumpfile("test_z80.lxt");
      $dumpvars(0,main);
   end
`endif

endmodule

`ifndef verilator
module clk_reset_gen(output reg clk, reset);
   initial begin
      reset = 1;
      #1000;
      reset = 0;
   end

   initial begin
      clk = 0;
      forever #5 clk = ~clk;
   end

endmodule
`endif

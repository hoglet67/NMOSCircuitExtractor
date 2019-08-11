`define HALFCYCLE 100

`define VIVADO 1

module gate_level_z80
(
    input eclk
  , input locked
  , output reg clk
  , output reg _reset
  , output _halt
  , output _iorq
  , output _mreq
  , output _m1
  , output _rd
  , output _rfsh
  , output _wr
  , output [7:0] data
  , input  [3:0] btn
  , output [3:0] led
  , input rxd
  , output txd
 );

   wire        erst;
   wire [15:0] ab;
   wire [7:0]  ram_dout;
   wire [7:0]  uart_dout;
   wire [7:0]  db_i;
   wire [7:0]  db_o;
   reg [31:0]  c;
   reg [7:0]   i;


   // TODO: should debounce this!
   assign ereset = btn[0];

   assign led[0] = ereset;
   assign led[1] = locked;
   assign led[2] = ~_halt;
   assign led[3] = ~_m1;

   always @(posedge eclk)
     if (ereset) begin
        c <= 0;
        _reset <= 1;
        clk <= 0;
        i <= 0;
     end else begin
        if (c != 32'hFFFFFFFF)
          c <= c + 1;
        if (c==`HALFCYCLE * 10)
          _reset <= 0;
        if (c==`HALFCYCLE * 20)
          _reset <= 1;
        if (i==8'd`HALFCYCLE-1) begin
           i <= 0;
           clk <= ~clk;
        end else
          i <= i + 1;
     end

   assign data = _wr ? db_i : db_o;

   chip_z80 _chip_z80
     (
      .eclk(eclk),
      .erst(ereset),
      .pad__busak(),
      .pad__busrq(1'b1),
      .pad__halt(_halt),
      .pad__int(1'b1),
      .pad__iorq(_iorq),
      .pad__m1(_m1),
      .pad__mreq(_mreq),
      .pad__nmi(1'b1),
      .pad__rd(_rd),
      .pad__reset(_reset),
      .pad__rfsh(_rfsh),
      .pad__wait(1'b1),
      .pad__wr(_wr),
      .pad_ab0(ab[0]),
      .pad_ab1(ab[1]),
      .pad_ab10(ab[10]),
      .pad_ab11(ab[11]),
      .pad_ab12(ab[12]),
      .pad_ab13(ab[13]),
      .pad_ab14(ab[14]),
      .pad_ab15(ab[15]),
      .pad_ab2(ab[2]),
      .pad_ab3(ab[3]),
      .pad_ab4(ab[4]),
      .pad_ab5(ab[5]),
      .pad_ab6(ab[6]),
      .pad_ab7(ab[7]),
      .pad_ab8(ab[8]),
      .pad_ab9(ab[9]),
      .pad_clk(clk),
      .pad_db0_i(db_i[0]),
      .pad_db1_i(db_i[1]),
      .pad_db2_i(db_i[2]),
      .pad_db3_i(db_i[3]),
      .pad_db4_i(db_i[4]),
      .pad_db5_i(db_i[5]),
      .pad_db6_i(db_i[6]),
      .pad_db7_i(db_i[7]),
      .pad_db0_o(db_o[0]),
      .pad_db1_o(db_o[1]),
      .pad_db2_o(db_o[2]),
      .pad_db3_o(db_o[3]),
      .pad_db4_o(db_o[4]),
      .pad_db5_o(db_o[5]),
      .pad_db6_o(db_o[6]),
      .pad_db7_o(db_o[7])
    );

   reg last_clk;
   reg last_iorq;
   reg last_mreq_and_wr;
   reg _uart_cs;
   reg ram_wr;

   always @(posedge eclk) begin
      last_clk <= clk;
   end

   // Generate a 1 cycle long CS pulse for the UART on the rising
   // edge of the first clock cycle in the IO cycle (end of T2)

   wire uart_select = (ab[7:1] == 7'b1010101);

   always @(posedge eclk) begin
      _uart_cs <= 1'b1;
      if (clk & !last_clk) begin
         last_iorq <= _iorq;
         if (last_iorq & (!_iorq) & uart_select)
            _uart_cs <= 1'b0;
      end
   end

   uart
     #(
      .CLKSPEED(62500000),
      .BAUD(115200)
      )
     uart0
     (
      .clk(eclk),
      .reset_b(_reset),
      .cs_b(_uart_cs),
      .rnw(_wr),
      .a0(!ab[0]),
      .dout(uart_dout),
      .din(db_o),
      .rxd(rxd),
      .txd(txd)
      );

   // Generate a 1 cycle long WR pulse for the RAM on the falling
   // edge of the first clock cycle in the Mem cycle (end of T2)

   always @(posedge eclk) begin
      ram_wr <= 1'b0;
      if (clk & !last_clk) begin
         last_mreq_and_wr <= (!_mreq) & (!_wr);
         if ((!last_mreq_and_wr) & (!_mreq) & (!_wr))
            ram_wr <= 1'b1;
      end
   end

   ram_64Kx8 ram
     (
      .clk(eclk),
      .a(ab),
      .dout(ram_dout),
      .din(db_o),
      .wr(ram_wr)
      );

   // The z80doc/z80full tests expect IO reads to return 0xBF

   assign db_i = _iorq ? ram_dout : uart_select ? uart_dout : 8'hBF;

endmodule

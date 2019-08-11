// Copyright 1986-2019 Xilinx, Inc. All Rights Reserved.
// --------------------------------------------------------------------------------
// Tool Version: Vivado v.2019.1 (lin64) Build 2552052 Fri May 24 14:47:09 MDT 2019
// Date        : Sun Aug 11 11:12:47 2019
// Host        : quadhog running 64-bit Ubuntu 16.04.3 LTS
// Command     : write_verilog -force -mode synth_stub
//               /home/dmb/atom/NMOSCircuitExtractor/xilinx/vivado/GateLevelZ80/GateLevelZ80.srcs/sources_1/bd/TopBlock/ip/TopBlock_gate_level_z80_0_0/TopBlock_gate_level_z80_0_0_stub.v
// Design      : TopBlock_gate_level_z80_0_0
// Purpose     : Stub declaration of top-level module interface
// Device      : xc7z020clg400-1
// --------------------------------------------------------------------------------

// This empty module with port declaration file causes synthesis tools to infer a black box for IP.
// The synthesis directives are for Synopsys Synplify support to prevent IO buffer insertion.
// Please paste the declaration into a Verilog source file or add the file as an additional source.
(* X_CORE_INFO = "gate_level_z80,Vivado 2019.1" *)
module TopBlock_gate_level_z80_0_0(eclk, locked, clk, _reset, _halt, _iorq, _mreq, _m1, _rd, 
  _rfsh, _wr, data, btn, led, rxd, txd)
/* synthesis syn_black_box black_box_pad_pin="eclk,locked,clk,_reset,_halt,_iorq,_mreq,_m1,_rd,_rfsh,_wr,data[7:0],btn[3:0],led[3:0],rxd,txd" */;
  input eclk;
  input locked;
  output clk;
  output _reset;
  output _halt;
  output _iorq;
  output _mreq;
  output _m1;
  output _rd;
  output _rfsh;
  output _wr;
  output [7:0]data;
  input [3:0]btn;
  output [3:0]led;
  input rxd;
  output txd;
endmodule

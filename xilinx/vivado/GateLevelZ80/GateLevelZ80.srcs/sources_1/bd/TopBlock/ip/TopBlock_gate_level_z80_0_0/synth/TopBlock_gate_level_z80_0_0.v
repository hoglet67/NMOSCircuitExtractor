// (c) Copyright 1995-2019 Xilinx, Inc. All rights reserved.
// 
// This file contains confidential and proprietary information
// of Xilinx, Inc. and is protected under U.S. and
// international copyright and other intellectual property
// laws.
// 
// DISCLAIMER
// This disclaimer is not a license and does not grant any
// rights to the materials distributed herewith. Except as
// otherwise provided in a valid license issued to you by
// Xilinx, and to the maximum extent permitted by applicable
// law: (1) THESE MATERIALS ARE MADE AVAILABLE "AS IS" AND
// WITH ALL FAULTS, AND XILINX HEREBY DISCLAIMS ALL WARRANTIES
// AND CONDITIONS, EXPRESS, IMPLIED, OR STATUTORY, INCLUDING
// BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, NON-
// INFRINGEMENT, OR FITNESS FOR ANY PARTICULAR PURPOSE; and
// (2) Xilinx shall not be liable (whether in contract or tort,
// including negligence, or under any other theory of
// liability) for any loss or damage of any kind or nature
// related to, arising under or in connection with these
// materials, including for any direct, or any indirect,
// special, incidental, or consequential loss or damage
// (including loss of data, profits, goodwill, or any type of
// loss or damage suffered as a result of any action brought
// by a third party) even if such damage or loss was
// reasonably foreseeable or Xilinx had been advised of the
// possibility of the same.
// 
// CRITICAL APPLICATIONS
// Xilinx products are not designed or intended to be fail-
// safe, or for use in any application requiring fail-safe
// performance, such as life-support or safety devices or
// systems, Class III medical devices, nuclear facilities,
// applications related to the deployment of airbags, or any
// other applications that could lead to death, personal
// injury, or severe property or environmental damage
// (individually and collectively, "Critical
// Applications"). Customer assumes the sole risk and
// liability of any use of Xilinx products in Critical
// Applications, subject only to applicable laws and
// regulations governing limitations on product liability.
// 
// THIS COPYRIGHT NOTICE AND DISCLAIMER MUST BE RETAINED AS
// PART OF THIS FILE AT ALL TIMES.
// 
// DO NOT MODIFY THIS FILE.


// IP VLNV: xilinx.com:module_ref:gate_level_z80:1.0
// IP Revision: 1

(* X_CORE_INFO = "gate_level_z80,Vivado 2019.1" *)
(* CHECK_LICENSE_TYPE = "TopBlock_gate_level_z80_0_0,gate_level_z80,{}" *)
(* CORE_GENERATION_INFO = "TopBlock_gate_level_z80_0_0,gate_level_z80,{x_ipProduct=Vivado 2019.1,x_ipVendor=xilinx.com,x_ipLibrary=module_ref,x_ipName=gate_level_z80,x_ipVersion=1.0,x_ipCoreRevision=1,x_ipLanguage=VERILOG,x_ipSimLanguage=MIXED}" *)
(* IP_DEFINITION_SOURCE = "module_ref" *)
(* DowngradeIPIdentifiedWarnings = "yes" *)
module TopBlock_gate_level_z80_0_0 (
  eclk,
  locked,
  clk,
  _reset,
  _halt,
  _iorq,
  _mreq,
  _m1,
  _rd,
  _rfsh,
  _wr,
  data,
  btn,
  led,
  rxd,
  txd
);

input wire eclk;
input wire locked;
(* X_INTERFACE_PARAMETER = "XIL_INTERFACENAME clk, FREQ_HZ 100000000, PHASE 0.000, CLK_DOMAIN TopBlock_gate_level_z80_0_0_clk, INSERT_VIP 0" *)
(* X_INTERFACE_INFO = "xilinx.com:signal:clock:1.0 clk CLK" *)
output wire clk;
(* X_INTERFACE_PARAMETER = "XIL_INTERFACENAME _reset, POLARITY ACTIVE_LOW, INSERT_VIP 0" *)
(* X_INTERFACE_INFO = "xilinx.com:signal:reset:1.0 _reset RST" *)
output wire _reset;
output wire _halt;
output wire _iorq;
output wire _mreq;
output wire _m1;
output wire _rd;
output wire _rfsh;
output wire _wr;
output wire [7 : 0] data;
input wire [3 : 0] btn;
output wire [3 : 0] led;
input wire rxd;
output wire txd;

  gate_level_z80 inst (
    .eclk(eclk),
    .locked(locked),
    .clk(clk),
    ._reset(_reset),
    ._halt(_halt),
    ._iorq(_iorq),
    ._mreq(_mreq),
    ._m1(_m1),
    ._rd(_rd),
    ._rfsh(_rfsh),
    ._wr(_wr),
    .data(data),
    .btn(btn),
    .led(led),
    .rxd(rxd),
    .txd(txd)
  );
endmodule

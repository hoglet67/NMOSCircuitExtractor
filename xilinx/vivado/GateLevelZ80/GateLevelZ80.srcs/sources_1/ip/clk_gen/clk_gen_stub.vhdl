-- Copyright 1986-2019 Xilinx, Inc. All Rights Reserved.
-- --------------------------------------------------------------------------------
-- Tool Version: Vivado v.2019.1 (lin64) Build 2552052 Fri May 24 14:47:09 MDT 2019
-- Date        : Tue Jul 30 18:47:31 2019
-- Host        : quadhog running 64-bit Ubuntu 16.04.3 LTS
-- Command     : write_vhdl -force -mode synth_stub
--               /home/dmb/atom/NMOSCircuitExtractor/xilinx/vivado/GateLevelZ80/GateLevelZ80.srcs/sources_1/ip/clk_gen/clk_gen_stub.vhdl
-- Design      : clk_gen
-- Purpose     : Stub declaration of top-level module interface
-- Device      : xc7z020clg400-1
-- --------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity clk_gen is
  Port ( 
    clk_out1 : out STD_LOGIC;
    reset : in STD_LOGIC;
    locked : out STD_LOGIC;
    clk_in1 : in STD_LOGIC
  );

end clk_gen;

architecture stub of clk_gen is
attribute syn_black_box : boolean;
attribute black_box_pad_pin : string;
attribute syn_black_box of stub : architecture is true;
attribute black_box_pad_pin of stub : architecture is "clk_out1,reset,locked,clk_in1";
begin
end;

-- Copyright 1986-2019 Xilinx, Inc. All Rights Reserved.
-- --------------------------------------------------------------------------------
-- Tool Version: Vivado v.2019.1 (lin64) Build 2552052 Fri May 24 14:47:09 MDT 2019
-- Date        : Sun Aug 11 11:12:48 2019
-- Host        : quadhog running 64-bit Ubuntu 16.04.3 LTS
-- Command     : write_vhdl -force -mode synth_stub
--               /home/dmb/atom/NMOSCircuitExtractor/xilinx/vivado/GateLevelZ80/GateLevelZ80.srcs/sources_1/bd/TopBlock/ip/TopBlock_gate_level_z80_0_0/TopBlock_gate_level_z80_0_0_stub.vhdl
-- Design      : TopBlock_gate_level_z80_0_0
-- Purpose     : Stub declaration of top-level module interface
-- Device      : xc7z020clg400-1
-- --------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity TopBlock_gate_level_z80_0_0 is
  Port ( 
    eclk : in STD_LOGIC;
    locked : in STD_LOGIC;
    clk : out STD_LOGIC;
    \_reset\ : out STD_LOGIC;
    \_halt\ : out STD_LOGIC;
    \_iorq\ : out STD_LOGIC;
    \_mreq\ : out STD_LOGIC;
    \_m1\ : out STD_LOGIC;
    \_rd\ : out STD_LOGIC;
    \_rfsh\ : out STD_LOGIC;
    \_wr\ : out STD_LOGIC;
    data : out STD_LOGIC_VECTOR ( 7 downto 0 );
    btn : in STD_LOGIC_VECTOR ( 3 downto 0 );
    led : out STD_LOGIC_VECTOR ( 3 downto 0 );
    rxd : in STD_LOGIC;
    txd : out STD_LOGIC
  );

end TopBlock_gate_level_z80_0_0;

architecture stub of TopBlock_gate_level_z80_0_0 is
attribute syn_black_box : boolean;
attribute black_box_pad_pin : string;
attribute syn_black_box of stub : architecture is true;
attribute black_box_pad_pin of stub : architecture is "eclk,locked,clk,\_reset\,\_halt\,\_iorq\,\_mreq\,\_m1\,\_rd\,\_rfsh\,\_wr\,data[7:0],btn[3:0],led[3:0],rxd,txd";
attribute X_CORE_INFO : string;
attribute X_CORE_INFO of stub : architecture is "gate_level_z80,Vivado 2019.1";
begin
end;

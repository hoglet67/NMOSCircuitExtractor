
set_property -dict { PACKAGE_PIN H16   IOSTANDARD LVCMOS33 } [get_ports { sys_clk }]; #IO_L13P_T2_MRCC_35 Sch=sysclk
create_clock -add -name sys_clk -period 8.00 -waveform {0 4} [get_ports { sys_clk }];

set_property -dict { PACKAGE_PIN Y18   IOSTANDARD LVCMOS33 } [get_ports { clk     }]; #IO_L17P_T2_34 Sch=rpio_04_r
set_property -dict { PACKAGE_PIN Y19   IOSTANDARD LVCMOS33 } [get_ports { _reset  }]; #IO_L17N_T2_34 Sch=rpio_05_r
set_property -dict { PACKAGE_PIN U18   IOSTANDARD LVCMOS33 } [get_ports { _halt   }]; #IO_L22P_T3_13 Sch=rpio_06_r
set_property -dict { PACKAGE_PIN U19   IOSTANDARD LVCMOS33 } [get_ports { _iorq   }]; #IO_L12P_T1_MRCC_34 Sch=rpio_07_r
set_property -dict { PACKAGE_PIN F19   IOSTANDARD LVCMOS33 } [get_ports { _mreq   }]; #IO_L12N_T1_MRCC_34 Sch=rpio_08_r
set_property -dict { PACKAGE_PIN V10   IOSTANDARD LVCMOS33 } [get_ports { _m1     }]; #IO_L21N_T3_DQS_13 Sch=rpio_09_r
set_property -dict { PACKAGE_PIN V8    IOSTANDARD LVCMOS33 } [get_ports { _rd     }]; #IO_L15P_T2_DQS_13 Sch=rpio_10_r
set_property -dict { PACKAGE_PIN W10   IOSTANDARD LVCMOS33 } [get_ports { _rfsh   }]; #IO_L16P_T2_13 Sch=rpio_11_r
set_property -dict { PACKAGE_PIN B20   IOSTANDARD LVCMOS33 } [get_ports { _wr     }]; #IO_L1N_T0_AD0N_35 Sch=rpio_12_r
set_property -dict { PACKAGE_PIN W8    IOSTANDARD LVCMOS33 } [get_ports { data[0] }]; #IO_L15N_T2_DQS_13 Sch=rpio_13_r
set_property -dict { PACKAGE_PIN V6    IOSTANDARD LVCMOS33 } [get_ports { data[1] }]; #IO_L22P_T3_13 Sch=rpio_14_r
set_property -dict { PACKAGE_PIN Y6    IOSTANDARD LVCMOS33 } [get_ports { data[2] }]; #IO_L13N_T2_MRCC_13 Sch=rpio_15_r
set_property -dict { PACKAGE_PIN B19   IOSTANDARD LVCMOS33 } [get_ports { data[3] }]; #IO_L2P_T0_AD8P_35 Sch=rpio_16_r
set_property -dict { PACKAGE_PIN U7    IOSTANDARD LVCMOS33 } [get_ports { data[4] }]; #IO_L11P_T1_SRCC_13 Sch=rpio_17_r
set_property -dict { PACKAGE_PIN C20   IOSTANDARD LVCMOS33 } [get_ports { data[5] }]; #IO_L1P_T0_AD0P_35 Sch=rpio_18_r
set_property -dict { PACKAGE_PIN Y8    IOSTANDARD LVCMOS33 } [get_ports { data[6] }]; #IO_L14N_T2_SRCC_13 Sch=rpio_19_r
set_property -dict { PACKAGE_PIN A20   IOSTANDARD LVCMOS33 } [get_ports { data[7] }]; #IO_L2N_T0_AD8N_35 Sch=rpio_20_r

#set_property -dict { PACKAGE_PIN Y9    IOSTANDARD LVCMOS33 } [get_ports { rpio_21_r }]; #IO_L14P_T2_SRCC_13 Sch=rpio_21_r
#set_property -dict { PACKAGE_PIN U8    IOSTANDARD LVCMOS33 } [get_ports { rpio_22_r }]; #IO_L17N_T2_13 Sch=rpio_22_r
#set_property -dict { PACKAGE_PIN W6    IOSTANDARD LVCMOS33 } [get_ports { rpio_23_r }]; #IO_IO_L22N_T3_13 Sch=rpio_23_r
#set_property -dict { PACKAGE_PIN Y7    IOSTANDARD LVCMOS33 } [get_ports { rpio_24_r }]; #IO_L13P_T2_MRCC_13 Sch=rpio_24_r
#set_property -dict { PACKAGE_PIN F20   IOSTANDARD LVCMOS33 } [get_ports { rpio_25_r }]; #IO_L15N_T2_DQS_AD12N_35 Sch=rpio_25_r
#set_property -dict { PACKAGE_PIN W9    IOSTANDARD LVCMOS33 } [get_ports { rpio_26_r }]; #IO_L16N_T2_13 Sch=rpio_26_r
#set_property -dict { PACKAGE_PIN Y16   IOSTANDARD LVCMOS33 } [get_ports { rpio_sd_r }]; #IO_L7P_T1_34 Sch=rpio_sd_r
#set_property -dict { PACKAGE_PIN Y17   IOSTANDARD LVCMOS33 } [get_ports { rpio_sc_r }]; #IO_L7N_T1_34 Sch=rpio_sc_r


##LEDs

set_property -dict { PACKAGE_PIN R14   IOSTANDARD LVCMOS33 } [get_ports { led[0] }]; #IO_L6N_T0_VREF_34 Sch=led[0]
set_property -dict { PACKAGE_PIN P14   IOSTANDARD LVCMOS33 } [get_ports { led[1] }]; #IO_L6P_T0_34 Sch=led[1]
set_property -dict { PACKAGE_PIN N16   IOSTANDARD LVCMOS33 } [get_ports { led[2] }]; #IO_L21N_T3_DQS_AD14N_35 Sch=led[2]
set_property -dict { PACKAGE_PIN M14   IOSTANDARD LVCMOS33 } [get_ports { led[3] }]; #IO_L23P_T3_35 Sch=led[3]

##Buttons

set_property -dict { PACKAGE_PIN D19   IOSTANDARD LVCMOS33 } [get_ports { btn[0] }]; #IO_L4P_T0_35 Sch=btn[0]
set_property -dict { PACKAGE_PIN D20   IOSTANDARD LVCMOS33 } [get_ports { btn[1] }]; #IO_L4N_T0_35 Sch=btn[1]
set_property -dict { PACKAGE_PIN L20   IOSTANDARD LVCMOS33 } [get_ports { btn[2] }]; #IO_L9N_T1_DQS_AD3N_35 Sch=btn[2]
set_property -dict { PACKAGE_PIN L19   IOSTANDARD LVCMOS33 } [get_ports { btn[3] }]; #IO_L9P_T1_DQS_AD3P_35 Sch=btn[3]

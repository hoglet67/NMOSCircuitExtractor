sim:
	verilator -Wno-fatal -DHALFCYCLE=100 --cc ../verilog/test_z80.v --top-module main -I../verilog --exe sim_main.cpp \
      -v ../verilog/models_monta.v \
      -v ../verilog/library.v \
      -v ../verilog/clocks_6502.v \
      -v ../verilog/ram_6502.v \
#	(cd obj_dir ; make -f Vmain.mk )
	(cd obj_dir ; make OPT_FAST="-O2" -f Vmain.mk )

clean:
	rm -rf obj_dir

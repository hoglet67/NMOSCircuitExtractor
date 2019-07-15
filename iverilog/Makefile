VSRC = ../verilog/test_z80.v\
       ../verilog/chip_z80.v\
       ../verilog/clocks_6502.v\
       ../verilog/ram_6502.v\
       ../verilog/models_peter.v

VFLAGS = -Wall

test_allasm:
	iverilog $(VFLAGS) -DMAXTICKS=400000 -DHALFCYCLE=200 -DDISPLAY_WRITES -DCODE=\"f5.bin\" -DCODE_START=0 -DRESET=4000 -I../verilog $(VSRC)

clean:
	rm -f a.out *.lxt

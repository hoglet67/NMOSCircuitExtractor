VSRC = ../verilog/test_z80.v\
       ../verilog/chip_z80.v\
       ../verilog/clocks_6502.v\
       ../verilog/ram_6502.v\
       ../verilog/library.v\
       ../verilog/models_monta.v

VFLAGS = -Wall

test_allasm:
	iverilog $(VFLAGS) -DMAXTICKS=100000 -DHALFCYCLE=100 -DDISPLAY_WRITES -DCODE=\"f5.bin\" -DCODE_START=0 -DRESET=4000 -I../verilog $(VSRC)
	./a.out -lxt2

clean:
	rm -f a.out *.lxt

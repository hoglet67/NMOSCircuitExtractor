module ram_64Kx8(input clk, input [15:0] a, output reg [7:0] dout, input [7:0] din, input wr);

   reg [7:0] mem[0:65535];

   initial
     begin
        $readmemh("test.mem", mem);
     end

   always @(posedge clk)
     begin
        if (wr)
           mem[a] <= din;
        dout <= mem[a];
     end

endmodule

// Encoding:
//
// 000x - undriven
// 0010 - floating 1
// 0011 - floating 0
// 1000 - S0 1
// 1001 - S0 0
// 1010 - S1 1
// 1011 - S1 0
// 1110 - S2 1
// 1111 - S2 0
// 1110 - S3 1
// 1111 - S3 0
//
// These are carefully chosen so the largest value wins a conflict

`define W 4

`define B_LEVEL    0
`define B_DRIVEN   3

`define L_LO       1'b1
`define L_HI       1'b0

`define S_OFF      3'b000
`define S_FLOATING 3'b001
`define S_S0       3'b100
`define S_S1       3'b101
`define S_S2       3'b110
`define S_S3       3'b111

`define S_PULLUP   `S_S0
`define S_STRONG   `S_S3

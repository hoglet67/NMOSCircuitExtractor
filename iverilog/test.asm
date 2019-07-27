   ld   sp, $1000
   ld   a, 127
   ld   b, 3
loop:
   push af
   inc  a
   out  ($aa),a
   djnz loop
   nop
   halt

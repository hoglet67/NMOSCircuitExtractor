   rrca
   rrca
   exx
   rrca
   rrca
   ex   af, af'
   rrca
   rrca
   ex   de, hl
   rrca
   rrca
   ex   de, hl
   rrca
   rrca
   set  5,e
   rrca
   rrca
   halt

   ld   hl, $8200
   push hl
   pop  af
   nop
   nop
   ccf
   scf
   nop
   nop
   push af
   nop
   nop
   halt


   ld   sp, $ffff
   ld   hl, $007e
   push hl
   pop  af
   ld   de, $ddee
   ld   hl, $880c
   nop
   set 5,e
   nop
   push af
   push de
   push hl
   nop
   nop
   ex de, hl
   nop
   nop
   ld  c, $fe
   out (C), 0
   nop
   halt

.text
ori $26,$0,0x1001
mtc0 $26,$12
ori $1,$0,0x1111
ori $2,$0,0x2222
nop

.ktext 0x4180
mfc0 $8,$13
ori $9,$0,0x7f20
sb $0,0($9)
eret

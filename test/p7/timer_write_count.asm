.text
sw $0,0x7f08($0)
addi $3,$0,1
nop
.ktext 0x4180
mfc0 $2,$13
mfc0 $4,$14
nop

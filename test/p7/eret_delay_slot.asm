.text
ori $4,$0,0x3010
mtc0 $4,$14
beq $0,$0,target
eret
addi $1,$0,1
target:
addi $2,$0,2
nop
.ktext 0x4180
nop

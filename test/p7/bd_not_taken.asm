.text
ori $1,$0,1
beq $0,$1,target
syscall
target:
addi $5,$0,5
.ktext 0x4180
mfc0 $2,$13
mfc0 $3,$14
nop

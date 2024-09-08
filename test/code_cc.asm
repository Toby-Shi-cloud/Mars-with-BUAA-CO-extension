mult $0, $0  # Mul
li $1, 1     # Other
div $0, $1   # Div
multu $0, $0  # Mul

j tag_1      # Jump
tag_1:

beq $0, $0, tag_2  # Jump
tag_2:

li $t0, 1    # Other
li $t1, 10   # Other

loop:  # x10
lw $0, 0($0) # Load
lb $0, 0($0) # Load
lh $0, 0($0) # Load
sw $0, 0($0) # Store
sb $0, 0($0) # Store
sh $0, 0($0) # Store
divu $0, $1   # Div

sub $t1, $t1, $t0  # Other
bnez $t1,loop  # Jump


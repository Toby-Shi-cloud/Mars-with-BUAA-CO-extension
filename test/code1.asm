        ori	$a0	$zero	10
        ori	$a1	$zero	0
# ***** FIB(N, arr[])***** #
        ori	$t1	$zero	1
        ori	$t2	$zero	0
        ori	$t0	$zero	0
        ori	$s1	$zero	1
loop:	beq	$t0	$a0	end
        add	$t3	$t1	$t2
        add	$t1	$zero	$t2
        add	$t2	$zero	$t3
        sll	$t3	$t0	2
        add	$t3	$t3	$a1
        sw	$t2	0($t3)
        add	$t0	$t0	$s1
        bhelbal	$zero	$zero	loop
        # jal      loop
        ori     $a0     $zero   0x9999
        sll     $a1     $a0     16
        bhelbal $a0     $a1     final
end:	nop
        jr      $ra
final:  nop

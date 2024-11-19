//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import mars.Globals;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.InstructionLoad;
import mars.simulator.DelayedBranch;

public class behlbal implements InstructionLoad {
    private final String template = "behlbal $t1, $t2, label";
    private final String formatStr = "B";
    private final String encoding = "111110 sssss fffff tttttttttttttttt";
    private final String description = "Branch if Equal High-Level Bit And Link";
    
    public behlbal() {
    }
    
    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        int a = operands[2];
        int pc = RegisterFile.getProgramCounter();
        int rs = RegisterFile.getValue(operands[0]);
        int rt = RegisterFile.getValue(operands[1]);
        int tempA = 0;
        int tempB = 0;
        
        for(int i = 0; i < 32; ++i) {
            if ((rs & 1) == 1) {
                ++tempA;
            }
            
            if ((rt & 1) == 1) {
                ++tempB;
            }
            
            rs >>>= 1;
            rt >>>= 1;
        }
        
        if (tempA == tempB) {
            this.processReturnAddress();
            this.processJump((a << 2) + pc);
        }
        
    }
    
    private void processJump(int displacement) {
        if (Globals.getSettings().getDelayedBranchingEnabled()) {
            DelayedBranch.register(displacement);
        } else {
            RegisterFile.setProgramCounter(displacement);
        }
        
    }
    
    private void processReturnAddress() {
        RegisterFile.updateRegister(31, RegisterFile.getProgramCounter() + (Globals.getSettings().getDelayedBranchingEnabled() ? 4 : 0));
    }
    
    public String getTemplate() {
        return "behlbal $t1, $t2, label";
    }
    
    public String getFormatStr() {
        return "B";
    }
    
    public String getDescription() {
        return "Branch if Equal High-Level Bit And Link";
    }
    
    public String getEncoding() {
        return "111110 sssss fffff tttttttttttttttt";
    }
}

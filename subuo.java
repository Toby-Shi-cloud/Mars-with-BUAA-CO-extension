
/*
 * This file is a part of BUAA CO extension.
 * 
 */

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.AdditionalInstruction;
import mars.mips.instructions.BasicInstructionFormat;

/**
 * An example of AdditionalInstruction.
 * 
 */
public class subuo implements AdditionalInstruction {

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0],
                RegisterFile.getValue(operands[1])
                        - RegisterFile.getValue(operands[2]));
    }

    @Override
    public String getExample() {
        return "subuo $t1,$t2,$t3";
    }

    @Override
    public String getDescription() {
        return "Subtraction without overflow : set $t1 to ($t2 minus $t3), same as subu (but machine code same as sub)";
    }

    @Override
    public BasicInstructionFormat getInstructionFormat() {
        return BasicInstructionFormat.R_FORMAT;
    }

    @Override
    public String getOperationMask() {
        return "000000 sssss ttttt fffff 00000 100010";
    }

}

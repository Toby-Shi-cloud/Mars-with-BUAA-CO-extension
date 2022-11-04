package mars.mips.instructions;

/*
 * This file is a part of BUAA CO extension.
 * 
 */

/**
 * Interface to represent an additional instruction. Any
 * class that implements this interface can be load during
 * runtime. Please extend BranchOperation if you want
 * your instruction support jump, branch, or link.
 * 
 * @author Toby Shi
 * @version November 2022
 * @see BranchOperation
 * @see SimulationCode
 * @see InstructionSet
 */
public interface AdditionalInstruction extends SimulationCode {

    /**
     * An example usage of the instruction, as a String.
     * (e.g. add $t1,$t2,$t3)
     * 
     * @return the example string of the instruction
     */
    public String getExample();

    /**
     * Return a brief description of the additional instruction
     * (e.g. Addition with overflow : set $t1 to ($t2 plus $t3))
     * 
     * @return the description string of the instruction
     */
    public String getDescription();

    /**
     * The instruction format must be R_FORMAT, I_FORMAT,
     * J_FORMAT, or I_BRANCH_FORMAT
     * 
     * @return the MIPS-defined formats of the instruction
     * @see BasicInstructionFormat
     */
    public BasicInstructionFormat getInstructionFormat();

    /**
     * The opcode mask is a 32 character string that contains the
     * opcode in binary in the appropriate bit positions and codes
     * for operand positions ('f', 's', 't') in the remaining positions.
     * 
     * @return the operation mask of the instruction
     */
    public String getOperationMask();
}

package mars.mips.instructions;

/*
 * This file is a part of BUAA CO extension.
 * 
 */

import mars.Globals;
import mars.Settings;
import mars.mips.hardware.RegisterFile;
import mars.simulator.DelayedBranch;

/**
 * This abstract class contains methods which can be use to simulate
 * branch, jump, or link. These methods are originally written
 * by Pete Sanderson and Ken Vollmar in InstructionSet.
 * 
 * @author Pete Sanderson and Ken Vollmar
 * 
 * @version November 2022 DPS:
 *          Notice that public method
 *          <code> Globals.getSettings().getDelayedBranchingEnabled() </code>
 *          is deprecated. I replaced it by a new method
 *          <code> Globals.getSettings().getBooleanSetting(int id) </code>.
 * 
 * @see SimulationCode
 * @see InstructionSet
 */
public abstract class BranchOperation {

    /**
     * Method to process a successful branch condition. DO NOT USE WITH JUMP
     * INSTRUCTIONS! The branch operand is a relative displacement in words
     * whereas the jump operand is an absolute address in bytes.
     * <br>
     * <br>
     * Handles delayed branching if that setting is enabled.
     *
     * @param displacement displacement operand from instruction.
     * 
     * @author Pete Sanderson and Ken Vollmar
     * @version 4 January 2008 DPS:
     *          The subtraction of 4 bytes (instruction length) after
     *          the shift has been removed. It is left in as commented-out code
     *          below.
     *          This has the effect of always branching as if delayed branching is
     *          enabled, even if it isn't. This mod must work in conjunction with
     *          ProgramStatement.java, buildBasicStatementFromBasicInstruction()
     *          method near the bottom (currently line 194, heavily commented).
     * 
     * @see InstructionSet
     * @see AdditionalInstruction
     */
    protected void processBranch(int displacement) {
        if (Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED)) {
            // Register the branch target address (absolute byte address).
            DelayedBranch.register(RegisterFile.getProgramCounter() + (displacement << 2));
        } else {
            // Decrement needed because PC has already been incremented
            RegisterFile.setProgramCounter(
                    RegisterFile.getProgramCounter()
                            + (displacement << 2)); // - Instruction.INSTRUCTION_LENGTH);
        }
    }

    /**
     * Method to process a jump. DO NOT USE WITH BRANCH INSTRUCTIONS!
     * The branch operand is a relative displacement in words
     * whereas the jump operand is an absolute address in bytes.
     * <br>
     * <br>
     * Handles delayed branching if that setting is enabled.
     *
     * @param targetAddress jump target absolute byte address.
     * 
     * @author Pete Sanderson and Ken Vollmar
     * 
     * @see InstructionSet
     * @see AdditionalInstruction
     */
    protected void processJump(int targetAddress) {
        if (Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED)) {
            DelayedBranch.register(targetAddress);
        } else {
            RegisterFile.setProgramCounter(targetAddress);
        }
    }

    /**
     * Method to process storing of a return address in the given
     * register. This is used only by the "and link"
     * instructions: jal, jalr, bltzal, bgezal. If delayed branching
     * setting is off, the return address is the address of the
     * next instruction (e.g. the current PC value). If on, the
     * return address is the instruction following that, to skip over
     * the delay slot.
     *
     * @param register register number to receive the return address.
     * 
     * @author Pete Sanderson and Ken Vollmar
     * 
     * @see InstructionSet
     * @see AdditionalInstruction
     */
    protected void processReturnAddress(int register) {
        RegisterFile.updateRegister(register, RegisterFile.getProgramCounter() +
                ((Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED))
                        ? Instruction.INSTRUCTION_LENGTH
                        : 0));
    }
}

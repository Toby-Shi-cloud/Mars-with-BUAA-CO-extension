package mars.simulator;

/*
Copyright (c) 2024,  swkfk

Developed by swkfk (kai_Ker@buaa.edu.cn)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

import mars.Globals;
import mars.ProgramStatement;

/**
 * Used to count the real cycles of the simulator. The cycles of different instructions may be different.
 *
 * @author swkfk
 * @version September 2024
 */
public class CycleCounter {
    public enum InstructionType {
        DIV(), MUL(), GOTO(), MEM(), OTHER();

        private float cycles;
        private int count;

        InstructionType() {
            this.cycles = 1;
            this.count = 0;
        }
    }

    private void setCycle(InstructionType type, float cycles) {
        type.cycles = cycles;
    }

    private void updateCycle(InstructionType type) {
        type.count++;
    }

    /**
     * Parse the cycles weight setting and set the cycles of different instructions.
     * The setting should be in the format of "DIV:MUL:GOTO:MEM:OTHER".
     * If the "cc" setting is false, the cycles will not be counted and the "ccw" setting will be ignored.
     */
    public CycleCounter() {
        if (!Globals.getSettings().getCountCycles()) {
            return;
        }
        String[] weights = Globals.getSettings().getCyclesWeight().split(":");
        try {
            setCycle(InstructionType.DIV, Float.parseFloat(weights[0]));
            setCycle(InstructionType.MUL, Float.parseFloat(weights[1]));
            setCycle(InstructionType.GOTO, Float.parseFloat(weights[2]));
            setCycle(InstructionType.MEM, Float.parseFloat(weights[3]));
            setCycle(InstructionType.OTHER, Float.parseFloat(weights[4]));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("Error: Invalid cycles weight setting.");
            System.exit(1);
        }
    }

    /**
     * Count the cycles of the instruction. If the "cc" setting is false, the cycles will not be counted.
     * @param statement The ProgramStatement to be counted.
     */
    public void update(ProgramStatement statement) {
        if (!Globals.getSettings().getCountCycles()) {
            return;
        }

        int opcode = statement.getBinaryStatement() >>> 26;
        int func = statement.getBinaryStatement() & 0x1F;

        if (opcode == 0x00) {
            if (func == 0b001000 || func == 0b001001) {
                // jr, jalr
                updateCycle(InstructionType.GOTO);
            } else if (func == 0b011000 || func == 0b011001) {
                // mult, multu
                updateCycle(InstructionType.MUL);
            } else if (func == 0b011010 || func == 0b011011) {
                // div, divu
                updateCycle(InstructionType.DIV);
            } else {
                updateCycle(InstructionType.OTHER);
            }
        } else if (opcode == 0x01) {
            if (func <= 0x07 || (0x10 <= func && func <= 0x13)) {
                // bltz, bgez, bltzl, bgezl, bltzal, bgezal, bltzall, bgczall
                updateCycle(InstructionType.GOTO);
            } else {
                updateCycle(InstructionType.OTHER);
            }
        } else if (opcode == 0b000010 || opcode == 0b000011) {
            // j, jal
            updateCycle(InstructionType.GOTO);
        } else if (opcode <= 0x07) {
            // beq, bne, blez, bgtz
            updateCycle(InstructionType.GOTO);
        } else if (0x14 <= opcode && opcode <= 0x17) {
            // beql, bnel, blezl, bgtzl
            updateCycle(InstructionType.GOTO);
        } else if (0x20 <= opcode && opcode <= 0x26) {
            // lb, lh, lwl, lw, lbu, lhu, lwr
            updateCycle(InstructionType.MEM);
        } else if (0x28 <= opcode && opcode <= 0x2E) {
            // sb, sh, swl, sw, swr
            updateCycle(InstructionType.MEM);
        } else {
            updateCycle(InstructionType.OTHER);
        }
    }

    public String emitResult() {
        if (!Globals.getSettings().getCountCycles()) {
            return "Cycles counting is disabled.";
        }
        float total = InstructionType.DIV.count * InstructionType.DIV.cycles +
                      InstructionType.MUL.count * InstructionType.MUL.cycles +
                      InstructionType.GOTO.count * InstructionType.GOTO.cycles +
                      InstructionType.MEM.count * InstructionType.MEM.cycles +
                      InstructionType.OTHER.count * InstructionType.OTHER.cycles;
        return String.format(
            "DIV: %d * %.1f%n" +
            "MUL: %d * %.1f%n" +
            "J/Br: %d * %.1f%n" +
            "Mem: %d * %.1f%n" +
            "Other: %d * %.1f%n" +
            "======%n" +
            "Total: %.1f Cycles",
            InstructionType.DIV.count, InstructionType.DIV.cycles,
            InstructionType.MUL.count, InstructionType.MUL.cycles,
            InstructionType.GOTO.count, InstructionType.GOTO.cycles,
            InstructionType.MEM.count, InstructionType.MEM.cycles,
            InstructionType.OTHER.count, InstructionType.OTHER.cycles,
            total
        );
    }
}

package mars.tools;

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

import mars.ProgramStatement;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.simulator.CycleCounter;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;

public class CyclesCounter extends AbstractMarsToolAndApplication {
    private static final String name = "Cycles Counter";
    private static final String version = " Version 1.0";
    protected int lastAddress = -1;

    protected int totalCount = 0;
    protected float[] cycleWeights = {25, 4, 2, 3, 1};
    protected int[] instructionCount = {0, 0, 0, 0, 0};
    private final String[] labels = {"Div", "Mul", "J/Br", "Mem", "Other"};

    private final JTextField[] instructionCountFields = new JTextField[5];
    private final JTextField[] weightFields = new JTextField[5];
    private final JProgressBar[] instructionProgressBars = new JProgressBar[5];
    private final JTextField CPIField = new JTextField(10);

    public CyclesCounter() {
        super(name + ", " + version, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected JComponent buildMainDisplayArea() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_END;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 0, 0);

        c.gridy = 1;
        c.gridx = 2;
        panel.add(new JLabel("CPI: "), c);

        c.gridx = 3;
        panel.add(CPIField, c);
        CPIField.setText("0");
        CPIField.setEditable(false);

        for (int i = 0; i < 5; i++) {
            instructionCountFields[i] = new JTextField("0", 10);
            instructionCountFields[i].setEditable(false);
            instructionProgressBars[i] = new JProgressBar(JProgressBar.HORIZONTAL);
            instructionProgressBars[i].setStringPainted(true);
            instructionProgressBars[i].setString("0.0");
            weightFields[i] = new JTextField(String.format("%.1f", cycleWeights[i]), 10);

            c.gridy = 3 + i;

            c.anchor = GridBagConstraints.LINE_END;
            c.gridx = 2;
            c.insets = new Insets(0, 0, 0, 0);
            panel.add(new JLabel(labels[i] + ": "), c);

            c.anchor = GridBagConstraints.LINE_START;
            c.gridx = 3;
            c.insets = new Insets(0, 0, 0, 0);
            panel.add(instructionCountFields[i], c);

            c.gridx = 4;
            c.insets = new Insets(3, 3, 3, 3);
            panel.add(instructionProgressBars[i], c);

            c.gridx = 5;
            c.insets = new Insets(0, 0, 0, 0);
            panel.add(weightFields[i], c);
        }
        return panel;
    }

    protected void addAsObserver() {
        addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }

    protected void processMIPSUpdate(Observable resource, AccessNotice notice) {
        if (!notice.accessIsFromMIPS() || notice.getAccessType() != AccessNotice.READ) {
            return;
        }
        MemoryAccessNotice m = (MemoryAccessNotice) notice;
        int addr = m.getAddress();
        if (addr == lastAddress) {
            return;
        }
        lastAddress = addr;
        totalCount++;
        try {
            ProgramStatement statement = Memory.getInstance().getStatement(addr);
            if (statement == null) {
                return;
            }
            CycleCounter.InstructionType type = CycleCounter.getType(Memory.getInstance().getStatement(addr));
            switch (type) {
                case DIV:
                    instructionCount[0]++;
                    break;
                case MUL:
                    instructionCount[1]++;
                    break;
                case GOTO:
                    instructionCount[2]++;
                    break;
                case MEM:
                    instructionCount[3]++;
                    break;
                case OTHER:
                    instructionCount[4]++;
                    break;
            }
        } catch (AddressErrorException e) {
            e.printStackTrace();
        }
        updateDisplay();
    }

    protected void initializePreGUI() {
        totalCount = 0;
        for (int i = 0; i < 5; i++) {
            instructionCount[i] = 0;
        }
        lastAddress = -1;
    }

    protected void reset() {
        totalCount = 0;
        for (int i = 0; i < 5; i++) {
            instructionCount[i] = 0;
        }
        lastAddress = -1;
        updateDisplay();
    }

    protected void updateDisplay() {
        for (int i = 0; i < 5; i++) {
            try {
                cycleWeights[i] = Float.parseFloat(weightFields[i].getText());
            } catch (NumberFormatException e) {
                weightFields[i].setText(String.format("%.1f", cycleWeights[i]));
            }
        }
        float totalCycles = 0;
        CPIField.setText(Integer.toString(totalCount));
        for (int i = 0; i < 5; i++) {
            totalCycles += instructionCount[i] * cycleWeights[i];
            instructionCountFields[i].setText(Integer.toString(instructionCount[i]));
        }
        for (int i = 0; i < 5; i++) {
            instructionProgressBars[i].setValue((int) (instructionCount[i] * cycleWeights[i]));
            instructionProgressBars[i].setMaximum((int) totalCycles);
        }
        for (int i = 0; i < 5; i++) {
            instructionProgressBars[i].setString(String.format("%.1f", instructionCount[i] * cycleWeights[i]));
        }
        if (totalCount == 0) {
            CPIField.setText("0");
        } else {
            CPIField.setText(String.format("%.1f / %d = %.1f", totalCycles, totalCount, totalCycles / totalCount));
        }
    }
}

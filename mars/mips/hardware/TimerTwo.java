package mars.mips.hardware;

import java.util.Observer;
import mars.Globals;

/**
 * Timer1 peripheral for P7 BUAA CO.
 * Memory-mapped at 0x7F10~0x7F1B (CTRL/PRESET/Count).
 * State machine: IDLE -> LOAD -> CNT -> INT.
 * @author Based on Mars_p7.jar decompilation
 */
public class TimerTwo {
    public static final int CTRL = 0;
    public static final int PRESET = 1;
    public static final int COUNT = 2;

    public static final int IDLE = 0;
    public static final int LOAD = 1;
    public static final int CNT = 2;
    public static final int INT = 3;

    public static int IRQ = 0;
    public static int state = 0;
    public static boolean enable = false;

    private static Register[] registers = new Register[] {
        new Register("$0 (ctrl)", 0, 0),
        new Register("$1 (preset)", 1, 0),
        new Register("$2 (count)", 2, 0)
    };

    public static void showRegisters() {
        for (int i = 0; i < registers.length; i++) {
            System.out.println("Name: " + registers[i].getName());
            System.out.println("Number: " + registers[i].getNumber());
            System.out.println("Value: " + registers[i].getValue());
            System.out.println("");
        }
    }

    public static int updateRegister(int num, int val) {
        int old = 0;
        for (int i = 0; i < registers.length; i++) {
            if (registers[i].getNumber() == num) {
                if (num == 0) {
                    val = val & 0xF; // CTRL only has 4 bits
                }
                old = registers[i].setValue(val);
                break;
            }
        }
        TimerTwo.updateIRQ();
        return old;
    }

    public static void update() {
        if (!enable) {
            return;
        }
        switch (state) {
            case IDLE:
                if ((registers[CTRL].getValue() & 1) != 0) {
                    state = LOAD;
                    IRQ = 0;
                } else {
                    state = IDLE;
                }
                break;
            case LOAD:
                state = CNT;
                updateRegister(COUNT, registers[PRESET].getValue());
                break;
            case CNT:
                if ((registers[CTRL].getValue() & 1) == 0) {
                    state = IDLE;
                    break;
                }
                if (registers[COUNT].getValue() <= 1) {
                    state = INT;
                    IRQ = 1;
                } else {
                    state = CNT;
                }
                updateRegister(COUNT, registers[COUNT].getValue() - 1);
                break;
            case INT:
                if ((registers[CTRL].getValue() & 6) == 0) {
                    // Mode 00: stop, clear enable
                    updateRegister(CTRL, registers[CTRL].getValue() & 0xFFFFFFFE);
                    state = IDLE;
                } else {
                    // Mode 01/10/11: clear IRQ, auto-restart (ctrl[0] stays set)
                    IRQ = 0;
                    state = IDLE;
                }
                break;
        }
        enable = false;
        TimerTwo.updateIRQ();
    }

    public static void setEnable(boolean bl) {
        enable = bl;
    }

    private static void updateIRQ() {
        if (IRQ != 0 && (registers[CTRL].getValue() & 8) != 0) {
            Globals.HWInt |= 2;
        } else {
            Globals.HWInt &= 0xFFFFFFFD;
        }
    }

    public static int getValue(int num) {
        for (int i = 0; i < registers.length; i++) {
            if (registers[i].getNumber() == num) {
                return registers[i].getValue();
            }
        }
        return 0;
    }

    public static Register[] getRegisters() {
        return registers;
    }

    public static int getRegisterPosition(Register r) {
        for (int i = 0; i < registers.length; i++) {
            if (registers[i] == r) {
                return i;
            }
        }
        return -1;
    }

    public static void resetRegisters() {
        for (int i = 0; i < registers.length; i++) {
            registers[i].resetValue();
        }
        state = 0;
        IRQ = 0;
    }

    public static void addRegistersObserver(Observer observer) {
        for (int i = 0; i < registers.length; i++) {
            registers[i].addObserver(observer);
        }
    }

    public static void deleteRegistersObserver(Observer observer) {
        for (int i = 0; i < registers.length; i++) {
            registers[i].deleteObserver(observer);
        }
    }
}

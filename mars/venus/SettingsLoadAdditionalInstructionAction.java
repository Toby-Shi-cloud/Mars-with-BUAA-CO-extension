package mars.venus;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import mars.mips.instructions.LoadInstruction;

import java.awt.event.ActionEvent;
import java.io.File;

public class SettingsLoadAdditionalInstructionAction extends GuiAction {
    private String title;

    public SettingsLoadAdditionalInstructionAction(String name, Icon icon, String descrip, Integer mnemonic,
            KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
        title = descrip;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        loadInstruction();
    }

    private boolean loadInstruction() {
        File theFile = null;
        JFileChooser loadDialog = null;
        boolean operationOK = false;

        loadDialog = new JFileChooser(mainUI.getEditor().getCurrentSaveDirectory());
        loadDialog.setDialogTitle(title);
        loadDialog.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return ".class - Addition Instruction .class File";
            }

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".class");
            }
        });
        while (!operationOK) {
            int decision = loadDialog.showOpenDialog(mainUI);
            if (decision != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            theFile = loadDialog.getSelectedFile();
            operationOK = false;
            if (theFile.exists()) {
                try {
                    LoadInstruction.loadClass(theFile.getAbsolutePath());
                    operationOK = true;
                } catch (ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(
                        mainUI, "Load Instruction Failed!",
                        title, JOptionPane.ERROR_MESSAGE
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                    mainUI, "File NOT Found!",
                    title, JOptionPane.ERROR_MESSAGE
                );
            }
            if (operationOK) {
                JOptionPane.showMessageDialog(
                    mainUI, "Load Instruction Successfully!",
                    title, JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
        return true;
    }
}

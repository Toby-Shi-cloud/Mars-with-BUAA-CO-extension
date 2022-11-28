package mars.venus;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import mars.Globals;

import java.awt.event.ActionEvent;

public class SettingsIgnoreArithmeticOverflowAction extends GuiAction {

    public SettingsIgnoreArithmeticOverflowAction(String name, Icon icon, String descrip, Integer mnemonic, KeyStroke accel,
            VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Globals.ignoreArithmeticOverflow = ((JCheckBoxMenuItem) e.getSource()).isSelected();
    }
}

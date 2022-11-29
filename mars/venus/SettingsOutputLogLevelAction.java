package mars.venus;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import mars.Globals;

import java.awt.event.ActionEvent;

public class SettingsOutputLogLevelAction extends GuiAction {
    private JCheckBoxMenuItem associated;
    private int level;

    public SettingsOutputLogLevelAction(String name, Icon icon, String descrip, Integer mnemonic, KeyStroke accel,
            VenusUI gui, JCheckBoxMenuItem associated, int level) {
        super(name, icon, descrip, mnemonic, accel, gui);
        this.associated = associated;
        this.level = level;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
        if (selected) {
            Globals.displayLevel = level;
            if (associated != null) {
                associated.setSelected(false);
            }
        } else {
            if (Globals.displayLevel == level) {
                Globals.displayLevel = 0;
            }
        }
    }
}

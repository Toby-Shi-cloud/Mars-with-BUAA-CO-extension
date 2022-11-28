package mars.venus;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

public class SettingsLoadAdditionalInstructionAction extends GuiAction {

    public SettingsLoadAdditionalInstructionAction(String name, Icon icon, String descrip, Integer mnemonic,
            KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO do something to load instruction
    }
}

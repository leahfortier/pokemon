package mapMaker.dialogs;

import main.Global;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Dimension;

public class TriggerDialog extends JPanel {
    public boolean giveOption(String name, JComponent parent) {
        Object[] options = { "Save or Whatever", "Cancel" };
        int results = JOptionPane.showOptionDialog(
                parent,
                this,
                name,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        return results == JOptionPane.YES_OPTION;
    }

    protected void setPanelSize() {
        Dimension d = Global.GAME_SIZE;
        this.setMinimumSize(d);
        this.setPreferredSize(d);
        this.setMaximumSize(d);
    }
}

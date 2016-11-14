package mapMaker.dialogs;

import main.Global;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Dimension;

public abstract class TriggerDialog<T> extends JPanel {
    public abstract T getMatcher();
    protected abstract void load(T matcher);

    public void loadMatcher(T matcher) {
        if (matcher != null) {
            this.load(matcher);
            this.render();
        }
    }

    protected void renderDialog() {}

    protected final void render() {
        this.renderDialog();

        this.setPanelSize();
        this.revalidate();
    }

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
        Dimension dimension = Global.GAME_SIZE;
        this.setMinimumSize(dimension);
        this.setPreferredSize(dimension);
        this.setMaximumSize(dimension);
    }
}

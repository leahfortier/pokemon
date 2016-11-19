package mapMaker.dialogs;

import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class TriggerDialog<T> extends JPanel {
    private DialogOptions dialogOptions;

    public abstract T getMatcher();
    protected abstract void load(T matcher);

    public void loadMatcher(T matcher) {
        if (matcher != null) {
            this.load(matcher);
            this.render();
        }
    }

    protected void renderDialog() {}

    public final void render() {
        this.removeAll();
        this.renderDialog();

        if (dialogOptions != null) {
            dialogOptions.render(this);
        }
    }

    public boolean giveOption(String name, JComponent parent) {
        this.dialogOptions = new DialogOptions(name, parent);
        this.render();
        this.dialogOptions.setVisible(true);

        boolean isSaved = this.dialogOptions.isSaved();
        this.dialogOptions = null;

        return isSaved;
    }
}

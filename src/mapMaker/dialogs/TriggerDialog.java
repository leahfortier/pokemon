package mapMaker.dialogs;

import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class TriggerDialog<T> extends JPanel {
    private final String dialogTitle;
    private DialogOptions dialogOptions;

    protected TriggerDialog(final String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    protected void renderDialog() {}

    public final void render() {
        this.removeAll();
        this.renderDialog();

        if (dialogOptions != null) {
            dialogOptions.render(this);
        }
    }

    public T getMatcher(JComponent parent) {
        this.dialogOptions = new DialogOptions(this.dialogTitle, parent);
        this.render();
        this.dialogOptions.setVisible(true);

        boolean isSaved = this.dialogOptions.isSaved();
        this.dialogOptions = null;

        return isSaved ? this.getMatcher() : null;
    }

    protected abstract T getMatcher();
}

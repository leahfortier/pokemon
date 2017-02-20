package mapMaker.dialogs.action.trigger;

import javax.swing.JPanel;

public abstract class TriggerContentsPanel extends JPanel {
    protected abstract void load(String triggerContents);
    protected abstract String getTriggerContents();
}

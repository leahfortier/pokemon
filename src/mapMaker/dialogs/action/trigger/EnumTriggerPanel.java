package mapMaker.dialogs.action.trigger;

import util.GUIUtils;

import javax.swing.JComboBox;

class EnumTriggerPanel<T extends Enum> extends TriggerContentsPanel {
    private final JComboBox<T> combobBox; // Not a typo
    private final T[] values;

    EnumTriggerPanel(String label, T[] values) {
        this.combobBox = GUIUtils.createComboBox(values);
        this.values = values;

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createComboBoxComponent(label, this.combobBox)
        );
    }

    private T valueOf(String name) {
        for (T value : values) {
            if (value.name().equals(name)) {
                return value;
            }
        }

        return null;
    }

    @Override
    protected void load(String triggerContents) {
        this.combobBox.setSelectedItem(valueOf(triggerContents));
    }

    @Override
    protected String getTriggerContents() {
        return ((T)this.combobBox.getSelectedItem()).name();
    }
}

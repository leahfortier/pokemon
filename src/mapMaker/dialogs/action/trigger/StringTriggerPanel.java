package mapMaker.dialogs.action.trigger;

import util.GUIUtils;

import javax.swing.JTextField;

class StringTriggerPanel extends TriggerContentsPanel {
    final JTextField textField;

    StringTriggerPanel(String label) {
        this.textField = GUIUtils.createTextField();

        GUIUtils.setVerticalLayout(this, GUIUtils.createTextFieldComponent(label, this.textField));
    }

    @Override
    protected void load(String triggerContents) {
        this.textField.setText(triggerContents);
    }

    @Override
    protected String getTriggerContents() {
        return this.textField.getText();
    }
}

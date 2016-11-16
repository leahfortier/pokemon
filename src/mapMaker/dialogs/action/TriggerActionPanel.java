package mapMaker.dialogs.action;

import map.triggers.TriggerType;
import pattern.action.ActionMatcher;
import pattern.action.TriggerActionMatcher;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;

class TriggerActionPanel extends ActionPanel {
    private JComboBox<TriggerType> triggerTypeCombobBox; // Not a typo
    private JTextField triggerContentsTextField;

    TriggerActionPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        triggerTypeCombobBox = new JComboBox<>(TriggerType.values());
        this.add(triggerTypeCombobBox);

        triggerContentsTextField = new JTextField();
        triggerContentsTextField.setColumns(10);
        this.add(triggerContentsTextField);
    }

    @Override
    protected void load(ActionMatcher matcher) {
        TriggerActionMatcher actionMatcher = matcher.getTrigger();
        triggerTypeCombobBox.setSelectedItem(actionMatcher.getTriggerType());
        triggerContentsTextField.setText(actionMatcher.triggerContents);
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        TriggerType triggerType = (TriggerType) triggerTypeCombobBox.getSelectedItem();
        String triggerContents = triggerContentsTextField.getText();
        TriggerActionMatcher triggerActionMatcher = new TriggerActionMatcher(triggerType, triggerContents);

        ActionMatcher actionMatcher = new ActionMatcher();
        actionMatcher.setTrigger(triggerActionMatcher);

        return actionMatcher;
    }
}
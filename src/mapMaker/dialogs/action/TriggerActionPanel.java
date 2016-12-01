package mapMaker.dialogs.action;

import map.triggers.TriggerType;
import pattern.action.ActionMatcher;
import pattern.action.TriggerActionMatcher;
import util.GUIUtils;

import javax.swing.JComboBox;
import javax.swing.JTextArea;

class TriggerActionPanel extends ActionPanel {
    private JComboBox<TriggerType> triggerTypeCombobBox; // Not a typo
    private JTextArea triggerContentsTextArea;

    TriggerActionPanel() {
        triggerTypeCombobBox = GUIUtils.createComboBox(TriggerType.values(), null);
        triggerContentsTextArea = GUIUtils.createTextArea();

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createComboBoxComponent("Trigger Type", triggerTypeCombobBox),
                GUIUtils.createTextAreaComponent("Trigger Contents", triggerContentsTextArea)
        );
    }

    @Override
    protected void load(ActionMatcher matcher) {
        TriggerActionMatcher actionMatcher = matcher.getTrigger();
        triggerTypeCombobBox.setSelectedItem(actionMatcher.getTriggerType());
        triggerContentsTextArea.setText(actionMatcher.getTriggerContents());
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        TriggerType triggerType = (TriggerType) triggerTypeCombobBox.getSelectedItem();
        String triggerContents = triggerContentsTextArea.getText();
        TriggerActionMatcher triggerActionMatcher = new TriggerActionMatcher(triggerType, triggerContents);

        ActionMatcher actionMatcher = new ActionMatcher();
        actionMatcher.setTrigger(triggerActionMatcher);

        return actionMatcher;
    }
}

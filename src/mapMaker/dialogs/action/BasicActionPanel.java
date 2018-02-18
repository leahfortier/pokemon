package mapMaker.dialogs.action;

import main.Global;
import pattern.action.ActionMatcher2;
import pattern.action.ActionMatcher2.GlobalActionMatcher;
import pattern.action.ActionMatcher2.GroupTriggerActionMatcher;
import pattern.action.ActionMatcher2.UpdateActionMatcher;
import util.GUIUtils;

import javax.swing.JTextField;

// For actions which only have a string field
class BasicActionPanel extends ActionPanel {
    private final JTextField textField;

    BasicActionPanel(String actionName) {
        textField = new JTextField();

        GUIUtils.setHorizontalLayout(
                this,
                GUIUtils.createTextFieldComponent(actionName, textField)
        );
    }

    @Override
    protected void load(ActionMatcher2 matcher) {
        textField.setText(matcher.getActionString());
    }

    @Override
    public ActionMatcher2 getActionMatcher(ActionType actionType) {
        String text = textField.getText().trim();

        switch (actionType) {
            case UPDATE:
                return new UpdateActionMatcher(text);
            case GROUP_TRIGGER:
                return new GroupTriggerActionMatcher(text);
            case GLOBAL:
                return new GlobalActionMatcher(text);
            default:
                Global.info("Invalid action type for basic panel... :(");
                return null;
        }
    }
}

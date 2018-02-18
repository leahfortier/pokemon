package mapMaker.dialogs.action;

import main.Global;
import pattern.action.ActionMatcher;
import pattern.action.StringActionMatcher;
import pattern.action.StringActionMatcher.GlobalActionMatcher;
import pattern.action.StringActionMatcher.GroupTriggerActionMatcher;
import pattern.action.StringActionMatcher.UpdateActionMatcher;
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
    protected void load(ActionMatcher matcher) {
        StringActionMatcher stringActionMatcher = (StringActionMatcher)matcher;
        textField.setText(stringActionMatcher.getActionString());
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
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

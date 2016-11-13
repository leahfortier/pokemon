package mapMaker.dialogs.action;

import main.Global;
import pattern.AreaDataMatcher.ActionMatcher;
import util.StringUtils;

import javax.swing.JTextField;

// TODO: honestly this is stupid and each one should just have their own shit who cares fuck it I hate these switches
// For actions which only have a string field
class BasicActionPanel extends ActionPanel {
    private JTextField textField;

    BasicActionPanel() {
        textField = new JTextField();
        textField.setColumns(10);
        this.add(textField);
    }

    @Override
    protected void load(ActionMatcher matcher) {
        ActionType actionType = matcher.getActionType();
        String text = StringUtils.empty();
        switch (actionType) {
            case UPDATE:
                text = matcher.update;
                break;
            case GROUP_TRIGGER:
                text = matcher.groupTrigger;
                break;
            case GLOBAL:
                text = matcher.global;
                break;
            default:
                Global.error("Invalid action type " + actionType);
                break;
        }

        textField.setText(text);
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        ActionMatcher actionMatcher = new ActionMatcher();
        String text = textField.getText().trim();

        switch (actionType) {
            case UPDATE:
                actionMatcher.update = text;
                break;
            case GROUP_TRIGGER:
                actionMatcher.groupTrigger = text;
                break;
            case GLOBAL:
                actionMatcher.global = text;
                break;
            default:
                Global.error("Invalid action type " + actionType);
                break;
        }

        return actionMatcher;
    }
}

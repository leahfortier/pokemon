package mapMaker.dialogs.action;

import pattern.action.ActionMatcher;

import javax.swing.JTextField;

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
        textField.setText(matcher.getActionString());
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        String text = textField.getText().trim();

        ActionMatcher actionMatcher = new ActionMatcher();
        actionMatcher.setActionString(text, actionType);

        return actionMatcher;
    }
}

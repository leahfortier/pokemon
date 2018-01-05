package mapMaker.dialogs.action;

import pattern.action.ActionMatcher;
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

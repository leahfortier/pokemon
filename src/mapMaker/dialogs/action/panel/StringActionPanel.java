package mapMaker.dialogs.action.panel;

import item.ItemNamesies;
import main.Global;
import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.UseItemActionMatcher;
import pattern.action.StringActionMatcher;
import pattern.action.StringActionMatcher.DialogueActionMatcher;
import pattern.action.StringActionMatcher.GlobalActionMatcher;
import pattern.action.StringActionMatcher.GroupTriggerActionMatcher;
import pattern.action.StringActionMatcher.MovePlayerActionMatcher;
import pattern.action.StringActionMatcher.UpdateActionMatcher;
import util.ColorDocumentListener;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JComponent;
import javax.swing.JTextField;

// For actions which only have a string field
public class StringActionPanel extends ActionPanel {
    protected final JTextField textField;

    public StringActionPanel(String actionName) {
        textField = new JTextField();

        GUIUtils.setHorizontalLayout(
                this,
                GUIUtils.createTextFieldComponent(actionName, textField)
        );
    }

    @Override
    protected void load(ActionMatcher matcher) {
        StringActionMatcher stringActionMatcher = (StringActionMatcher)matcher;
        textField.setText(stringActionMatcher.getStringValue());
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        String text = textField.getText().trim();

        switch (actionType) {
            case DIALOGUE:
                return new DialogueActionMatcher(text);
            case GLOBAL:
                return new GlobalActionMatcher(text);
            case GROUP_TRIGGER:
                return new GroupTriggerActionMatcher(text);
            case MOVE_PLAYER:
                return new MovePlayerActionMatcher(text);
            case UPDATE:
                return new UpdateActionMatcher(text);
            default:
                Global.info("Invalid action type for basic panel... :(");
                return null;
        }
    }

    public static class ItemActionPanel extends StringActionPanel {
        public ItemActionPanel() {
            super("Item Name");

            ColorCondition colorCondition = () -> ItemNamesies.tryValueOf(textField.getText().trim()) != null;
            textField.getDocument().addDocumentListener(new ColorDocumentListener(colorCondition) {
                @Override
                protected JComponent colorComponent() {
                    return textField;
                }
            });
        }

        @Override
        protected void load(ActionMatcher matcher) {
            UseItemActionMatcher itemMatcher = (UseItemActionMatcher)matcher;
            textField.setText(itemMatcher.getItem().getName());
        }

        @Override
        public ActionMatcher getActionMatcher(ActionType actionType) {
            String text = textField.getText().trim();
            return new UseItemActionMatcher(ItemNamesies.getValueOf(text));
        }
    }
}

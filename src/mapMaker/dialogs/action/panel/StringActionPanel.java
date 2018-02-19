package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionPanel;
import pattern.action.StringActionMatcher;
import util.GUIUtils;

import javax.swing.JTextField;
import java.util.function.Function;

// For actions which only have a string field
public class StringActionPanel extends ActionPanel<StringActionMatcher> {
    private final JTextField textField;

    private final Function<String, StringActionMatcher> actionMatcherGetter;

    public StringActionPanel(String actionName, Function<String, StringActionMatcher> actionMatcherGetter) {
        this.actionMatcherGetter = actionMatcherGetter;

        textField = new JTextField();
        GUIUtils.setHorizontalLayout(
                this,
                GUIUtils.createTextFieldComponent(actionName, textField)
        );
    }

    @Override
    protected void load(StringActionMatcher matcher) {
        textField.setText(matcher.getStringValue());
    }

    @Override
    public StringActionMatcher getActionMatcher() {
        String text = textField.getText().trim();
        return this.actionMatcherGetter.apply(text);
    }
}

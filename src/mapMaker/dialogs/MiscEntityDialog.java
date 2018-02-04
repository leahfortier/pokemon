package mapMaker.dialogs;

import map.condition.Condition.GlobalCondition;
import mapMaker.dialogs.action.ActionListPanel;
import pattern.action.ActionMatcher;
import pattern.map.MiscEntityMatcher;
import util.GUIUtils;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MiscEntityDialog extends TriggerDialog<MiscEntityMatcher> {

    private final JPanel topComponent;

    private final JTextField nameTextField;
    private final JTextArea conditionTextArea;
    private final ActionListPanel actionListPanel;

    public MiscEntityDialog(MiscEntityMatcher matcher) {
        super("Misc Trigger Editor");

        this.nameTextField = GUIUtils.createTextField();
        this.conditionTextArea = GUIUtils.createTextArea();
        this.actionListPanel = new ActionListPanel(this);

        JPanel nameComponent = GUIUtils.createTextFieldComponent("Name", nameTextField);
        JPanel conditionComponent = GUIUtils.createTextAreaComponent("Condition", conditionTextArea);

        this.topComponent = GUIUtils.createVerticalLayoutComponent(nameComponent, conditionComponent);

        this.load(matcher);
    }

    @Override
    public void renderDialog() {
        removeAll();
        GUIUtils.setVerticalLayout(this, topComponent, actionListPanel);
    }

    @Override
    protected MiscEntityMatcher getMatcher() {
        ActionMatcher[] actions = actionListPanel.getActions();
        if (actions == null || actions.length == 0) {
            System.err.println("Need at least one action for a valid misc entity.");
            return null;
        }

        return new MiscEntityMatcher(
                this.getNameField(nameTextField),
                // TODO: PLACEHOLDER
                new GlobalCondition(conditionTextArea.getText()),
                actions
        );
    }

    private void load(MiscEntityMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getBasicName());
        // TODO: PLACEHOLDER
        conditionTextArea.setText(matcher.getCondition().toString());
        actionListPanel.load(matcher.getActionMatcherList());
    }
}

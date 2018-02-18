package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.action.ActionMatcher2;
import pattern.map.MiscEntityMatcher;
import util.GUIUtils;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class MiscEntityDialog extends TriggerDialog<MiscEntityMatcher> {

    private final JPanel topComponent;

    private final JTextField nameTextField;
    private final ConditionPanel conditionPanel;
    private final ActionListPanel actionListPanel;

    public MiscEntityDialog(MiscEntityMatcher matcher) {
        super("Misc Trigger Editor");

        this.nameTextField = GUIUtils.createTextField();
        this.conditionPanel = new ConditionPanel();
        this.actionListPanel = new ActionListPanel(this);

        JPanel nameComponent = GUIUtils.createTextFieldComponent("Name", nameTextField);

        this.topComponent = GUIUtils.createVerticalLayoutComponent(nameComponent, conditionPanel);

        this.load(matcher);
    }

    @Override
    public void renderDialog() {
        removeAll();
        GUIUtils.setVerticalLayout(this, topComponent, actionListPanel);
    }

    @Override
    protected MiscEntityMatcher getMatcher() {
        ActionMatcher2[] actions = actionListPanel.getActions();
        if (actions == null || actions.length == 0) {
            System.err.println("Need at least one action for a valid misc entity.");
            return null;
        }

        return new MiscEntityMatcher(
                this.getNameField(nameTextField),
                conditionPanel.getConditionName(),
                conditionPanel.getConditionSet(),
                actions
        );
    }

    private void load(MiscEntityMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getBasicName());
        conditionPanel.load(matcher);
        actionListPanel.load(matcher.getActionMatcherList());
    }
}

package mapMaker.dialogs;

import main.Global;
import mapMaker.dialogs.action.ActionListPanel;
import pattern.action.ActionMatcher;
import pattern.map.EventMatcher;
import util.GuiUtils;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class EventTriggerDialog extends TriggerDialog<EventMatcher> {
    private final JPanel topComponent;

    private final JTextField nameTextField;
    private final ConditionPanel conditionPanel;
    private final ActionListPanel actionListPanel;

    public EventTriggerDialog(EventMatcher eventMatcher) {
        super("Event Trigger Editor");

        this.nameTextField = GuiUtils.createTextField();
        this.conditionPanel = new ConditionPanel();
        this.actionListPanel = new ActionListPanel(this);

        JPanel nameComponent = GuiUtils.createTextFieldComponent("Name", nameTextField);

        this.topComponent = GuiUtils.createVerticalLayoutComponent(nameComponent, conditionPanel);

        this.load(eventMatcher);
    }

    @Override
    public void renderDialog() {
        removeAll();
        GuiUtils.setVerticalLayout(this, topComponent, actionListPanel);
    }

    @Override
    protected EventMatcher getMatcher() {
        ActionMatcher[] actions = actionListPanel.getActions();
        if (actions == null || actions.length == 0) {
            Global.info("Need at least one action for a valid event trigger.");
            return null;
        }

        return new EventMatcher(
                this.getNameField(nameTextField),
                conditionPanel.getConditionName(),
                conditionPanel.getConditionSet(),
                actions
        );
    }

    private void load(EventMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getBasicName());
        conditionPanel.load(matcher);
        actionListPanel.load(matcher.getActions());
    }
}

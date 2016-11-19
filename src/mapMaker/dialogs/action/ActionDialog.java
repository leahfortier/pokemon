package mapMaker.dialogs.action;

import mapMaker.dialogs.TriggerDialog;
import pattern.action.ActionMatcher;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.util.EnumMap;
import java.util.Map;

class ActionDialog extends TriggerDialog<ActionMatcher> {
    private Map<ActionType, ActionPanel> map;

    private JPanel panel;

    private JComboBox<ActionType> actionComboBox;
    private JPanel actionPanel;

    ActionDialog(ActionMatcher actionMatcher) {
        super("New Action Dialog");

        this.map = new EnumMap<>(ActionType.class);
        for (ActionType action : ActionType.values()) {
            this.map.put(action, action.createActionData());
        }

        actionComboBox = new JComboBox<>(ActionType.values());
        actionComboBox.addActionListener(event -> {
            setActionPanelToSelectedAction();
            render();
        });

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(actionComboBox);

        add(panel);

        setActionPanelToSelectedAction();

        this.load(actionMatcher);
    }

    private void setActionPanelToSelectedAction() {
        if (actionPanel != null) {
            panel.remove(actionPanel);
        }

        ActionType selectedAction = (ActionType) actionComboBox.getSelectedItem();
        actionPanel = map.get(selectedAction);
    }

    @Override
    protected ActionMatcher getMatcher() {
        ActionType actionType = (ActionType) actionComboBox.getSelectedItem();
        return this.map.get(actionType).getActionMatcher(actionType);
    }

    private void load(ActionMatcher matcher) {
        if (matcher == null) {
            return;
        }

        ActionType actionType = matcher.getActionType();
        actionComboBox.setSelectedItem(actionType);
        map.get(actionType).load(matcher);
    }

    @Override
    protected void renderDialog() {
        if (actionPanel != null) {
            panel.add(actionPanel);
        }
    }
}

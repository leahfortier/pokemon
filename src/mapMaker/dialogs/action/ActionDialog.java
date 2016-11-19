package mapMaker.dialogs.action;

import mapMaker.dialogs.TriggerDialog;
import pattern.action.ActionMatcher;
import util.GUIUtils;

import javax.swing.JComboBox;
import java.util.EnumMap;
import java.util.Map;

class ActionDialog extends TriggerDialog<ActionMatcher> {
    private final Map<ActionType, ActionPanel> map;

    private final JComboBox<ActionType> actionComboBox;

    ActionDialog(ActionMatcher actionMatcher) {
        super("New Action Dialog");

        this.map = new EnumMap<>(ActionType.class);
        for (ActionType action : ActionType.values()) {
            this.map.put(action, action.createActionData());
        }

        actionComboBox = new JComboBox<>(ActionType.values());
        actionComboBox.addActionListener(event -> {
            render();
        });

        this.actionComboBox.setSelectedIndex(0);

        this.load(actionMatcher);
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
        removeAll();

        ActionType selectedAction = (ActionType) actionComboBox.getSelectedItem();
        GUIUtils.setVerticalLayout(this, actionComboBox, map.get(selectedAction));
    }
}

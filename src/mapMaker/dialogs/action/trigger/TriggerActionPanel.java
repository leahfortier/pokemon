package mapMaker.dialogs.action.trigger;

import mapMaker.dialogs.action.ActionDialog;
import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.TriggerActionMatcher;
import util.GUIUtils;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.util.EnumMap;
import java.util.Map;

public class TriggerActionPanel extends ActionPanel {
    private final JPanel topComponent;

    private final Map<TriggerActionType, TriggerContentsPanel> map;
    private final JComboBox<TriggerActionType> triggerTypeCombobBox; // Not a typo

    private final ActionDialog parentDialog;

    public TriggerActionPanel(ActionDialog actionDialog) {
        this.parentDialog = actionDialog;
        this.triggerTypeCombobBox = GUIUtils.createComboBox(TriggerActionType.values(), event -> renderDialog());
        this.topComponent = GUIUtils.createHorizontalLayoutComponent(GUIUtils.createComboBoxComponent("Trigger Type", triggerTypeCombobBox));

        this.map = new EnumMap<>(TriggerActionType.class);
        for (TriggerActionType triggerActionType : TriggerActionType.values()) {
            this.map.put(triggerActionType, triggerActionType.createPanel());
        }

        GUIUtils.setVerticalLayout(this, topComponent);
        this.triggerTypeCombobBox.setSelectedIndex(0);
    }

    @Override
    protected void load(ActionMatcher matcher) {
        TriggerActionMatcher actionMatcher = (TriggerActionMatcher)matcher;
        TriggerActionType triggerActionType = actionMatcher.getTriggerActionType();
        triggerTypeCombobBox.setSelectedItem(triggerActionType);
        this.map.get(triggerActionType).load(actionMatcher.getTriggerContents());
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        TriggerActionType triggerType = (TriggerActionType)triggerTypeCombobBox.getSelectedItem();
        String triggerContents = this.map.get(triggerType).getTriggerContents();
        return new TriggerActionMatcher(triggerType.getTriggerType(), triggerContents, null);
    }

    private void renderDialog() {
        removeAll();

        TriggerActionType selectedAction = (TriggerActionType)triggerTypeCombobBox.getSelectedItem();
        GUIUtils.setVerticalLayout(this, topComponent, map.get(selectedAction));
        parentDialog.render();
    }
}

package mapMaker.dialogs.action;

import mapMaker.dialogs.TriggerDialog;
import pattern.action.ActionMatcher2;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

public class ActionListPanel extends JPanel {
    private final TriggerDialog parent;

    private final List<ActionMatcher2> actionList;
    private final JButton newActionButton;

    public ActionListPanel(TriggerDialog parent) {
        this.parent = parent;

        actionList = new ArrayList<>();

        newActionButton = GUIUtils.createButton(
                "New Action",
                event -> {
                    actionList.add(null);
                    render();
                }
        );

        render();
    }

    public void load(List<ActionMatcher2> actions) {
        actionList.addAll(actions);
        render();
    }

    private void render() {
        removeAll();

        List<JComponent> components = new ArrayList<>();

        for (int i = 0; i < actionList.size(); i++) {
            final int index = i;
            final ActionMatcher2 actionMatcher = actionList.get(index);

            JButton actionButton = GUIUtils.createButton(
                    actionMatcher == null ? "EMPTY" : actionMatcher.getActionType().name(),
                    event -> {
                        ActionMatcher2 newActionMatcher = new ActionDialog(actionMatcher).getMatcher(parent);
                        if (newActionMatcher != null) {
                            actionList.set(index, newActionMatcher);
                            render();
                        }
                    }
            );

            JButton deleteButton = GUIUtils.createButton(
                    "Delete",
                    event -> {
                        actionList.remove(index);
                        render();
                    }
            );

            components.add(GUIUtils.createHorizontalLayoutComponent(actionButton, deleteButton));
        }

        components.add(newActionButton);

        GUIUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));

        parent.render();
    }

    public ActionMatcher2[] getActions() {
        return this.actionList.toArray(new ActionMatcher2[0]);
    }
}

package mapMaker.dialogs.action;

import mapMaker.dialogs.TriggerDialog;
import pattern.action.ActionList;
import pattern.action.ActionMatcher;
import util.GuiUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActionListPanel extends JPanel {
    private final TriggerDialog parent;

    private final List<ActionMatcher> actionList;
    private final JButton newActionButton;

    public ActionListPanel(TriggerDialog parent) {
        this.parent = parent;

        actionList = new ArrayList<>();

        newActionButton = GuiUtils.createButton(
                "New Action",
                event -> {
                    actionList.add(null);
                    render();
                }
        );

        render();
    }

    public void load(ActionList actions) {
        actionList.addAll(actions.asList());
        render();
    }

    private void render() {
        removeAll();

        List<JComponent> components = new ArrayList<>();

        for (int i = 0; i < actionList.size(); i++) {
            final int index = i;
            final ActionMatcher actionMatcher = actionList.get(index);

            JButton actionButton = GuiUtils.createButton(
                    actionMatcher == null ? "EMPTY" : actionMatcher.getActionType().name(),
                    event -> {
                        ActionMatcher newActionMatcher = new ActionDialog(actionMatcher).getMatcher(parent);
                        if (newActionMatcher != null) {
                            actionList.set(index, newActionMatcher);
                            render();
                        }
                    }
            );

            JButton deleteButton = GuiUtils.createButton(
                    "Delete",
                    event -> {
                        actionList.remove(index);
                        render();
                    }
            );

            components.add(GuiUtils.createHorizontalLayoutComponent(actionButton, deleteButton));
        }

        components.add(newActionButton);

        GuiUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));

        parent.render();
    }

    public ActionMatcher[] getActions() {
        this.actionList.removeIf(Objects::isNull);
        return this.actionList.toArray(new ActionMatcher[0]);
    }
}

package mapMaker.dialogs.action;

import mapMaker.dialogs.TriggerDialog;
import pattern.action.ActionMatcher;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

public class ActionListPanel extends JPanel {
    private final TriggerDialog parent;

    private final List<ActionMatcher> actionList;
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

    public void load(List<ActionMatcher> actions) {
        actionList.addAll(actions);
        render();
    }

    public void render() {
        removeAll();

        List<JComponent> components = new ArrayList<>();

        for (int i = 0; i < actionList.size(); i++) {
            final int index = i;

            JButton actionButton = GUIUtils.createButton(
                    "Action",
                    event -> {
                        ActionMatcher actionMatcher = actionList.get(index);
                        ActionMatcher newActionMatcher = new ActionDialog(actionMatcher).getMatcher();
                        if (newActionMatcher != null) {
                            actionList.set(index, newActionMatcher);
                        }
                    }
            );

            components.add(actionButton);
        }

        components.add(newActionButton);

        GUIUtils.setHorizontalLayout(this, components.toArray(new JComponent[0]));

        parent.render();
    }

    public ActionMatcher[] getActions() {
        return this.actionList.toArray(new ActionMatcher[0]);
    }
}

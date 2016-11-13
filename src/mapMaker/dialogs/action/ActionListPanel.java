package mapMaker.dialogs.action;

import pattern.AreaDataMatcher.ActionMatcher;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionListPanel extends JPanel {
    private List<ActionMatcher> actionList;
    private JButton newActionButton;

    public ActionListPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        actionList = new ArrayList<>();
        newActionButton = new JButton("New Action");
        newActionButton.addActionListener(event -> {
            actionList.add(null);
            render();
        });

        render();
    }

    public void load(ActionMatcher[] actions) {
        actionList.addAll(Arrays.asList(actions));
    }

    public void render() {
        removeAll();

        for (int i = 0; i < actionList.size(); i++) {
            final int index = i;

            JButton actionButton = new JButton("Action");
            actionButton.addActionListener(event -> {
                ActionDialog actionDialog = new ActionDialog();
                ActionMatcher actionMatcher = actionList.get(index);
                actionDialog.loadMatcher(actionMatcher);

                if (actionDialog.giveOption("New Action Dialog", this)) {
                    ActionMatcher newActionMatcher = actionDialog.getMatcher();
                    actionList.set(index, newActionMatcher);
                }
            });

            add(actionButton);
        }

        add(newActionButton);
        revalidate();
    }

    public ActionMatcher[] getActions() {
        return this.actionList.toArray(new ActionMatcher[0]);
    }
}

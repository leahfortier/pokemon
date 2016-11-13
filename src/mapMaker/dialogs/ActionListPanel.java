package mapMaker.dialogs;

import pattern.AreaDataMatcher.ActionMatcher;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

public class ActionListPanel extends JPanel {
    private List<ActionMatcher> actionList;
    private JButton newActionButton;

    public ActionListPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        actionList = new ArrayList<>();
        newActionButton = new JButton("New Action");
        newActionButton.addActionListener(event -> {
            actionList.add(new ActionMatcher());
            render();
        });

        render();
    }

    private void render() {
        removeAll();

        for (ActionMatcher matcher : actionList) {
            JButton actionButton = new JButton("Action");
            actionButton.addActionListener(event -> {
                new ActionDialog().giveOption("New Action Dialog", this);
            });
            add(actionButton);
        }

        add(newActionButton);
        revalidate();
    }
}

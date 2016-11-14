package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.ActionMatcher;
import pattern.NPCInteractionMatcher;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NPCInteractionDialog extends TriggerDialog<NPCInteractionMatcher> {
    private JTextField interactionNameTextField;
    private JCheckBox walkToPlayerCheckBox;
    private ActionListPanel actionListPanel;

    public NPCInteractionDialog() {
        interactionNameTextField = new JTextField();
        walkToPlayerCheckBox = new JCheckBox("Walk to playa");
        this.actionListPanel = new ActionListPanel();

        render();
    }

    @Override
    public void load(NPCInteractionMatcher matcher) {
        interactionNameTextField.setText(matcher.getName());
        walkToPlayerCheckBox.setSelected(matcher.shouldWalkToPlayer());
        actionListPanel.load(matcher.getActionMatcherList());
    }

    @Override
    public NPCInteractionMatcher getMatcher() {
        String interactionName = interactionNameTextField.getText();
        boolean walkToPlayer = walkToPlayerCheckBox.isSelected();
        ActionMatcher[] actions = actionListPanel.getActions();

        return new NPCInteractionMatcher(interactionName, walkToPlayer, actions);
    }

    @Override
    protected void renderDialog() {
        actionListPanel.render();
        removeAll();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(interactionNameTextField);
        panel.add(walkToPlayerCheckBox);
        panel.add(actionListPanel);
        add(panel);
    }
}

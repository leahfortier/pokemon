package mapMaker.dialogs;

import mapMaker.dialogs.action.ActionListPanel;
import pattern.action.ActionMatcher;
import pattern.action.NPCInteractionMatcher;
import util.GuiUtils;
import util.string.StringUtils;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

class NPCInteractionDialog extends TriggerDialog<NPCInteractionMatcher> {
    private final JPanel topComponent;

    private final JTextField interactionNameTextField;
    private final JCheckBox walkToPlayerCheckBox;
    private final ActionListPanel actionListPanel;

    private final int interactionIndex;

    NPCInteractionDialog(NPCInteractionMatcher npcInteractionMatcher, int index) {
        super("New NPC Interaction Dialog");

        this.interactionIndex = index;

        interactionNameTextField = GuiUtils.createTextField(this.getDefaultName());
        walkToPlayerCheckBox = GuiUtils.createCheckBox("Walk to playa");
        this.actionListPanel = new ActionListPanel(this);

        this.topComponent = GuiUtils.createHorizontalLayoutComponent(
                GuiUtils.createTextFieldComponent("Interaction Name", interactionNameTextField),
                walkToPlayerCheckBox
        );

        this.load(npcInteractionMatcher);
    }

    private String getDefaultName() {
        return "Interaction" + interactionIndex;
    }

    private void load(NPCInteractionMatcher matcher) {
        if (matcher == null) {
            return;
        }

        interactionNameTextField.setText(matcher.getName());
        walkToPlayerCheckBox.setSelected(matcher.shouldWalkToPlayer());
        actionListPanel.load(matcher.getActions());
    }

    @Override
    protected NPCInteractionMatcher getMatcher() {
        String interactionName = interactionNameTextField.getText();
        boolean walkToPlayer = walkToPlayerCheckBox.isSelected();
        ActionMatcher[] actions = actionListPanel.getActions();

        if (StringUtils.isNullOrEmpty(interactionName)) {
            interactionName = this.getDefaultName();
        }

        return new NPCInteractionMatcher(interactionName, walkToPlayer, actions);
    }

    @Override
    protected void renderDialog() {
        removeAll();
        GuiUtils.setVerticalLayout(this, topComponent, actionListPanel);
    }
}

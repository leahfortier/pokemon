package mapMaker.dialogs.interaction;

import pattern.action.ActionMatcher;
import pattern.interaction.NPCInteractionMatcher;
import util.GuiUtils;

import javax.swing.JCheckBox;

public class NPCInteractionDialog extends InteractionDialog<NPCInteractionMatcher> {
    private final JCheckBox walkToPlayerCheckBox;

    public NPCInteractionDialog(NPCInteractionMatcher npcInteractionMatcher, int index) {
        super(npcInteractionMatcher, index);

        walkToPlayerCheckBox = GuiUtils.createCheckBox("Walk to playa");

        // Add checkbox to top component
        this.topComponent = GuiUtils.createHorizontalLayoutComponent(
                super.topComponent,
                walkToPlayerCheckBox
        );

        this.load(npcInteractionMatcher);
    }

    @Override
    protected void load(NPCInteractionMatcher matcher) {
        // Checkbox will be null if called from super's constructor before initialized
        if (walkToPlayerCheckBox == null || matcher == null) {
            return;
        }

        super.load(matcher);
        walkToPlayerCheckBox.setSelected(matcher.shouldWalkToPlayer());
    }

    @Override
    protected NPCInteractionMatcher getMatcher() {
        String interactionName = this.getInteractionName();
        boolean walkToPlayer = walkToPlayerCheckBox.isSelected();
        ActionMatcher[] actions = actionListPanel.getActions();

        return new NPCInteractionMatcher(interactionName, walkToPlayer, actions);
    }
}

package mapMaker.dialogs.interaction;

import pattern.action.ActionMatcher;
import pattern.interaction.InteractionMatcher;

public class BasicInteractionDialog extends InteractionDialog<InteractionMatcher> {
    public BasicInteractionDialog(InteractionMatcher interactionMatcher, int index) {
        super(interactionMatcher, index);
    }

    @Override
    protected InteractionMatcher getMatcher() {
        String interactionName = this.getInteractionName();
        ActionMatcher[] actions = actionListPanel.getActions();

        return new InteractionMatcher(interactionName, actions);
    }
}

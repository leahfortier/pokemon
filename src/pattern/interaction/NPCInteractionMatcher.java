package pattern.interaction;

import pattern.action.ActionMatcher;

public class NPCInteractionMatcher extends InteractionMatcher {
    private boolean walkToPlayer;

    public NPCInteractionMatcher(String name, boolean walkToPlayer, ActionMatcher[] actions) {
        super(name, actions);
        this.walkToPlayer = walkToPlayer;
    }

    public boolean shouldWalkToPlayer() {
        return this.walkToPlayer;
    }
}

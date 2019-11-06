package pattern.interaction;

import map.entity.movable.NPCInteraction;
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

    public NPCInteraction getInteraction() {
        return new NPCInteraction(this.shouldWalkToPlayer(), this.getActions());
    }
}

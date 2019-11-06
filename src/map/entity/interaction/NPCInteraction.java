package map.entity.interaction;

import pattern.action.ActionList;
import pattern.action.ActionMatcher;
import pattern.action.EntityActionMatcher.BattleActionMatcher;

public class NPCInteraction extends Interaction {
    private final boolean walkToPlayer;

    public NPCInteraction(boolean walkToPlayer, ActionList actions) {
        super(actions);
        this.walkToPlayer = walkToPlayer;
    }

    public boolean shouldWalkToPlayer() {
        return this.walkToPlayer;
    }

    public boolean isBattleInteraction() {
        for (ActionMatcher action : this.getActions()) {
            if (action instanceof BattleActionMatcher) {
                return true;
            }
        }

        return false;
    }
}
